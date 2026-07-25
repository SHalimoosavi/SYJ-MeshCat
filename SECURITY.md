# Security Policy

SYJ-MeshChat handles end-to-end encrypted, peer-relayed messaging over an
unauthenticated wireless medium (BLE). Security issues are taken seriously.

## Supported Versions

Until the first `v1.0.0` release, only the `main` branch is supported for
security fixes.

| Version        | Supported          |
| -------------- | ------------------- |
| `main` (pre-1.0) | ✅                  |
| Tagged releases  | ✅ (once published) |

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Instead, report privately to: **cto@sayanjalinexus.com**

Please include:

- A description of the vulnerability and its potential impact.
- Steps to reproduce, or a proof-of-concept if available.
- Affected component (BLE transport, mesh routing, cryptography, REST
  bridge, Python SDK, etc).

You should receive an acknowledgement within **5 business days**. We will
work with you to understand and validate the issue, develop a fix, and
coordinate disclosure timing.

## Scope of Concern

Particular attention is given to reports involving:

- Key generation, storage, or rotation (X25519 / Ed25519).
- Payload encryption/decryption (ChaCha20-Poly1305) or nonce handling.
- Packet signature verification and replay-attack resistance.
- Routing logic that could allow message spoofing, flooding, or
  denial-of-service across the mesh.
- The local REST bridge (`localhost` API) — e.g. unauthorized access from
  other apps on the same device.

## Cryptography Principles

This project only uses well-established, audited cryptographic primitives
(X25519, Ed25519, ChaCha20-Poly1305, HKDF, SHA-256) via maintained
libraries. No custom cryptographic algorithms are implemented. If you spot
a deviation from this principle, please report it as a vulnerability.
