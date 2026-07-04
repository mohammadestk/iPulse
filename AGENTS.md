# AGENTS.md — iPulse (Device Pulse)

## What this is

Kotlin Multiplatform (Compose Multiplatform) project: real-time MQTT sensor telemetry monitor.
Targets: Android, iOS, Desktop (JVM), Web (JS/WasmJS — non-goal for Device Pulse, ignore for now).

## Key files

- `specs/device-pulse-spec.md` — the engineering spec. Read before implementing anything.
- `specs/device-pulse-design-system.html` — UI design tokens and screen references.
- `shared/` — all shared KMP code (domain, transport, UI).
- `androidApp/`, `desktopApp/`, `iosApp/`, `webApp/` — thin platform shells.

## Build & run

```bash
./gradlew :androidApp:assembleDebug          # Android APK
./gradlew :desktopApp:run                     # Desktop JVM
./gradlew :webApp:wasmJsBrowserDevelopmentRun # Web (WasmJS)
```

## Test

```bash
./gradlew :shared:testAndroidHostTest         # Android host tests
./gradlew :shared:jvmTest                     # JVM desktop tests
```

All domain tests live in `shared/src/commonTest/`. They use `FakeTransport` — no real broker required.

## Architecture rules (from spec)

- **`commonMain` must never import MQTT libraries.** Only knows the `Transport` interface. This is a hard boundary — if you find an MQTT type in `commonMain`, you've leaked an abstraction.
- `ConnectionState` is a sealed class hierarchy (not `isConnected: Boolean`).
- `Transport` is dumb I/O. `ConnectionManager` in `commonMain` owns state and backoff.
- Backoff resets only on **message received**, not on socket connect.
- Buffer: `Channel<Reading>(capacity = 200, onBufferOverflow = DROP_OLDEST)`.
- **iOS transport**: MQTT-over-WebSocket via Ktor HTTP client (Darwin engine). Do NOT use `ktor-network` raw sockets — the WebSocket approach is the documented, mature option (spec Section 3).
- **JVM transport**: standard MQTT-over-TCP (no WebSocket needed). JVM sockets are not the constrained path (spec Section 6).

## Ownership boundary (spec Section 0)

- **Phases 0–2** (domain logic, state machine, backoff, backpressure, tests): implement a draft but the owner will review and likely rewrite. Document every non-obvious decision in `DECISIONS.md`.
- **Phase 3+** (transport wiring, publisher, UI): free to implement fully.
- **Phase 0 is a blocker**: iOS transport strategy must be resolved before Phase 2+ transport work begins. Write outcome to `DECISIONS.md`.
- **`test.mosquitto.org` caveat**: WebSocket/TLS support is often unavailable on the public test server. Local Mosquitto via `docker-compose` is the reliable fallback for demos.

## Source set structure

```
shared/src/
├── commonMain/     → domain logic, Transport interface, ConnectionManager
├── jvmCommon/      → JVM transport impl (shared by androidMain + desktopMain)
├── androidMain/    → Android lifecycle, DataStore settings
├── iosMain/        → iOS transport impl
├── desktopMain/    → Compose Desktop entry
├── commonTest/     → FakeTransport + all domain/state-machine tests
├── jvmTest/
├── iosTest/
└── androidHostTest/
```

## Gotchas

- `gradle.properties` sets `org.gradle.configuration-cache=true` and `org.gradle.caching=true`. Both are on.
- JDK toolchain: Amazon Corretto **21** (see `gradle-daemon-jvm.properties`).
- iOS framework is built as a **static** framework named `Shared`.
- `commonMain` already depends on Compose runtime, foundation, material3, and lifecycle. Don't add MQTT libs here.
- The spec explicitly bans these anti-patterns: `isConnected: Boolean`, reset-on-connect-only backoff, duplicated state machine logic in platform modules.
