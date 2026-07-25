# Architecture

> This document describes the target architecture. As of Milestone 1, none
> of the components below are implemented yet — this is the design that
> subsequent milestones will build toward.

## Overview

SYJ-MeshChat has two cooperating parts:

1. **Android app** — the actual mesh node. Owns the BLE radio, identity
   keys, routing table, and local database. Runs as a foreground service
   so the mesh stays alive in the background.
2. **Python SDK** — a Termux-friendly client that talks to the Android
   app over a `localhost` REST API. It never touches BLE directly.

```
┌────────────────────────────────────────────────────────────┐
│                        Android App                         │
│                                                              │
│  ┌───────────────┐   ┌────────────────┐   ┌──────────────┐ │
│  │ Compose UI     │──▶│ ViewModels      │──▶│ Repositories │ │
│  │ (Material 3)   │   │ (MVVM, Flow)    │   │ (single      │ │
│  └───────────────┘   └────────────────┘   │  source of    │ │
│                                             │  truth)       │ │
│                                             └──────┬───────┘ │
│                                                     │         │
│         ┌───────────────────────────────────────────┐        │
│         │                Data Layer                  │        │
│         │  ┌───────────┐ ┌───────────┐ ┌───────────┐ │        │
│         │  │ Room DB    │ │ Mesh      │ │ Crypto     │ │        │
│         │  │ (entities) │ │ Router    │ │ Engine     │ │        │
│         │  └───────────┘ └───────────┘ └───────────┘ │        │
│         │  ┌───────────┐ ┌───────────────────────┐   │        │
│         │  │ BLE        │ │ REST Bridge            │   │        │
│         │  │ Transport  │ │ (localhost server)      │  │        │
│         │  └───────────┘ └───────────┬───────────┘   │        │
│         └──────────────────────────────┼──────────────┘        │
│                          Foreground Service                    │
└─────────────────────────────────────────┼──────────────────────┘
                                           │ REST (localhost)
                                           ▼
                              ┌───────────────────────┐
                              │   Python SDK / CLI     │
                              │ (Termux) FastAPI client│
                              │ Typer · Rich · PyNaCl  │
                              └───────────────────────┘
```

## Android Layers (MVVM + Repository)

- **UI (Compose):** stateless composables driven by `StateFlow` from
  ViewModels. No business logic in the UI layer.
- **ViewModel:** owns UI state, exposes `Flow`/`StateFlow`, calls into
  repositories. Scoped with Hilt.
- **Repository:** single source of truth per domain (Messages, Peers,
  Identity, Statistics). Mediates between Room, the mesh router, and the
  crypto engine.
- **Data sources:**
  - `Room` — persistent storage for all entities.
  - `BleTransport` — advertising/scanning/GATT connection management.
  - `MeshRouter` — TTL routing, duplicate suppression, relay decisions.
  - `CryptoEngine` — key management, signing, encryption/decryption.
  - `RestBridgeServer` — exposes the localhost REST API for the SDK.

## Dependency Injection

Hilt provides the DI graph. Modules are split by concern
(`BleModule`, `CryptoModule`, `DatabaseModule`, `NetworkModule`,
`RepositoryModule`) to keep components loosely coupled and testable in
isolation (fakes/mocks swapped in for instrumentation and unit tests).

## Concurrency Model

- **Coroutines** for all async work (BLE callbacks bridged via
  `callbackFlow`/`suspendCancellableCoroutine`).
- **Flow** for reactive state: peer list, message stream, connection
  status, statistics.
- **WorkManager** for deferred/periodic background tasks (e.g. store-and-
  forward retry, log export, battery-aware scan throttling).
- **Foreground Service** keeps BLE scanning/advertising alive with a
  persistent notification, per Android background execution limits.

## Mesh Networking Design

See [PROTOCOL.md](PROTOCOL.md) for the wire format. At a high level:

- Every node has a persistent `Identity` (X25519 + Ed25519 keypairs).
- Packets carry TTL and hop count; nodes decrement TTL and relay only
  while `TTL > 0`.
- A bounded LRU `SeenPacket` cache prevents re-relaying duplicates.
- Private messages are encrypted point-to-point; public channel messages
  are signed but broadcast in the clear to channel subscribers.
- Neighbour quality scores inform relay/route preference over time.

## REST Bridge

A lightweight embedded HTTP server (bound to `127.0.0.1` only) exposes the
mesh to local processes — primarily the Python SDK. See
[API.md](API.md) for the full contract. The bridge is intentionally
loopback-only; it is not a remote/network API.

## Python SDK

The SDK is a thin client: it never implements BLE logic. It talks to the
Android app's REST bridge, using `PyNaCl` only for any local
pre-processing needs (e.g. validating key material shown to the user),
`Pydantic` for request/response models, `httpx` for transport, `Typer` for
the CLI, and `Rich` for terminal output.

## Security Boundaries

- Private keys never leave the Android Keystore-backed storage; the REST
  bridge never exposes raw private key material.
- All packets are signed (Ed25519) and verified before being accepted or
  relayed.
- Replay protection via the `SeenPacket` cache plus timestamp/message-ID
  validation.

## Testing Strategy

- **Unit tests** (JVM, `app/src/test`): routing logic, crypto wrappers,
  repository logic, CLI commands.
- **Instrumentation tests** (`app/src/androidTest`): Room DAOs, BLE
  integration (where emulable), Compose UI.
- **Integration tests**: REST bridge ↔ Python SDK round-trip.
- Coverage target: **≥90%** across both Kotlin and Python codebases.
