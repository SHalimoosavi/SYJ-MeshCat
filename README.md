# SYJ-MeshChat

**An Android-native, offline Bluetooth Low Energy mesh messenger.**

SYJ-MeshChat lets nearby Android devices exchange encrypted messages over a
self-forming Bluetooth Low Energy mesh — with no internet, no cellular
network, and no central server. Messages hop from device to device using
TTL-bounded relay routing until they reach their destination or a public
channel subscriber.

A companion Python SDK (designed for Termux) talks to the running Android
app over a local REST bridge, so mesh messaging can be scripted, automated,
or integrated into other tools.

> Status: **Milestone 1 — Repository Structure** (see [ROADMAP.md](ROADMAP.md))
> This project is under active, milestone-by-milestone development.
> No BLE, crypto, or UI code exists yet — see the roadmap for what's next.

---

## Why SYJ-MeshChat?

- **Offline-first.** No internet, no SIM, no Wi-Fi required — just BLE.
- **Store-and-forward mesh.** Messages relay across peers with TTL routing,
  duplicate suppression, and delivery acknowledgements.
- **End-to-end encrypted.** X25519 key exchange, Ed25519 signing,
  ChaCha20-Poly1305 payload encryption. No custom cryptography.
- **Scriptable.** A local REST API + Python SDK/CLI for automation, bots,
  and Termux workflows.
- **Fully open source.** MIT licensed, original architecture, built only on
  public Android APIs.

## Architecture at a Glance

```
┌─────────────────────────────┐        ┌───────────────────────────┐
│   Android App (Kotlin)      │        │   Python SDK (Termux)     │
│  Jetpack Compose · MVVM     │        │  FastAPI client · Typer   │
│  Hilt · Room · Coroutines   │◄──────►│  SQLite cache · PyNaCl    │
│  BLE Advertise/Scan/GATT    │  REST  │  Rich CLI                 │
│  Mesh Router · Crypto Layer │  :  📡  │  httpx + Pydantic         │
└─────────────────────────────┘        └───────────────────────────┘
            │
            ▼
   ┌─────────────────┐
   │  Nearby Peers    │
   │  (BLE Mesh)      │
   └─────────────────┘
```

Full details in [ARCHITECTURE.md](ARCHITECTURE.md),
protocol/packet spec in [PROTOCOL.md](PROTOCOL.md), and REST contract in
[API.md](API.md).

## Repository Layout

```
SYJ-MeshChat/
├── app/                  # Android application module (Kotlin, Compose, MVVM)
│   └── src/
│       ├── main/kotlin
│       ├── test/kotlin          # JVM unit tests
│       └── androidTest/kotlin   # Instrumentation tests
├── sdk/
│   └── python/           # Termux-friendly Python SDK + CLI
│       ├── mesh_sdk/
│       └── tests/
├── docs/                 # Extended documentation
├── examples/             # Usage examples (Android + Python)
├── .github/workflows/    # CI/CD (build, lint, test, release)
├── README.md
├── LICENSE
├── CONTRIBUTING.md
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── CHANGELOG.md
├── ROADMAP.md
├── ARCHITECTURE.md
├── API.md
└── PROTOCOL.md
```

## Getting Started (Termux)

This project is developed and built entirely from Termux on Android. Full
build instructions land in later milestones once the Gradle project exists;
for now, clone the repo to get set up:

```bash
pkg install git -y
git clone https://github.com/SHalimoosavi/SYJ-MeshChat.git
cd SYJ-MeshChat
```

## Roadmap

Development proceeds one milestone at a time — each milestone compiles,
passes tests, and updates the changelog before the next begins. See
[ROADMAP.md](ROADMAP.md) for the full 14-milestone plan.

## Contributing

Contributions are welcome once the core architecture (Milestones 1–8)
lands. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

Please see [SECURITY.md](SECURITY.md) for how to report vulnerabilities —
especially anything related to the cryptography or mesh protocol.

## License

MIT — see [LICENSE](LICENSE).

## Author

Built by **Syed Ali Hasan Moosavi** ([SAYANJALI NEXUS PRIVATE LIMITED](https://github.com/SHalimoosavi)).
