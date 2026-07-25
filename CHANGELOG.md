# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/) once
`v1.0.0` is released. Until then, milestones are tracked explicitly below.

## [Unreleased]

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
