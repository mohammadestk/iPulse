# Device Pulse

**Multiplatform IoT Dashboard for Live Telemetry Monitoring**

Built with Compose Multiplatform, Clean Architecture, and MQTT

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVI-0B6E4F)](#architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](#license)

---

## Screens

![Device Pulse Screens](specs/screens.png)

- **Dashboard** — Device list with live status, connection chips, signal trace
- **Device Detail** — Sensor readout, connection log, signal quality
- **Empty State** — Initial state before any device publishes

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                Feature Layer                │
│  Screen ← ViewModel ← UseCase              │
├─────────────────────────────────────────────┤
│                Domain Layer                 │
│  Repository Interface ← Domain Models       │
├─────────────────────────────────────────────┤
│                 Data Layer                  │
│  Repository Impl → DataSource → MQTT        │
└─────────────────────────────────────────────┘
```

- **MVI** — Unidirectional data flow with `StateFlow`
- **Package by feature** — Dashboard, DeviceDetail
- **Clean separation** — No framework leaks between layers

---

## Tech Stack

| Category | Library |
|----------|---------|
| UI | Compose Multiplatform + Material 3 |
| ViewModel | AndroidX ViewModel (KMP) |
| DI | Koin |
| Networking | Ktor (MQTT over WebSocket) |
| Database | Room (KMP) |
| Navigation | Navigation 3 |

---

## Design System

**Device Pulse** — A graphite instrument panel designed for long monitoring sessions

- **Signal Amber** (`#FFB238`) — Live, attention-worthy
- **Signal Cyan** (`#45D6C4`) — Stable, nominal
- **Fault Red** (`#FF5C5C`) — Errors, faults
- **IBM Plex Mono** — Data readouts, timestamps, topic strings

> See the full spec at [`specs/device-pulse-design-system.html`](specs/device-pulse-design-system.html)

---

## Publishing Test Data

The dashboard connects to a public MQTT broker and subscribes to a single topic. You can send test data using [MQTTX](https://mqttx.app/), a cross-platform MQTT client.

### Broker Settings

| Field    | Value                |
|----------|----------------------|
| Host     | `test.mosquitto.org` |
| Port     | `8081`               |
| Protocol | WebSocket            |
| Path     | `/mqtt`              |

### Topic

```
/esteki/devices
```

### Payload

Each message is a single sensor reading. All fields except `unit` and `timestamp` are required.

`{"deviceId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","name":"Ward B — bed 4","sensorType":"temperature","value":24.6,"unit":"°C","timestamp":1720000000000,"status":"live"}`

| Field        | Type   | Required | Description |
|--------------|--------|----------|-------------|
| `deviceId`   | string | yes      | Stable UUID grouping sensors under one device |
| `name`       | string  | yes      | Human-readable display name |
| `sensorType` | string  | yes      | `temperature`, `pressure`, or `humidity` |
| `value`      | number  | yes      | The numeric reading |
| `unit`       | string  | no       | Override unit (defaults: °C, hPa, %RH) |
| `timestamp`  | integer | no       | Epoch ms (defaults to receive time) |
| `status`     | string  | yes      | `live`, `reconnecting`, or `offline` |

### Steps

1. Open MQTTX and create a new connection
2. Enter the broker settings above
3. Connect, then open the **Publish** tab
4. Set topic to `/esteki/devices`
5. Paste a JSON payload and hit **Publish**

> The full JSON Schema is at [`specs/telemetry-payload.schema.json`](specs/telemetry-payload.schema.json).

---

## Module Structure

```
iPulse/
├── shared/              # KMP module — all UI, ViewModels, repositories
├── androidApp/          # Android entry point
├── desktopApp/          # Desktop entry point (JVM)
├── webApp/              # Web entry point (Wasm + JS)
├── iosApp/              # iOS entry point (SwiftUI)
├── publisher/           # Test MQTT publisher
└── specs/               # Design system specification
```

---

## Getting Started

```bash
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run

# Web (Wasm)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# iOS — open iosApp/ in Xcode
```

---

## Testing

```bash
./gradlew :shared:test
```

---

## License

Apache 2.0
