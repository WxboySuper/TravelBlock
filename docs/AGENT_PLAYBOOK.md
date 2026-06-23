# TravelBlock Agent Playbook

## Purpose

This playbook tells coding agents how to work on TravelBlock without turning the project into a baggage carousel of half-finished features.

## Main Rule

Build one phase at a time.

Do not redefine the product. Do not add surprise features. Do not port old React Native code. Rebuild the concepts natively.

## Required Agent Workflow

For every task:

1. Read `ROADMAP.md`.
2. Read the relevant spec file.
3. Work only on the assigned phase.
4. Keep the app compiling.
5. Add or update tests where the phase calls for tests.
6. Update version metadata if the phase has a target version.
7. Update `AGENT_NOTES.md`.
8. Stop when the acceptance criteria are met.

## `AGENT_NOTES.md` Template

Agents should append entries like this:

```markdown
## YYYY-MM-DD - Phase X: Short Name

### Changed
- File or area changed
- File or area changed

### Decisions
- Decision and reason

### Tests
- What was run
- Result

### Follow-ups
- Known issue or next step
```

## Context Order

When working in the new repo, use context in this order:

1. `ROADMAP.md`
2. `PRODUCT_SPEC.md`
3. `UX_SPEC.md`
4. `DATA_SPEC.md`
5. `AGENT_PLAYBOOK.md`
6. `REFERENCE_SUMMARY.md`
7. `OLD_FEATURE_MAP.md`
8. `AIRPORT_ENGINE_NOTES.md`
9. `UI_LESSONS.md`
10. `MIGRATION_BOUNDARIES.md`

Do not read the full old Expo/React Native project unless Alex explicitly gives it for a specific reference task.

## Coding Style Goals

- Simple Compose screens.
- Clear ViewModels.
- Domain logic outside UI.
- Repository layer around persistence.
- DataStore and Room hidden behind repositories.
- No network dependency in v1.
- No authentication.
- No iOS code.

## Build Discipline

After code changes, agents should try to run the relevant build/test commands if the environment supports it.

Preferred checks:

```bash
./gradlew test
./gradlew assembleDebug
```

If the environment cannot run Gradle, the agent must say so in `AGENT_NOTES.md` and avoid claiming the build passed.

## What Agents Should Not Do

Agents must not:

- Add cloud sync.
- Add accounts.
- Add real flight APIs.
- Add real weather APIs.
- Add ads or purchases.
- Add iOS or web targets.
- Replace the product loop with a generic Pomodoro app.
- Make Home mostly a stats dashboard.
- Add a pause button to Cockpit.
- Copy React Native components into Android code.
- Introduce a large dependency just for minor UI polish.

## Preferred Task Shape

Good task:

```text
Implement Phase 2: airport engine. Add models, dataset, distance calculation, reachable destination filtering, route labels, and unit tests. Do not modify UI except as needed to expose temporary debug output.
```

Bad task:

```text
Build the whole app and make it nice.
```

## Review Checklist

Before a phase is accepted:

- Does the app still build?
- Does the phase meet its acceptance criteria?
- Did the agent avoid unrelated features?
- Is domain logic separated from UI?
- Are version values updated?
- Is `AGENT_NOTES.md` updated?
- Can the next agent understand what changed?

## Agent Roles

Use agents like this:

### Architect Agent

Good for project structure, repository design, and phase planning.

### Implementation Agent

Good for adding one feature slice.

### Repair Agent

Good for build errors, test failures, and narrow bugs.

### Review Agent

Good for reading a completed phase and identifying issues before the next phase.

Do not let a single agent own the whole product direction.
