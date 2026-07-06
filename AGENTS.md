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

## SOLID Principles

- **S — Single Responsibility**: Every class does one thing. A repository stores/retrieves data. A service orchestrates domain operations. A ViewModel manages UI state. Never mix concerns.
- **O — Open/Closed**: Extend behavior through composition and interfaces, not by modifying existing classes. Add new use cases or services rather than adding `if/else` branches to existing ones.
- **L — Liskov Substitution**: Interface implementations are interchangeable. If `BrokerConnectionImpl` is swapped for a test fake, everything downstream works without changes.
- **I — Interface Segregation**: Repository interfaces are narrow. `DeviceRepository` only exposes reads; `BrokerConnection` only exposes connection ops. Clients depend only on what they use.
- **D — Dependency Inversion**: Domain and presentation depend on abstractions (interfaces in `domain/repository/`), never on concrete implementations. DI wires them at runtime.

## Clean Architecture Layers

```
presentation/  →  UI, ViewModels, MVI contracts, theme, navigation
domain/        →  Models, repository interfaces, use cases
data/          →  Repository implementations, remote/data sources, DTOs
```

Rules:
- **Domain is pure Kotlin.** No Compose, no coroutines framework, no platform APIs. Models are data classes/enums. Repository interfaces return `Flow` or `suspend` results.
- **Data depends on domain.** Implementations import domain interfaces and models. Domain never imports data.
- **Presentation depends on domain.** ViewModels inject domain services, never data-layer classes. UI maps domain models to UI models.
- **One direction of dependency:** presentation → domain ← data. Domain is the center; it knows nothing about its consumers or providers.
- **No god classes.** If a class has more than ~5 responsibilities, split it. Domain services orchestrate — they don't parse payloads, manage in-memory state, or know about MQTT topics.
- **Use cases are mandatory.** Every interaction between presentation and domain goes through a use case. Each use case encapsulates one business operation with a single `operator fun invoke()` method. Use cases are small, focused, and named with verb-noun pairs (e.g. `ConnectToBroker`, `GetDeviceById`, `ObserveTelemetry`). They live in `domain/usecase/`, inject domain services or repositories, and are the only way ViewModels access domain logic. If a use case only delegates to one method, that's fine — it's a seam for testing and a boundary that keeps the domain explicit.

## Conventions

- **One declaration per file.** Each file contains exactly one class, interface, object, or enum. Name the file after the declaration.
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
