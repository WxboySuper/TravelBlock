# TravelBlock Start-Fresh Strategy

## Recommendation

Start fresh in a new native Android repo.

Do not build the Kotlin/Compose version inside the old Expo/React Native folder. Keep the old project as a reference archive.

## Why Start Fresh

The old app proved important things:

- The flight-focus concept works.
- Home airport and current airport are important.
- Destination discovery is the core mechanic.
- Booking, boarding pass, cockpit, and diversion form a strong product loop.
- Expo/React Native created enough friction that continuing in that stack is not ideal for Android-only development.

But the old codebase is tied to a stack that the new app should not inherit.

Fresh rebuild means:

- Cleaner Android architecture.
- Less package clutter.
- No Expo/EAS baggage.
- Better Android-native UI.
- Agents can work phase-by-phase without migration confusion.

## Folder Strategy

Create a new folder:

```text
TravelBlockAndroid/
```

Copy these docs into it first. Do not copy old source code.

Keep the old project elsewhere:

```text
TravelBlockLegacyExpo/
```

or leave it in its existing repo and treat it as read-only reference.

## Should the Old Folder Be Given as Agent Context?

Default answer: no.

The old folder will clutter context and may encourage agents to port the wrong patterns. It may make agents think the goal is a migration instead of a native rebuild.

Use the old folder only for targeted reference tasks:

- Summarize existing behavior.
- Extract airport dataset ideas.
- Compare current feature parity.
- Document an old flow before rebuilding it.

Do not let an implementation agent read the whole old project unless the task explicitly requires it.

## Best Context Strategy

Use docs as the bridge.

Old app to docs, then docs to new app.

Workflow:

1. Use an agent to scan the old project and create reference docs.
2. Put the reference docs in the new repo.
3. Build the new native Android app from the docs.
4. Only open old source files when a specific detail is missing.

## Recommended New Repo Contents Before Code

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

## Development Sequence

### Step 1: Create the new folder

Create `TravelBlockAndroid` somewhere separate from the old project.

### Step 2: Add docs

Copy this docs pack into the new folder.

### Step 3: Open Android Studio

Use Android Studio to create a new Empty Activity project inside that folder, or let an agent create it if your tooling supports project generation.

Recommended project type:

- Empty Activity
- Kotlin
- Jetpack Compose
- Minimum SDK: reasonable modern Android target, such as 26 or later
- Package name: `com.travelblock.app` or similar

### Step 4: Run the empty app

Before agents add features, make sure the empty app runs on emulator or phone.

### Step 5: Run Phase 1 agent task

Ask the agent to create the shell, navigation, theme, placeholder screens, and version metadata.

### Step 6: Build incrementally

Do not skip phases. The early phases are the runway.

## If Android Studio Feels Confusing

That is normal. The first goal is not mastery. The first goal is:

- Open project.
- Let Gradle sync.
- Click Run.
- See app on emulator or phone.

Everything else can be learned as the project needs it.

## Agentic Development Plan

Use agents in small phases:

1. Shell agent
2. Airport engine agent
3. Persistence agent
4. Onboarding agent
5. Home departure board agent
6. Booking agent
7. Cockpit timer agent
8. Arrival/diversion agent
9. Logbook agent
10. Rewards/store agent
11. Polish agent
12. Release candidate agent

After each phase, manually test the app. Even a five-minute review is better than letting agents stack broken assumptions ten layers deep.

## Branch Strategy

Simple option:

```text
main
phase/01-shell
phase/02-airport-engine
phase/03-persistence
```

Merge a phase only when it builds and its acceptance criteria pass.

## What to Do If a Phase Goes Bad

Do not keep prompting the same agent forever.

Instead:

1. Save the error output.
2. Ask a repair agent to fix only the error.
3. If the phase is messy, revert the branch and retry with a narrower prompt.

## The Golden Rule

The old app is a teacher, not a landlord.

Learn from it, then move into the clean new house.
