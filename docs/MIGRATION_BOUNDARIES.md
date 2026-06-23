# Migration Boundaries

## Purpose

This file prevents the native Android rebuild from becoming a messy port.

The old Expo/React Native app is useful, but the new app should be a native rebuild.

## Hard Rules

1. Do not copy React Native components.
2. Do not copy Expo-specific patterns.
3. Do not recreate EAS workflows.
4. Do not preserve old folder structure.
5. Do not use the old app as the active codebase.
6. Do not add cross-platform complexity for v1.
7. Do not let agents migrate screens one-by-one without redesigning the architecture.

## What Can Be Preserved

Concepts:

- Flight-focus loop
- Home airport
- Current airport
- Duration-based routes
- Booking
- Boarding pass
- Cockpit
- Diversion
- Logbook
- Rewards

Rules:

- No normal pause button
- Route availability based on duration
- Completed flight updates current airport
- Diversion saves partial progress

Data ideas:

- Airport fields
- Flight fields
- Points transaction fields
- Seat and cosmetic concepts

## What Must Be Reimplemented

- UI screens in Jetpack Compose
- Navigation
- Persistence
- Timer logic
- Airport engine
- Rewards service
- Settings
- Audio/haptics
- Build system

## When to Look at Old Code

Only look at old code when a specific question exists, such as:

- What did the old airport dataset include?
- How did old route availability behave?
- What fields existed in old flight state?
- What old UI flow should be preserved conceptually?

Do not hand the old codebase to a general implementation agent unless the task is explicitly to summarize it.

## Migration Language to Avoid

Avoid prompts like:

```text
Port the app to Android.
```

Use prompts like:

```text
Rebuild the TravelBlock booking flow natively in Kotlin and Jetpack Compose using PRODUCT_SPEC.md and UX_SPEC.md. Use the old app only as product reference, not as source code to copy.
```

## Architecture Boundary

React Native concepts should not leak into the native app.

Do not create:

- Component structures that mimic React files unnecessarily
- JavaScript-style state stores
- Expo config equivalents
- Web-first styling abstractions

Use Android patterns:

- ViewModels
- Kotlin data classes
- Repositories
- Room
- DataStore
- Compose screens and composables
- Material 3 theming

## Final Reminder

The old app is a teacher, not a dependency.
