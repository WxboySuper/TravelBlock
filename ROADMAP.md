# TravelBlock Agent-First Roadmap

## Project Position

TravelBlock v1.0 is a fresh native Android rebuild of the existing TravelBlock idea.

The old Expo/React Native app is a prototype and research artifact. It proved the product loop, surfaced UX lessons, and contains useful implementation ideas, but it should not be treated as the new codebase. The rebuild should be developed in small agent-sized phases with clear acceptance criteria.

## Product North Star

TravelBlock is an airline/travel-themed focus app.

The app turns a focus session into a simulated flight:

1. The user is at a current airport.
2. The user chooses a focus duration.
3. The app shows reachable destinations from that airport.
4. The user books a focus flight.
5. The user enters cockpit mode and focuses while the flight progresses.
6. The user lands, diverts, or abandons.
7. The result is saved to the logbook.
8. Rewards and current-airport state update.
9. The next available routes change based on where the user is.

The dashboard is not a generic stats page. It is a departure board.

The timer is not the product. The flight loop is the product.

## Recommended Stack

- Native Android
- Kotlin
- Jetpack Compose
- Material 3
- Android architecture components
- Room for structured local data
- DataStore for preferences
- Gradle Kotlin DSL if practical

## Version Code Policy

Each coding phase should update Android package version metadata.

Use:

- `versionName`: readable phase marker, such as `0.1.0-shell`
- `versionCode`: monotonically increasing integer, such as `100`

Version codes should never decrease.

## Agent Operating Rules

Every agent task should follow these rules:

1. Work only on the assigned phase.
2. Keep the app compiling after every task.
3. Do not add unrelated features.
4. Prefer simple, readable implementation over clever abstractions.
5. Update `AGENT_NOTES.md` after each phase.
6. If stuck, document the blocker instead of rewriting large areas blindly.
7. Do not introduce network dependencies unless the current phase explicitly asks for them.
8. Do not add authentication, cloud sync, monetization, or iOS work in v1.
9. Use fake/local data where needed until a later phase replaces it.
10. Preserve the product loop: airport to route to booking to flight to landing/diversion to logbook.

## Target Repository Structure

```text
TravelBlockAndroid/
  README.md
  ROADMAP.md
  AGENT_NOTES.md
  PRODUCT_SPEC.md
  UX_SPEC.md
  DATA_SPEC.md
  AGENT_PLAYBOOK.md
  app/
    build.gradle.kts
    src/main/java/com/travelblock/app/
      MainActivity.kt
      data/
        airport/
        database/
        flight/
        rewards/
        settings/
      domain/
        airport/
        booking/
        flight/
        rewards/
      ui/
        components/
        navigation/
        screens/
          onboarding/
          home/
          booking/
          cockpit/
          arrival/
          logbook/
          store/
          settings/
        theme/
      util/
```

## Phase 0: Planning and Reference Pack

Target package version: not applicable

Goal: Create planning docs before Android code.

Deliverables:

- `ROADMAP.md`
- `PRODUCT_SPEC.md`
- `UX_SPEC.md`
- `DATA_SPEC.md`
- `AGENT_PLAYBOOK.md`
- `START_FRESH_STRATEGY.md`
- `REFERENCE_SUMMARY.md`
- `OLD_FEATURE_MAP.md`
- `AIRPORT_ENGINE_NOTES.md`
- `UI_LESSONS.md`
- `MIGRATION_BOUNDARIES.md`

Acceptance criteria:

- The old project is summarized into docs, not copied into the new repo.
- Agents can understand the product without reading the old source code.
- The v1 scope is separated from future ideas.
- The rebuild rules are clear.

## Phase 1: Native Android Shell

Target package version:

- `versionName`: `0.1.0-shell`
- `versionCode`: `100`

Goal: Create a clean native Android project that runs.

Deliverables:

- Kotlin + Jetpack Compose app
- Material 3 theme
- Edge-to-edge layout
- Basic navigation shell
- Placeholder screens: Home, Book, Cockpit, Logbook, Store, Settings
- Bottom navigation
- `AGENT_NOTES.md`

Acceptance criteria:

- App builds successfully.
- App launches on Android emulator or physical phone.
- Bottom navigation works.
- No real business logic yet.

## Phase 2: Core Models and Airport Engine

Target package version:

- `versionName`: `0.2.0-airport-engine`
- `versionCode`: `200`

Goal: Build the domain foundation.

Deliverables:

- Models: Airport, RouteOption, FlightPlan, FlightStatus, SeatOption, UserProgress
- Local airport dataset with meaningful regional and major-airport coverage
- Airport search by code, ICAO, name, city, and region
- Great-circle distance calculation
- Duration-to-reachable-distance logic
- Route labels: Best Match, Short Hop, Long Stretch, Return Home, Too Far
- Unit tests for distance and filtering

Acceptance criteria:

- KOUN produces useful route options.
- Short durations show nearby airports.
- Longer durations unlock farther airports.
- Tests pass.

## Phase 3: Persistence Foundation

Target package version:

- `versionName`: `0.3.0-persistence`
- `versionCode`: `300`

Goal: Make the app local-first.

Deliverables:

- DataStore preferences for onboarding, home airport, current airport, theme, sound, ambient mode, haptics
- Room database for flights, point transactions, and unlocked cosmetics
- Repository layer: SettingsRepository, AirportRepository, FlightRepository, RewardsRepository

Acceptance criteria:

- Home airport persists after restart.
- Current airport persists after restart.
- Flights and points transactions can be inserted and read.
- Persistence stays out of Composables.

## Phase 4: Onboarding and Airport Picker

Target package version:

- `versionName`: `0.4.0-onboarding`
- `versionCode`: `400`

Goal: Create the first-launch setup.

Deliverables:

- Welcome screen
- TravelBlock explanation
- Airport picker screen
- Airport search/filter
- Home airport confirmation
- Settings option to edit home airport

Acceptance criteria:

- New users must select a home airport before main app.
- Returning users skip onboarding.
- KOUN is searchable by KOUN, OUN, Norman, University of Oklahoma, and Westheimer.

## Phase 5: Home as Departure Board

Target package version:

- `versionName`: `0.5.0-departure-board`
- `versionCode`: `500`

Goal: Make Home route-first.

Deliverables:

- Current airport card
- Points pill
- Duration chips
- Available departures
- Route cards with destination, focus duration, distance, reward, and label
- Compact stats row
- Continue Journey / Return Home card when relevant

Acceptance criteria:

- The user sees routes before tapping deeper into booking.
- Home answers: where am I, where can I fly, and how long will it take?
- Stats are secondary.
- UI feels like a travel app, not a generic habit tracker.

## Phase 6: Booking Flow and Boarding Pass

Target package version:

- `versionName`: `0.6.0-booking`
- `versionCode`: `600`

Goal: Turn routes into airline-style bookings.

Deliverables:

- Book screen with origin and duration selector
- Reachable airports list
- Route detail state
- Seat selection MVP
- Flight number generation
- Gate and boarding group generation
- Booking review
- Boarding pass screen

Acceptance criteria:

- User can select a route from Home or Book.
- User can select a seat.
- User can generate a boarding pass.
- Boarding pass includes origin, destination, flight number, gate, group, seat, departure time, arrival time, and focus duration.
- Booking can launch cockpit mode.

## Phase 7: Active Cockpit and Resilient Timer

Target package version:

- `versionName`: `0.7.0-cockpit-timer`
- `versionCode`: `700`

Goal: Implement the focus session as a real-time flight that survives restarts.

Deliverables:

- Active flight persistence
- Cockpit screen
- Large countdown
- Route progress
- Miles flown and remaining
- Estimated arrival
- No pause button
- Divert action
- Timer based on timestamps: startedAt, plannedArrivalAt, durationMinutes, status

Acceptance criteria:

- Timer progress is calculated from wall-clock time.
- Closing and reopening the app preserves correct progress.
- If arrival time has passed, the app can show arrival-ready state.
- Cockpit is visually calmer and darker than normal screens.

## Phase 8: Arrival, Diversion, and Current Airport State

Target package version:

- `versionName`: `0.8.0-arrival-diversion`
- `versionCode`: `800`

Goal: Complete the flight loop.

Deliverables:

- Arrival screen
- Completed flight save
- Points award
- Current airport updates to destination on completion
- Diversion confirmation
- Diverted flight save
- Partial progress calculation
- Diversion reward rules
- Post-flight choices: Continue Journey, Return Home, Go to Logbook, Go Home

Acceptance criteria:

- Completing a flight saves a completed logbook entry.
- Diverting saves a diverted logbook entry.
- Rewards apply correctly.
- Current airport updates correctly after completion.

## Phase 9: Logbook and Flight Details

Target package version:

- `versionName`: `0.9.0-logbook`
- `versionCode`: `900`

Goal: Make completed work visible and satisfying.

Deliverables:

- Logbook list
- Flight detail screen
- Completed/diverted labels
- Summary stats: total flights, focus time, miles, completed flights, diverted flights, points earned
- Filters: all, completed, diverted

Acceptance criteria:

- Flights persist and display after restart.
- Details are readable and useful.
- Empty state explains what to do next.
- Summary stats match saved data.

## Phase 10: Rewards, Store, and Economy

Target package version:

- `versionName`: `0.10.0-rewards-store`
- `versionCode`: `1000`

Goal: Add a simple, non-predatory reward loop.

Deliverables:

- Points balance
- Points transaction history
- Store screen
- Unlockable cosmetics: seat classes, boarding pass styles, aircraft icons
- Purchase flow using points only
- Equipped cosmetic setting

Acceptance criteria:

- Completed flights earn points.
- Purchases subtract points and are recorded.
- Unlocked cosmetics persist.
- No real money purchases.

## Phase 11: Settings, Audio, Haptics, and Cabin Polish

Target package version:

- `versionName`: `0.11.0-cabin-polish`
- `versionCode`: `1100`

Goal: Make the app feel complete without bloating it.

Deliverables:

- Settings screen polish
- Sound toggles
- Ambient mode toggle
- Haptics toggle
- Theme preference
- Reset data action with confirmation
- Sound/haptic hooks: departure, arrival, selection, diversion warning

Acceptance criteria:

- Settings persist.
- Audio/haptic settings are respected.
- Reset data requires confirmation.
- App remains usable with all sounds disabled.

## Phase 12: UI Refinement and Travel-Themed Design Pass

Target package version:

- `versionName`: `0.12.0-ui-refinement`
- `versionCode`: `1200`

Goal: Make the app feel like a real Android travel app.

Deliverables:

- Visual pass across all screens
- Better spacing and typography
- Better empty states
- Better route cards
- Better boarding pass
- Darker cockpit-only style
- Light/dark theme cleanup
- Accessibility review: contrast, touch targets, content descriptions, readable text sizes

Acceptance criteria:

- Normal screens feel like a clean travel/airline app, not a neon cockpit.
- Cockpit feels special, calmer, and more immersive.
- App is readable on Alex's Android phone.
- No major visual inconsistencies remain.

## Phase 13: Testing and Release Candidate

Target package version:

- `versionName`: `1.0.0-rc1`
- `versionCode`: `1300`

Goal: Prepare a stable v1.0 release candidate.

Deliverables:

- Manual QA checklist
- Unit tests for domain logic
- Persistence tests where practical
- Navigation smoke tests where practical
- Debug build tested on physical Android device
- Release build generated
- Known issues list
- Screenshots
- README updated

Acceptance criteria:

- New user can complete onboarding, book a flight, complete it, earn points, and see it in logbook.
- Returning user sees persisted state.
- Active flight survives app restart.
- Diversion works.
- No obvious crash paths remain in normal flow.

## v1.0 Release

Target package version:

- `versionName`: `1.0.0`
- `versionCode`: `1400`

Goal: Ship the first complete Android-first TravelBlock release.

Acceptance criteria:

- All v1.0 release candidate issues are either fixed or documented.
- README explains the app clearly.
- Version metadata is finalized.
- The app can be installed on Alex's Android phone.
- The app delivers the complete core loop.

## Out of Scope for v1

Do not add:

- Accounts
- Cloud sync
- iOS
- Web app
- Public social sharing
- Real flight APIs
- Real-money purchases
- Complex aircraft simulation
- Weather API dependency
- Push notification system unless the core loop is already stable
