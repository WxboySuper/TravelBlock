# TravelBlock Android Rebuild Docs Pack

This folder is the planning and agent context pack for a fresh native Android rebuild of TravelBlock.

TravelBlock is an Android-first airline/travel-themed focus app where focus sessions become simulated flights. The old Expo/React Native version should be treated as a prototype and reference artifact, not as the new codebase.

## Recommended use

Create a new repo or folder named `TravelBlockAndroid`, then copy these files into it before any app code is written.

Suggested starting folder:

```text
TravelBlockAndroid/
  README.md
  ROADMAP.md
  PRODUCT_SPEC.md
  UX_SPEC.md
  DATA_SPEC.md
  AGENT_PLAYBOOK.md
  START_FRESH_STRATEGY.md
  AGENT_PROMPTS.md
  REFERENCE_SUMMARY.md
  OLD_FEATURE_MAP.md
  AIRPORT_ENGINE_NOTES.md
  UI_LESSONS.md
  MIGRATION_BOUNDARIES.md
  WINDOWS_ANDROID_SETUP.md
```

## Source basis

These docs were built from:

- The public TravelBlock README and ROADMAP.
- The current app direction discussed by Alex.
- The current UI screenshot showing the existing home screen.
- The planned native Android direction: Kotlin, Jetpack Compose, Room, DataStore, local-first v1.0.

The docs intentionally avoid copying React Native code. They preserve product lessons and implementation intent so coding agents can rebuild the app natively without dragging the old stack into the new project.

## First move

Do not code immediately. Read `START_FRESH_STRATEGY.md`, then run Phase 0 from `ROADMAP.md` and use `AGENT_PROMPTS.md` to create the first Android shell.
