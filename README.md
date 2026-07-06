<div align="center">

# Device Pulse

**A multiplatform IoT dashboard for monitoring live telemetry**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.11.1-4285F4)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## Overview

Device Pulse is a visual language for reading the health of a live telemetry connection at a glance. Built for a multiplatform sensor dashboard running on **Android**, **iOS**, **Desktop**, and **Web**.

Monitor device telemetry over MQTT with real-time updates, connection state visualization, and signal quality indicators.

<div align="center">

![Design System](specs/device-pulse-design-system.html)

**[View Full Design System](specs/device-pulse-design-system.html)**

</div>

---

## Features

- **Real-time Telemetry** — Live MQTT data streaming with instant updates
- **Connection State Visualization** — Color-coded chips with animated indicators
- **Signal Quality Trace** — Oscilloscope-style line showing connection health
- **Multiplatform** — Single codebase for Android, iOS, Desktop, and Web
- **Dark Theme** — Graphite instrument panel designed for long monitoring sessions

---

## Design System

### Color Palette

A graphite instrument panel rather than pure black — warm enough to hold long monitoring sessions. Two signal accents carry meaning, never decoration.

| Color | Hex | Usage |
|-------|-----|-------|
| Background | `#15181C` | Main background |
| Panel | `#1D2126` | Card surfaces |
| Signal Amber | `#FFB238` | Live, attention-worthy |
| Signal Cyan | `#45D6C4` | Stable, nominal |
| Fault Red | `#FF5C5C` | Errors, faults |
| Text Primary | `#E9EDF0` | Main text |
| Text Muted | `#8A939C` | Secondary text |
| Text Dim | `#5B646C` | Labels, timestamps |

### Typography

| Style | Size | Weight | Font |
|-------|------|--------|------|
| Display | 44px | 600 | Space Grotesk |
| Title | 24px | 500 | Space Grotesk |
| Subtitle | 17px | 600 | Space Grotesk |
| Body | 16px | 400 | IBM Plex Sans |
| Body Small | 13.5px | 500 | IBM Plex Sans |
| Caption | 12.5px | 400 | IBM Plex Sans |
| Overline | 11px | 500 | IBM Plex Mono |
| Data Large | 34px | 500 | IBM Plex Mono |
| Data Medium | 20px | 500 | IBM Plex Mono |
| Data Small | 15px | 400 | IBM Plex Mono |
| Mono Micro | 11px | 400 | IBM Plex Mono |
| Button Label | 12.5px | 600 | IBM Plex Mono |

### Connection Chips

Connection state is never a color alone — a dot plus a word plus, where it matters, a trace.

- **Connected** — Cyan dot + label
- **Connecting** — Amber dot + label  
- **Reconnecting** — Blinking amber dot + backoff timer
- **Disconnected** — Gray dot + label
- **Error** — Red dot + label

---

## Tech Stack

| Concern | Technology |
|---------|------------|
| UI | Compose Multiplatform + Material 3 |
| ViewModel | AndroidX ViewModel (KMP) |
| DI | Koin |
| Networking | Ktor (MQTT over WebSocket) |
| Database | Room (KMP) |
| MQTT Broker | `test.mosquitto.org:8080` |

---

## Project Structure

```
iPulse/
├── shared/              # KMP module — all UI, ViewModels, repositories
├── androidApp/          # Android entry point
├── desktopApp/          # Desktop entry point (JVM)
├── webApp/              # Web entry point (Wasm + JS)
├── iosApp/              # iOS entry point (SwiftUI)
├── publisher/           # Test MQTT publisher
└── specs/               # Design system reference
```

---

## Getting Started

### Prerequisites

- JDK 11+
- Android Studio (for Android)
- Xcode (for iOS)

### Run

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
./gradlew :shared:testAndroidHostTest
./gradlew :shared:jvmTest
./gradlew :shared:wasmJsTest
```

---

## Design Philosophy

> The signal trace is the one motif repeated everywhere a connection appears — dashboard header, per-device row, detail view, desktop panel. It's not decorative: a flat, tight line means stable; a jagged one means the connection is alive but unhealthy; a dashed flat line means no data at all. A user should be able to tell those three apart without reading a single word, the same way you'd read an oscilloscope.

---

## License

Copyright 2024 Mohammad Esteki

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

---

<div align="center">

**Device Pulse** — Design System v0.1

Built with Compose Multiplatform

</div>
