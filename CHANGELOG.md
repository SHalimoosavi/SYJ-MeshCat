# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/) once
`v1.0.0` is released. Until then, milestones are tracked explicitly below.

## [Unreleased]

### Milestone 3 — BLE Scanning (2026-07-26)

#### Added
- `PermissionsManager`: centralizes the Android-version-dependent
  Bluetooth permission model (legacy BLUETOOTH/BLUETOOTH_ADMIN/
  ACCESS_FINE_LOCATION pre-API 31, granular BLUETOOTH_SCAN/CONNECT/
  ADVERTISE from API 31, POST_NOTIFICATIONS from API 33).
- `BleTransport` interface + `AndroidBleTransport` implementation:
  wraps `BluetoothLeScanner` in a cancellable `callbackFlow`, filtered by
  the app's custom mesh service UUID (`BleConstants.MESH_SERVICE_UUID`),
  mapping `ScanCallback` results/failures into `BleScanEvent`.
- `PeerRepository` / `PeerRepositoryImpl`: single source of truth for
  BLE-visible peers. Gates scanning on Bluetooth availability and
  permissions, de-duplicates peers by address, and runs a background
  eviction loop that prunes peers unseen for `PEER_STALE_TIMEOUT_MS`
  (BLE visibility is transient — a peer walking out of range never sends
  a "goodbye").
- `MeshForegroundService` (`LifecycleService` + Hilt): keeps scanning
  alive in the background via a persistent low-priority notification
  showing live peer count, with a Stop action. Registered in the
  manifest with `foregroundServiceType="connectedDevice"`.
- `BleModule` (Hilt) binding `BleTransport` → `AndroidBleTransport` and
  `PeerRepository` → `PeerRepositoryImpl`.
- Home screen now shows live scan state, a Start/Stop Scan button wired
  to the Android runtime permission dialog, and a list of discovered
  peers (nickname, address, RSSI).
- Unit tests: `BleScanFailureReasonTest`, `PermissionsManagerTest` (mockk
  static-mocks `ContextCompat`), `PeerRepositoryImplTest` (permission/
  availability gating, peer discovery, stop/start idempotency — using a
  `FakeBleTransport` test double), extended `HomeViewModelTest`.
  Instrumentation tests extended in `HomeScreenTest` (peer list, empty
  state, scan button interaction).

#### Notes
- BLE **advertising** (the other half of peer discovery) is Milestone 4 —
  this milestone only scans. `BLUETOOTH_ADVERTISE` is requested now
  alongside `SCAN`/`CONNECT` to avoid a second permission prompt later,
  but isn't used yet.
- ktlint and Detekt are wired into the build but set to
  `ignoreFailures = true` for now — see README/CHANGELOG note in
  Milestone 2. They still run and report; nothing here has been verified
  against the real toolchain in this environment.
- As with Milestone 2: statically verified only (XML well-formedness,
  brace/paren balance, package/directory/resource cross-checks,
  `gradlew` shell-syntax). No real `./gradlew build` has been run against
  the Android/Kotlin toolchain — verify locally and report back any
  compiler errors.
- Next: Milestone 4 — BLE advertising (the other half of peer discovery,
  making this device visible to other mesh nodes).

### Milestone 2 — Android Architecture (2026-07-25)

#### Added
- Gradle Kotlin DSL multi-module build: root `settings.gradle.kts` /
  `build.gradle.kts`, `app` module, centralized version catalog
  (`gradle/libs.versions.toml`) covering AGP 8.5.2, Kotlin 2.0.21, Compose
  BOM 2024.10.01, Hilt 2.51.1, Room 2.6.1, Navigation Compose 2.8.4,
  KSP, kotlinx-coroutines and kotlinx-serialization.
- Gradle wrapper scripts (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.properties` pinned to Gradle 8.9).
- `AndroidManifest.xml` with BLE/foreground-service/notification
  permissions declared (not yet requested at runtime — lands in
  Milestone 3/4), loopback-only network security config for the future
  REST bridge, adaptive launcher icon (brand amber/violet/near-black).
- Hilt DI graph bootstrap: `MeshChatApplication` (`@HiltAndroidApp`),
  `MainActivity` (`@AndroidEntryPoint`), `DispatcherModule` (testable
  `DispatcherProvider`), `AppModule` (process-lifetime `CoroutineScope`
  via `@ApplicationScope`).
- MVVM skeleton: `BaseViewModel` (coroutine launch + error funnel),
  established `UiState` / `ViewModel` / `Screen` pattern via the first
  real screen — `HomeUiState`, `HomeViewModel`, `HomeScreen`.
- Jetpack Compose scaffold: Material 3 theme (`Color.kt`, `Type.kt`,
  `Theme.kt`) with dark-mode-first brand palette and optional Android 12+
  dynamic color; type-safe Navigation Compose graph (`MeshRoute`,
  `MeshNavHost`) with `Home` as the sole destination.
- ktlint and Detekt wired into the `app` module build, with a minimal
  project-specific Detekt ruleset (`config/detekt/detekt.yml`).
- Unit test (`HomeViewModelTest`, JUnit4 + Turbine + a `TestDispatcherProvider`
  fake) and instrumentation test (`HomeScreenTest`, Compose UI testing)
  establishing the testing conventions for every screen going forward.

#### Notes
- No BLE, crypto, routing, or database code yet — this milestone is
  architecture and scaffolding only, per the roadmap.
- Built and statically verified in a sandboxed environment without
  Android SDK/Google Maven access: all XML validated well-formed, all
  Kotlin/Gradle files brace/paren-balanced, `gradlew` shell-syntax
  checked, and every resource/package reference cross-checked against
  the files that define it. A full `./gradlew build` has **not** been
  executed against the real Android toolchain — verify locally and
  report back any compiler errors.
- Next: Milestone 3 — BLE scanning (peer discovery, permissions manager,
  foreground service groundwork).

### Milestone 1 — Repository Structure (2026-07-25)

#### Added
- Initial repository scaffold: `app/` (Android module placeholder),
  `sdk/python/` (Python SDK placeholder), `docs/`, `examples/`,
  `.github/workflows/`.
- Governance and community files: `README.md`, `LICENSE` (MIT),
  `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, `CHANGELOG.md`.
- Planning documents: `ROADMAP.md`, `ARCHITECTURE.md`, `API.md`,
  `PROTOCOL.md`.
- `.gitignore` covering Android/Gradle and Python build artifacts.

#### Notes
- No application code (Kotlin or Python) has been implemented yet — this
  milestone establishes structure and documentation only.
- Next: Milestone 2 — Android architecture (Gradle project, Hilt, MVVM
  skeleton, Compose scaffold).
