# Device Pulse — Engineering Specification

**Target reader:** an implementation agent (human or AI) building this from scratch.
**Owner intent:** this is a portfolio project. The connection-state machine, backoff policy,
and backpressure policy (Phase 2) are the core engineering content and should be understood,
not just generated — see "Ownership boundary" below before starting.

---

## 0. Ownership boundary — read first

- **Phase 0–2 (domain logic, state machine, backoff, backpressure, tests):** implement a first
  draft, but the project owner will review and likely rewrite this by hand. Do not treat this
  phase as "done" once it compiles and passes tests — flag every non-obvious design decision
  you made in a `DECISIONS.md` file at the repo root so the owner can interrogate each one.
- **Phase 3+ (transport wiring, publisher, UI):** free to implement fully.
- If at any point a decision in this spec looks wrong or a better alternative exists, **stop
  and surface it** rather than silently deviating or silently complying. State the tradeoff.

---

## 1. Product summary

Device Pulse is a real-time sensor telemetry monitor (temperature, pressure) over MQTT,
running on **Android, iOS, and Desktop (JVM)** from one shared Kotlin Multiplatform codebase.
It subscribes to a namespaced topic tree, renders live readings, and — this is the point of
the project — visibly and correctly handles the unhappy path: dropped connections, broker
misbehavior, backgrounding, and message floods.

**Non-goals (explicitly out of scope — do not build these):**
- User accounts, auth, or multi-tenant anything.
- A hosted backend service. The "backend" is a public MQTT broker plus a local publisher tool.
- Push notifications.
- Data persistence beyond: (a) last-known-reading cache in memory, (b) broker settings in
  local device storage.
- Web/JS target.

---

## 2. Module structure

```
device-pulse/
├── shared/
│   ├── commonMain/     → domain: ConnectionState, DisconnectReason, BackoffPolicy,
│   │                      BufferPolicy, Transport interface, ConnectionManager,
│   │                      RawTransportEvent, domain models (Reading, DeviceId, Topic)
│   ├── jvmCommon/       → transport impl shared by androidMain + desktopMain (JVM MQTT client)
│   ├── androidMain/     → Android lifecycle wiring, DataStore-backed settings
│   ├── iosMain/         → iOS transport impl (see Phase 0 — pending research spike)
│   ├── desktopMain/     → Compose Desktop entry point
│   └── commonTest/      → FakeTransport + all domain/state-machine tests (no real broker)
├── androidApp/          → Compose UI (thin — presentation only)
├── desktopApp/           → Compose UI (thin — presentation only)
├── iosApp/               → Compose UI via Compose Multiplatform iOS target (thin)
├── publisher/           → standalone JVM console app: fakes sensor data, injects
│                           disconnects/jitter/malformed payloads for testing resilience
├── docker/
│   └── docker-compose.yml → local Mosquitto broker for reliable, controllable demos
├── DECISIONS.md         → agent-authored log of non-obvious calls made during implementation
└── README.md            → architecture rationale (see Section 8)
```

**Hard rule:** all logic in `commonMain` must be free of any MQTT-library import. It only
knows about the `Transport` interface (Section 4). If you find yourself importing an MQTT
type into `commonMain`, stop — that's a leaked abstraction.

---

## 3. Phase 0 — Research spike (do this before writing domain code)

Determine the iOS transport strategy. Two candidates, in order of preference:

1. **MQTT over WebSocket via Ktor's HTTP client (Darwin engine on iOS)** — the recommended
   default. Ktor's WebSocket client support is a mature, documented multiplatform feature
   (unlike `ktor-network`'s raw socket support, which is less certain on Native/iOS — verify
   current status before relying on it). Implement minimal MQTT 3.1.1 packet framing
   (CONNECT/CONNACK, PUBLISH, SUBSCRIBE/SUBACK, PINGREQ/PINGRESP, DISCONNECT) in `commonMain`,
   sent as binary frames over a WebSocket connection using subprotocol `mqtt` (or legacy
   `mqttv3.1`) — this is the standard MQTT-over-WebSocket handshake, not a custom protocol.
   Public test endpoint: `ws://test.mosquitto.org:8080/mqtt` (unencrypted, unauthenticated;
   `wss://test.mosquitto.org:8081/mqtt` for TLS). **Caveat, not a footnote**: Mosquitto's own
   docs for this test server state it often runs experimental code and that "websockets and
   TLS support are the most likely to be unavailable" — this is exactly why the local
   Mosquitto via `docker-compose` (Section 6) is not optional polish, it's the fallback that
   makes demos reliable. Do not depend on the public server's WebSocket listener for anything
   you plan to demo live.
2. **Fallback**: native iOS MQTT client (e.g. CocoaMQTT) via Kotlin/Native cinterop, wrapped
   behind the same `Transport` interface in `iosMain`. Expect real friction here (cinterop
   header generation, CocoaPods integration) — budget accordingly. Only fall back to this if
   option 1's WebSocket framing proves genuinely broken in testing, not preemptively.

Do not start Phase 2+ transport work until this is resolved. Write the outcome and reasoning
into `DECISIONS.md`.

---

## 4. Domain model (commonMain) — Phase 1

### 4.1 ConnectionState (sealed, not booleans)

```kotlin
sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val since: Instant) : ConnectionState()
    data class Reconnecting(val attempt: Int, val nextRetryAt: Instant) : ConnectionState()
    data class Disconnected(val reason: DisconnectReason) : ConnectionState()
    data class Failed(val reason: DisconnectReason, val attempts: Int) : ConnectionState()
}

sealed class DisconnectReason {
    data object NetworkLost : DisconnectReason()
    data object Backgrounded : DisconnectReason()
    data object UserInitiated : DisconnectReason()
    data class BrokerRejected(val code: Int, val message: String) : DisconnectReason()
    data object HandshakeTimeout : DisconnectReason()
    data class Unknown(val cause: Throwable) : DisconnectReason()
}
```

`Backgrounded` must be distinct from `NetworkLost` — they need different recovery UX
(auto-resume on foreground vs. active retry).

### 4.2 Backoff policy — exact numbers, do not invent your own

- Algorithm: exponential backoff with **full jitter** (AWS-style): `sleep = random(0, min(cap, base * 2^attempt))`
- `base = 1_000ms`, `cap = 60_000ms`
- **Reset condition: backoff resets only when a message is actually received, not merely on
  socket connect.** A broker can accept a TCP/MQTT connection and never deliver a single
  message on the subscribed topic (wrong topic, ACL issue, etc.) — resetting on connect alone
  would mask that failure mode. This is a deliberate, non-obvious design decision; the agent
  must not "simplify" it back to reset-on-connect.
- Write this as a pure function: `fun nextBackoff(attempt: Int, random: Random): Duration` —
  fully unit-testable without any I/O.

### 4.3 Backpressure / buffering policy — exact numbers

- Bounded channel, capacity **200** messages per subscribed topic.
- Overflow policy: **drop-oldest**. Justification: for a live telemetry display, only the
  latest reading is meaningful — buffering old readings under load risks OOM and shows stale
  data anyway. (If a future feature needs guaranteed delivery — e.g. commands, not
  telemetry — that would need a different policy; do not generalize this decision beyond
  telemetry display.)
- Implement via `Channel<Reading>(capacity = 200, onBufferOverflow = BufferOverflow.DROP_OLDEST)`
  or platform equivalent — exposed as `Flow<Reading>` to consumers.

### 4.4 Transport interface — dumb I/O only, no backoff/state logic inside it

```kotlin
interface Transport {
    suspend fun connect(config: BrokerConfig)
    suspend fun disconnect()
    suspend fun subscribe(topic: String, qos: Int)
    val events: Flow<RawTransportEvent>
}

sealed class RawTransportEvent {
    data object Connected : RawTransportEvent()
    data class Disconnected(val reason: DisconnectReason) : RawTransportEvent()
    data class MessageReceived(val topic: String, val payload: ByteArray) : RawTransportEvent()
    data class Error(val cause: Throwable) : RawTransportEvent()
}
```

`ConnectionManager` (commonMain) consumes `Transport.events`, owns the `ConnectionState`
`StateFlow`, drives reconnect attempts via the backoff policy, and applies the buffering
policy to incoming messages. `Transport` itself must never reference `ConnectionState` —
that separation is the point: I/O is dumb, orchestration is testable pure logic.

### 4.5 Topic namespacing

`devicepulse/{clientId}/sensors/{metric}` where `clientId` is a UUID generated once on first
launch and persisted locally. Public brokers are shared infrastructure — an unnamespaced topic
will collect other people's test traffic and produce undiagnosable noise. Do not skip this.

---

## 5. Testing requirements (commonTest) — Phase 2

Build a `FakeTransport` implementing `Transport` that a test can fully control: force a
`Connected` event, force a `Disconnected(reason)` event, emit `MessageReceived` on demand,
introduce artificial delay. Required test cases — all must exist before Phase 3 starts:

1. Backoff duration stays within `[0, min(cap, base * 2^attempt)]` for attempts 0–10.
2. Backoff attempt counter resets on `MessageReceived`, **not** on `Connected` alone —
   write a test that connects, receives no message, disconnects again, and asserts the
   next backoff duration reflects a continued (not reset) attempt count.
3. `ConnectionState` transitions correctly through: `Idle → Connecting → Connected →
   Reconnecting → Connected` and `... → Failed` after some attempt ceiling (define one,
   e.g. 8 attempts, then surface `Failed` requiring manual retry).
4. Buffer drop-oldest: flood 500 messages into a 200-capacity channel, assert exactly the
   most recent 200 survive and no exception/OOM occurs.
5. Every `DisconnectReason` variant reaches a distinguishable `ConnectionState` — no
   reason should be silently coerced into a generic "disconnected" state.

If any of these can't be tested without real I/O, that's a sign the interface boundary in
4.4 is wrong — fix the boundary before writing more code, don't work around it with
integration tests that need a real broker.

---

## 6. Transport implementation — Phase 3

- `jvmCommon`: real MQTT client (e.g. HiveMQ MQTT Client or Eclipse Paho) implementing
  `Transport`, shared by `androidMain` and `desktopMain` since both are JVM targets. These can
  use standard MQTT-over-TCP (no WebSocket needed) since JVM sockets aren't the constrained
  path — WebSocket is only the iOS workaround, not a project-wide requirement.
- `iosMain`: per Phase 0 outcome (MQTT-over-WebSocket via Ktor, most likely).
- Default demo broker: `test.mosquitto.org` — port 1883 for JVM targets (plain MQTT-over-TCP),
  `ws://test.mosquitto.org:8080/mqtt` for the iOS WebSocket path. Given the reliability caveat
  in Section 3, treat this as the "works most of the time" default, not the reliable path —
  the settings screen must let the user point at the local Mosquitto instance
  (`docker/docker-compose.yml`) for demos where you need to guarantee the connection can
  actually be killed and recovered on camera.

---

## 7. Publisher tool — Phase 4

Standalone JVM console app (`publisher/`), separate from the main app modules. Publishes
fake temperature/pressure readings on a schedule, and **must** support CLI flags to inject:
`--drop-rate=0.1` (randomly skip publishes), `--jitter-ms=500` (randomize interval),
`--disconnect-every=30s` (force broker disconnect periodically). Without this, the resilience
code in Phase 1–2 never actually gets exercised against real misbehavior — a well-behaved
broker connection proves nothing about the reconnect logic.

---

## 8. Android-specific decision — finalize now, don't leave implicit

**Default: reconnect-on-resume, not a foreground service.** On backgrounding, the MQTT socket
dies (expected on Android 14+); on foreground resume, `ConnectionManager` re-attempts
connection through the normal backoff path, using `DisconnectReason.Backgrounded` to
distinguish this from a real network failure in the UI (e.g. no alarming red state while
backgrounded). This is simpler and more honest about mobile OS constraints than fighting for
a persistent foreground service and its associated permission/UX overhead. If always-on
monitoring while backgrounded becomes a real requirement later, that's a deliberate scope
change requiring a `FOREGROUND_SERVICE` justification — do not add it speculatively.

---

## 9. UI — Phase 5 (last, deliberately)

Do not start UI work until Phase 1–4 are done and tested. Compose UI (shared via Compose
Multiplatform across Android/iOS/Desktop) must be a pure function of `ConnectionState` and
`Reading` — if the UI layer needs to know anything about MQTT, QoS, or backoff timers
directly, an abstraction has leaked; go back and fix the domain layer instead of patching it
in the UI.

**Design tokens and screen references**: match the delivered design system
(`device-pulse-design-system.html`) exactly — colors, type scale (12 named roles), the
connection-state visual system (waveform shape per state), and the three reference screens
(Android dashboard, iOS device detail, Desktop fleet console). Two structural rules from that
system, already fixed once and not to be regressed:
- Any state-of-data indicator (chip, badge) must sit **adjacent to the label of the data it
  qualifies**, never in a header/topbar disconnected from the reading itself.
- When `ConnectionState` is anything other than `Connected`, the reading value itself must be
  visually dimmed — staleness is signaled by both the chip and the data's own rendering, not
  the chip alone.

Required screens (Android + iOS + Desktop, same domain logic):
1. Dashboard / device list
2. Device detail (chip + reading + waveform/chart + connection log)
3. Disconnected/empty state (no devices discovered yet)
4. Broker settings (host, port/URL, switch between public broker and local Mosquitto)

---

## 10. Definition of Done

- [ ] Phase 0 spike resolved and documented in `DECISIONS.md`, iOS transport strategy chosen
- [ ] All Section 5 tests passing, zero real I/O in `commonTest`
- [ ] `commonMain` has zero MQTT-library imports (grep-verifiable)
- [ ] Publisher tool can force-break the connection on demand and the app visibly recovers
- [ ] Every `DisconnectReason` reachable and distinguishable in the UI
- [ ] README documents backoff numbers, buffering policy, and topic namespacing with the
      *why*, not just the *what* — this is the artifact a reviewer reads first
- [ ] `DECISIONS.md` lists every place the agent deviated from or extended this spec

---

## 11. What NOT to do (anti-patterns explicitly ruled out by earlier design review)

- Do not model connection status as `isConnected: Boolean`.
- Do not reset backoff on socket connect alone.
- Do not let any platform module duplicate state-machine or backoff logic — it belongs in
  `commonMain` only.
- Do not change a shared UI component's layout model (e.g. a CSS/Compose `Modifier` pattern
  used in multiple places) without checking every consumer of it first.
- Do not build a hosted backend, user accounts, or any persistence beyond what Section 1
  specifies.
