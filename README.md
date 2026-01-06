# TravelBlock

Expo + React Native mobile app (Android-first). TravelBlock is an Expo Router app that provides airport-related utilities and a small demo UI. This repository is configured for development with EAS workflows and an Expo dev client.

**Status**: Android-first; iOS simulator/device builds were available but this project is currently focused on Android testing and development.

---

## Quick start

### Prerequisites

- Node.js (LTS)
- npm
- npx (bundled with npm)

### Install

```bash
npm install
```

### Start Metro (development server)

```bash
npm run start
# or to open directly on an Android device/emulator
npm run android
```

### Create development builds (EAS)

```bash
# Creates development builds via the preconfigured workflow
npm run development-builds
```

### Notes about development builds

- The project uses EAS Workflows defined in `.eas/workflows/create-development-builds.yml`.
- This workflow was adjusted to only queue Android builds (see [.eas/workflows/create-development-builds.yml](.eas/workflows/create-development-builds.yml)).
- EAS will snapshot the repository at build submission time — local edits after submission do not affect queued builds.
- Keystore generation for Android is interactive by default; run the EAS credential flow locally the first time so remote builds don't fail in non-interactive CI.

### Credentials / common EAS commands

```bash
# Login to Expo/EAS
npx eas-cli@latest login
# Inspect builds
npx eas-cli@latest build:list
npx eas-cli@latest build:view <BUILD_ID>
# Manage credentials locally
npx eas-cli@latest credentials
```

### Testing & linting

```bash
npm test
npm run test:expo
npm run test:coverage
npm run lint
```

### Reset project (create a fresh app folder)

```bash
npm run reset-project
```

### Project structure (high level)

- `app/` — Expo Router entry points and screens
- `components/` — React components and UI primitives
- `services/` — business logic and data services
- `expo-sqlite/` — local kv-store wrapper
- `eas.json` — EAS build profiles
- `.eas/workflows/` — EAS workflow definitions

### Contributing

- This is a personal / hobby project. Pull requests welcome for bug fixes and small improvements. Keep TypeScript strict and follow existing patterns.

### License

- MIT
