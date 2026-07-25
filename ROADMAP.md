# Roadmap

SYJ-MeshChat is built one milestone at a time. Each milestone must compile,
pass its tests, and update `CHANGELOG.md` before the next begins.

| # | Milestone | Status |
|---|-----------|--------|
| 1 | Repository structure | ✅ Complete |
| 2 | Android architecture (Gradle, Hilt, MVVM skeleton) | ✅ In progress (this milestone) |
| 3 | BLE scanning | ⬜ Not started |
| 4 | BLE advertising | ⬜ Not started |
| 5 | Mesh routing (TTL, duplicate suppression, relay) | ⬜ Not started |
| 6 | Encryption (X25519, Ed25519, ChaCha20-Poly1305) | ⬜ Not started |
| 7 | Local database (Room entities) | ⬜ Not started |
| 8 | REST bridge (localhost API) | ⬜ Not started |
| 9 | Python SDK (FastAPI client, Typer CLI) | ⬜ Not started |
| 10 | Compose UI (Material 3, dark mode, settings) | ⬜ Not started |
| 11 | Testing (unit, instrumentation, integration; ≥90% coverage) | ⬜ Not started |
| 12 | Documentation (docs/, examples/) | ⬜ Not started |
| 13 | GitHub CI/CD (Actions: build, lint, test, release) | ⬜ Not started |
| 14 | v1.0.0 Release Candidate | ⬜ Not started |

## Milestone Details

### 1. Repository Structure
Directory layout, governance docs, license, CI folder placeholders. No
application logic yet.

### 2. Android Architecture
Gradle Kotlin DSL multi-module setup, Hilt DI graph, MVVM layer
boundaries (UI / ViewModel / Repository / Data source), Coroutines +
Flow conventions, base Compose scaffold with navigation.

### 3–4. BLE Scanning & Advertising
Peer discovery via BLE advertising and scanning, automatic reconnect,
permissions manager (runtime BLE/location permissions per Android version),
foreground service for background operation.

### 5. Mesh Routing
TTL-bounded relay, LRU duplicate/seen-packet suppression, neighbour
quality scoring, routing statistics.

### 6. Encryption
X25519 key exchange, Ed25519 packet signing, ChaCha20-Poly1305 payload
encryption, HKDF key derivation, key rotation support, replay protection.

### 7. Local Database
Room entities: `Peer`, `Identity`, `Packet`, `Message`, `SeenPacket`,
`Statistics`, `Settings`.

### 8. REST Bridge
Localhost-only REST API exposed by the Android app
(`/status`, `/peers`, `/messages`, `/send`, `/statistics`, `/logs`,
`/identity`) for the Python SDK to consume.

### 9. Python SDK
Termux-friendly SDK (FastAPI client side, Typer CLI, SQLite cache,
PyNaCl, Rich, Pydantic, httpx). Commands: `mesh send`, `mesh receive`,
`mesh peers`, `mesh status`, `mesh stats`.

### 10. Compose UI
Material 3 UI, dark mode, chat screens, peer list, statistics dashboard,
settings screen.

### 11. Testing
Unit tests (routing, crypto, database, CLI), instrumentation tests
(BLE, Compose UI), integration tests (REST API ↔ SDK). Target ≥90%
coverage.

### 12. Documentation
Fill out `docs/`, `examples/`, expand `ARCHITECTURE.md`, `API.md`,
`PROTOCOL.md` with final, implementation-accurate detail.

### 13. GitHub CI/CD
GitHub Actions: Android build, Python tests, lint (ktlint/detekt,
black/ruff), formatting checks, release automation.

### 14. v1.0.0 Release Candidate
Final hardening pass, changelog finalization, tagged release, published
artifacts.
