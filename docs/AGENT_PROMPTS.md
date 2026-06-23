# TravelBlock Agent Prompts

Use these prompts one phase at a time. Do not paste the entire file into an agent unless you are using it as context. Give the agent the relevant phase prompt plus the docs it needs.

## Phase 0: Docs and Repo Prep

```text
You are preparing a fresh native Android rebuild of TravelBlock. Do not write app code yet.

Read the docs in this repo and improve them if needed. The app is a native Android Kotlin + Jetpack Compose rebuild of an older Expo/React Native prototype. The old app is reference only.

Make sure the docs clearly explain the product loop: current airport, focus duration, reachable destinations, booking, boarding pass, cockpit focus session, arrival/diversion, logbook, rewards, updated current airport.

Do not add cloud sync, accounts, iOS, web, real flight APIs, real-money purchases, or weather APIs to v1.
```

## Phase 1: Native Android Shell

```text
Implement Phase 1 from ROADMAP.md.

Create a fresh native Android Kotlin Jetpack Compose project for TravelBlock. Implement a Material 3 theme, edge-to-edge layout, bottom navigation, and placeholder screens for Home, Book, Cockpit, Logbook, Store, and Settings.

Set Android package versionName to 0.1.0-shell and versionCode to 100.

Do not implement real business logic yet. Keep the app compiling and runnable. Update AGENT_NOTES.md with changed files, decisions, test/build results, and follow-ups.
```

## Phase 2: Airport Engine

```text
Implement Phase 2 from ROADMAP.md.

Add TravelBlock's core airport and route engine. Add Airport, RouteOption, FlightPlan, FlightStatus, SeatOption, and UserProgress models. Add a local airport dataset large enough to make route availability meaningful, including KOUN, KOKC, KTUL, KDFW, KDAL, KICT, KMCI, KDEN, KSTL, KAUS, KHOU, KIAH, and other useful regional and major airports.

Implement great-circle distance calculation and duration-to-reachable-destinations logic. Add route labels such as Best Match, Short Hop, Long Stretch, Return Home, and Too Far. Add unit tests.

Set versionName to 0.2.0-airport-engine and versionCode to 200. Keep UI changes minimal. Update AGENT_NOTES.md.
```

## Phase 3: Persistence Foundation

```text
Implement Phase 3 from ROADMAP.md.

Add local-first persistence. Use DataStore for preferences and Room for structured data. Persist firstLaunchCompleted, homeAirportCode, currentAirportCode, themePreference, soundEnabled, ambientModeEnabled, and hapticsEnabled in DataStore.

Add Room entities and DAOs for flights, point transactions, and unlocked cosmetics. Add repositories for settings, airports, flights, and rewards. Keep persistence access out of Composables.

Set versionName to 0.3.0-persistence and versionCode to 300. Update AGENT_NOTES.md.
```

## Phase 4: Onboarding and Airport Picker

```text
Implement Phase 4 from ROADMAP.md.

Build first-launch onboarding and airport picker. New users should see a welcome screen explaining that focus sessions become simulated flights, then choose a home airport using search by code, ICAO, city, name, region, or keyword. Persist selected home airport and current airport. Returning users should skip onboarding.

Add an edit-home-airport option in Settings. KOUN should be searchable by KOUN, OUN, Norman, University of Oklahoma, and Westheimer.

Set versionName to 0.4.0-onboarding and versionCode to 400. Update AGENT_NOTES.md.
```

## Phase 5: Home as Departure Board

```text
Implement Phase 5 from ROADMAP.md.

Build the Home screen as a departure board. Show current airport, points, duration chips, available departure routes, compact stats, and a continue/return-home card when relevant. Route cards must show destination, focus duration, distance, reward, and label.

Make route discovery the main content of Home. Do not make this a generic stats dashboard.

Set versionName to 0.5.0-departure-board and versionCode to 500. Update AGENT_NOTES.md.
```

## Phase 6: Booking and Boarding Pass

```text
Implement Phase 6 from ROADMAP.md.

Users can select a route from Home or Book, choose duration and destination, select a simple seat option, review the flight, and generate a boarding pass. Generate a believable flight number, gate, and boarding group.

The boarding pass must include origin, destination, flight number, gate, boarding group, seat, departure time, arrival time, and focus duration. It should launch cockpit mode.

Set versionName to 0.6.0-booking and versionCode to 600. Update AGENT_NOTES.md.
```

## Phase 7: Cockpit Timer

```text
Implement Phase 7 from ROADMAP.md.

Implement active cockpit mode and a resilient focus timer. Active flight truth must be based on startedAt, plannedArrivalAt, durationMinutes, origin, destination, distance, seat, and status. Do not rely on an in-memory countdown as source of truth.

The Cockpit screen should calculate progress from timestamps and show countdown, route progress, miles flown, miles remaining, estimated arrival, and a Divert action. There should be no normal pause button.

Set versionName to 0.7.0-cockpit-timer and versionCode to 700. Update AGENT_NOTES.md.
```

## Phase 8: Arrival and Diversion

```text
Implement Phase 8 from ROADMAP.md.

When a flight reaches plannedArrivalAt, show an arrival screen, save a completed flight, award points, and update currentAirportCode to the destination. When the user diverts, confirm first, calculate partial progress, save a diverted flight, and apply reduced or no reward based on DATA_SPEC.md.

After arrival or diversion, offer Continue Journey, Return Home, Logbook, and Home actions.

Set versionName to 0.8.0-arrival-diversion and versionCode to 800. Update AGENT_NOTES.md.
```

## Phase 9: Logbook

```text
Implement Phase 9 from ROADMAP.md.

Build the Logbook from Room data. Show completed and diverted flights with date, origin, destination, duration, distance, status, and points. Add a flight detail screen and summary stats for total flights, focus time, miles, completed flights, diverted flights, and points earned. Add filters for all, completed, and diverted.

Set versionName to 0.9.0-logbook and versionCode to 900. Update AGENT_NOTES.md.
```

## Phase 10: Rewards and Store

```text
Implement Phase 10 from ROADMAP.md.

Add points balance, points transaction history, and Store screen where users can spend points on cosmetic seat classes, boarding pass styles, and aircraft icons. Purchases must persist through Room and create transactions. There should be no real-money purchases.

Set versionName to 0.10.0-rewards-store and versionCode to 1000. Update AGENT_NOTES.md.
```

## Phase 11: Cabin Polish

```text
Implement Phase 11 from ROADMAP.md.

Polish Settings and cabin controls. Add toggles for sound, ambient mode, haptics, and theme. Add reset data with confirmation. Add basic sound and haptic hooks for departure, arrival, selection, and diversion warning, while respecting settings.

Set versionName to 0.11.0-cabin-polish and versionCode to 1100. Update AGENT_NOTES.md.
```

## Phase 12: UI Refinement

```text
Implement Phase 12 from ROADMAP.md.

Perform a UI refinement pass across TravelBlock. Normal screens should feel like a polished travel/airline app with restrained color, clean route cards, and strong readability. Cockpit may be darker and more immersive, but avoid neon overload.

Improve spacing, typography, empty states, boarding pass design, accessibility, touch targets, and content descriptions.

Set versionName to 0.12.0-ui-refinement and versionCode to 1200. Update AGENT_NOTES.md.
```

## Phase 13: Release Candidate

```text
Implement Phase 13 from ROADMAP.md.

Prepare TravelBlock for a v1.0 release candidate. Add or improve tests for route logic, rewards, flight progress, arrival, and diversion. Create a manual QA checklist. Verify onboarding, booking, cockpit, arrival, diversion, logbook, rewards, settings, and app restart persistence. Update README and known issues.

Generate a release build if the environment supports it. Set versionName to 1.0.0-rc1 and versionCode to 1300. Update AGENT_NOTES.md.
```
