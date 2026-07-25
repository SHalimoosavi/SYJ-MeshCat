# Contributing to SYJ-MeshChat

Thank you for considering a contribution. This project is developed
milestone-by-milestone (see [ROADMAP.md](ROADMAP.md)); please align PRs with
the current milestone in progress rather than jumping ahead to unimplemented
subsystems.

## Ground Rules

- **No proprietary code.** Do not port or adapt code from closed-source or
  incompatible-license projects. All contributions must be original or
  compatibly licensed.
- **No custom cryptography.** Only well-known, audited primitives
  (X25519, Ed25519, ChaCha20-Poly1305, HKDF, SHA-256) via established
  libraries. Do not invent or hand-roll crypto.
- **No placeholder implementations.** Every merged feature must be
  functionally complete, not a stub.
- **Every commit must build.** `./gradlew build` (Android) and
  `pytest` (Python SDK) must both pass before a PR is opened.

## Development Environment

This project is designed to be built entirely from **Termux on Android**,
though any standard Android Studio / JDK 17+ / Python 3.10+ environment
works too.

### Android

```bash
pkg install openjdk-17 gradle git -y
git clone https://github.com/SHalimoosavi/SYJ-MeshChat.git
cd SYJ-MeshChat
./gradlew build
```

### Python SDK

```bash
cd sdk/python
pip install -e .
pytest
```

## Code Style

- **Kotlin:** follow the official Kotlin style guide, enforced via
  `ktlint` and `detekt`. Run `./gradlew ktlintCheck detekt` before
  submitting.
- **Python:** formatted with `black`, linted with `ruff`. Type hints via
  `pydantic` models where applicable.
- Document every public class and function.
- Prefer small, loosely-coupled modules following SOLID principles.

## Commit Messages

Use clear, conventional messages, e.g.:

```
feat(routing): add TTL-based relay with LRU duplicate cache
fix(crypto): correct nonce reuse in ChaCha20-Poly1305 wrapper
docs(protocol): document packet CRC field
```

## Pull Request Process

1. Fork the repo and create a feature branch from `main`.
2. Make your changes, including tests (unit, and instrumentation/CLI tests
   where relevant).
3. Ensure `./gradlew build test` and `pytest` pass locally.
4. Update `CHANGELOG.md` under an "Unreleased" section.
5. Open a PR describing the change, the milestone it relates to, and any
   security-relevant considerations.
6. At least one maintainer review is required before merge.

## Reporting Issues

Use GitHub Issues for bugs and feature requests. For security
vulnerabilities, follow [SECURITY.md](SECURITY.md) instead of filing a
public issue.

## Code of Conduct

By participating, you agree to uphold the
[Code of Conduct](CODE_OF_CONDUCT.md).
