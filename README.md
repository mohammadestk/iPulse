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

## License

Apache 2.0
