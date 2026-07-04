# AGENTS.md — iPulse

## Project Overview

iPulse is a Kotlin Multiplatform IoT dashboard that monitors device telemetry over MQTT. It targets Android, iOS, Desktop (JVM), and Web (Wasm/JS) with shared UI and business logic.

## Architecture

- **Pattern**: Clean Architecture + MVI + SOLID principles
- **Shared layer**: All UI (Compose Multiplatform) and business logic lives in the `:shared` module
- **Platform modules**: Thin entry points that wire up the shared module

## Module Map

| Module | Purpose |
|--------|---------|
| `shared/` | KMP module — all UI, ViewModels, repositories, data sources |
| `androidApp/` | Android entry point (`MainActivity`) |
| `desktopApp/` | Desktop entry point (`MainKt`) |
| `webApp/` | Web entry point (Wasm + JS targets) |
| `iosApp/` | iOS entry point (SwiftUI wrapping Compose via `MainViewController`) |
| `publisher/` | Test tool — publishes sample MQTT data to `test.mosquitto.org:8080` |
| `specs/` | Design system reference (HTML) |

## Build & Run

```bash
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run
./gradlew :desktopApp:hotRun --auto    # hot reload

# Web (Wasm — faster, modern browsers)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Web (JS — legacy browser support)
./gradlew :webApp:jsBrowserDevelopmentRun

# iOS — open iosApp/ in Xcode
```

## Testing

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:jvmTest
./gradlew :shared:wasmJsTest
./gradlew :shared:jsTest
./gradlew :shared:iosSimulatorArm64Test
```

## Tech Stack

| Concern | Library | Notes |
|---------|---------|-------|
| Networking | Ktor | MQTT over WebSocket |
| Database | Room | KMP-compatible |
| DI | Koin | |
| MQTT Broker | `test.mosquitto.org:8080` | WebSocket, hardcoded |
| UI | Compose Multiplatform + Material 3 | |
| ViewModel | AndroidX ViewModel (KMP) | `lifecycle-viewmodel-compose` |

## Design System

**Source of truth**: `specs/device-pulse-design-system.html`

Key tokens:
- **Background**: `#15181C` (graphite)
- **Signal amber**: `#FFB238` (live, attention)
- **Signal cyan**: `#45D6C4` (stable, nominal)
- **Fault red**: `#FF5C5C` (errors)
- **Fonts**: Space Grotesk (display), IBM Plex Sans (body), IBM Plex Mono (data)

Key components: Connection chips (connected/connecting/reconnecting/disconnected/error), signal trace (oscilloscope line), sensor readouts, event log.

## Source Set Layout (`shared/src/`)

```
commonMain/     → All shared UI, ViewModels, repositories, domain logic
commonTest/     → Shared unit tests
androidMain/    → Android-specific implementations
androidHostTest/→ Android instrumented tests
iosMain/        → iOS-specific (MainViewController)
iosTest/        → iOS tests
jvmMain/        → Desktop-specific
jvmCommon/      → Shared JVM code (desktop + Android host tests)
jvmTest/        → Desktop tests
jsMain/         → JS web target
wasmJsMain/     → Wasm web target
```

## Key Versions

- Kotlin: `2.4.0`
- Compose Multiplatform: `1.11.1`
- AGP: `9.2.1`
- Android compileSdk: `37`, minSdk: `24`
- Version catalog: `gradle/libs.versions.toml`

## Conventions

- **New shared code** goes in `shared/src/commonMain/kotlin/dev/esteki/ipulse/`
- **Platform-specific** code goes in the corresponding `*Main` source set
- **iOS entry**: `MainViewController()` wraps `App()` composable
- **Desktop main class**: `dev.esteki.ipulse.MainKt`
- **Android namespace**: `dev.esteki.ipulse`
- **Shared namespace**: `dev.esteki.ipulse.shared`

## Gotchas

- `settings.gradle.kts` uses `TYPESAFE_PROJECT_ACCESSORS` — reference modules as `projects.shared` not `":shared"`
- Gradle config cache and caching are enabled (`gradle.properties`)
- JVM target is `11` across all modules
- Web has two targets: `wasmJs` (preferred) and `js` (legacy) — don't confuse them
- The `publisher/` module is a standalone Kotlin JVM tool, not part of the KMP build
