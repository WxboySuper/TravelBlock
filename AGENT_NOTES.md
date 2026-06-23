# AGENT_NOTES

## 2026-05-19 - Phase 1: Native Android Shell

### Changed
- Enabled the app as a Kotlin Android Compose project using the existing AGP 9 Android plugin support.
- Added a single-activity Compose entry point in `MainActivity`.
- Added Material 3 TravelBlock theme files with restrained blues, soft neutrals, and travel-app surfaces.
- Added bottom navigation for Home, Book, Cockpit, Logbook, Store, and Settings.
- Added placeholder screens for each tab.
- Built a polished static Home mock with TravelBlock header, `0 pts`, KOUN airport card, duration chips, route cards, and a booking CTA.
- Added requested package structure placeholders for data, data.local, data.repository, domain, domain.model, and domain.engine.
- Updated version metadata to `versionName = "0.1.0-shell"` and `versionCode = 100`.

### Files Created or Edited
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/java/com/travelblock/app/MainActivity.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/theme/Color.kt`
- `app/src/main/java/com/travelblock/app/ui/theme/Theme.kt`
- `app/src/main/java/com/travelblock/app/ui/theme/Type.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockDestination.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockBottomBar.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/common/PlaceholderScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsScreen.kt`
- `app/src/main/java/com/travelblock/app/data/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/data/local/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/data/repository/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/domain/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/domain/model/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/PackageInfo.kt`
- `AGENT_NOTES.md`

### Build and Run
- Build/check command used:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug
```

- Result: `BUILD SUCCESSFUL`
- To run from Android Studio, open the project, select the `app` configuration, and run it on an emulator or device.

### Issues or Assumptions
- `JAVA_HOME` was not set in the shell, so the build command used Android Studio's bundled JBR.
- No real airport parsing, Room, DataStore, logbook persistence, active timer, rewards, or booking logic was added.
- No React Native, Expo, Flutter, or web tooling was introduced.

### Phase 2 Next
- Add domain models for airports, routes, flight plans, seat options, and user progress.
- Add the airport search and route engine using `data/airports.json`.
- Add distance and duration filtering tests, with KOUN as a known useful origin.

## 2026-05-19 - Phase 2A: Airport Domain Engine

### Changed
- Added Phase 2A domain models for `Airport`, `FlightRoute`, `FocusDurationOption`, and `RouteAvailabilityResult`.
- Added a pure Kotlin route engine with great-circle distance, focus-duration target distance, reachable-airport filtering, origin exclusion, best-match sorting, and safe empty results.
- Added centralized route tuning constants in `RouteEngineConfig`.
- Added `AirportRepository` plus `DefaultAirportRepository` for code lookup and reachable route queries.
- Added `AirportJsonParser` and `AirportAssetDataSource` for loading the bundled airport dataset inside the Android app.
- Added `AirportEngineSmokeCheck` so a debug/helper path can request KOUN reachable routes for 15, 25, 45, and 60 minutes without adding production UI.
- Added the airport dataset to `app/src/main/assets/airports.json` so Android runtime code can load it through `AssetManager`.
- Added `kotlinx-serialization-json` for JSON parsing only; no persistence or booking flow was added.

### Files Created or Edited
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/assets/airports.json`
- `app/src/main/java/com/travelblock/app/data/local/AirportAssetDataSource.kt`
- `app/src/main/java/com/travelblock/app/data/local/AirportJsonParser.kt`
- `app/src/main/java/com/travelblock/app/data/repository/AirportRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/DefaultAirportRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/PackageInfo.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/AirportEngineSmokeCheck.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/AirportRouteEngine.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/RouteEngineConfig.kt`
- `app/src/main/java/com/travelblock/app/domain/model/Airport.kt`
- `app/src/main/java/com/travelblock/app/domain/model/FlightRoute.kt`
- `app/src/main/java/com/travelblock/app/domain/model/FocusDurationOption.kt`
- `app/src/main/java/com/travelblock/app/domain/model/RouteAvailabilityResult.kt`
- `app/src/main/java/com/travelblock/app/domain/model/PackageInfo.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/AirportRouteEngineTest.kt`
- `AGENT_NOTES.md`

### Airport Data Loading
- The original source dataset remains at `data/airports.json`.
- A runtime copy lives at `app/src/main/assets/airports.json`.
- `AirportAssetDataSource` opens `airports.json` from Android assets and passes the text to `AirportJsonParser`.
- Local unit tests parse the source `data/airports.json` directly so routing behavior is tested against the real dataset without Android instrumentation.

### Tests
- Added local JVM tests for:
  - KOUN to KOKC great-circle distance.
  - Origin airport exclusion from reachable destinations.
  - Reachable route set changing as duration increases.
  - Invalid airport code returning an empty result.
  - KOUN lookup by IATA code `OUN`.

### Commands Run
- Baseline before changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

- After changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug
```

- Result: `BUILD SUCCESSFUL`

### Known Issues or Next Recommended Task
- `JAVA_HOME` is still not set globally in this shell, so commands used Android Studio's bundled JBR.
- The route engine currently treats very short valid routes as `ShortHop` instead of applying the spec's suggested lower distance multiplier; this keeps 15-minute KOUN route discovery useful and can be tuned after real UX review.
- Phase 2B should add airport search behavior and broader model coverage from the full Phase 2 roadmap, then Phase 5 can wire real route previews into Home.

## 2026-05-19 - Route-First Home Screen

### Changed
- Replaced the static Home placeholder with a route-first Home screen backed by the airport/domain engine.
- Added `HomeViewModel` with injected `AirportRepository` and UI state for KOUN, points, selected duration, available durations, routes, selection, loading, and error/empty messaging.
- Wired the runtime Home screen to `AirportAssetDataSource` and `DefaultAirportRepository`, using `app/src/main/assets/airports.json`.
- Added duration chip behavior for 15, 25, 45, and 60 minutes.
- Added route-card selection and a primary button that navigates to the existing Book tab as a placeholder handoff.
- Kept persistence, Room, DataStore, and full booking flow out of scope.

### Screens and Components Created or Edited
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModelFactory.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/home/HomeViewModelTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/home/HomeScreenTest.kt`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `AGENT_NOTES.md`

### Manual Home Screen Test
- Open the app on an emulator or physical device.
- Confirm Home opens to `TravelBlock`, a greeting, `0 pts`, and KOUN airport details.
- Tap `15m`, `25m`, `45m`, and `60m`; the departure list should update from real KOUN reachability data.
- Tap a route card; the selected card should show the blue selected treatment and the button should change to `Book <CODE> Flight`.
- Tap the button; it should navigate to the existing Book placeholder tab.

### Commands Run
- Baseline before changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

- After changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no emulator or device attached.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: no local AVDs listed.

### Tests Added
- `HomeViewModelTest`
  - Confirms default origin is KOUN.
  - Confirms changing duration updates route state and selection.
  - Confirms selected route state is tracked.
- `HomeScreenTest`
  - Compose instrumentation test compiles and covers TravelBlock text, duration tapping, and visible KOUN route content.
  - Not executed with `connectedDebugAndroidTest` because no device/emulator/AVD was available.

### UI Compromises
- Home uses synchronous asset-backed repository construction for now; this is acceptable for the current local-only foundation but should move behind a small app container or async loading path before heavier UI work.
- Estimated points use the simple v1 reward formula as display-only text and do not persist anything.
- The Book CTA navigates to the existing Book placeholder instead of starting a booking flow.

### Next Recommended Task
- Add a small app-level dependency container so asset loading/repositories are not created directly from Composables.
- Then build the Phase 2B airport search/model completion or move into Phase 5 wiring once the domain scope is accepted.

## 2026-05-19 - Booking Flow Skeleton

### Summary
- Added the first real booking flow skeleton: selected Home route to Book review, seat selection, and boarding pass preview.
- Kept the flow in memory only. No Room, DataStore, active timer, logbook write, or rewards spending was added.
- `Start Focus Flight` navigates to the existing Cockpit placeholder, preserving the boundary that the real cockpit timer is a later phase.

### Files Changed
- `app/src/main/java/com/travelblock/app/domain/model/DraftFlightBooking.kt`
- `app/src/main/java/com/travelblock/app/domain/model/SeatOption.kt`
- `app/src/main/java/com/travelblock/app/domain/model/TicketClass.kt`
- `app/src/main/java/com/travelblock/app/domain/model/BoardingPass.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModelFactory.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/book/BookingViewModelTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `AGENT_NOTES.md`

### State Flow from Home to Book
- `HomeViewModel` keeps the selected reachable `FlightRoute` and converts it to a `DraftFlightBooking`.
- `HomeScreen` passes that draft through `onBookSelectedRoute`.
- `TravelBlockApp` stores the draft in a remembered in-memory state value and navigates to the Book tab.
- `BookScreen` receives the draft and creates `BookingViewModel`, which generates flight number, gate, boarding group, departure/arrival times, seat options, and boarding pass preview state.

### Manual Test
- Open Home.
- Select any departure card.
- Tap `Book <CODE> Flight`.
- Confirm Book shows the selected origin/destination, duration, distance, reward, generated flight number, and gate.
- Select a different seat.
- Tap `Continue to Boarding Pass`.
- Confirm the boarding pass shows TravelBlock, flight number, origin/destination, seat, gate, boarding group, departure/arrival, and focus duration.
- Tap `Start Focus Flight`; it should route to the Cockpit placeholder.

### Commands Run
- Baseline before changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

- After changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no emulator or device attached.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: no local AVDs listed.

### Tests Added
- `BookingViewModelTest`
  - Creates booking state from selected route.
  - Confirms generated flight number, gate, and boarding group are non-empty.
  - Confirms selecting a seat updates state.
  - Confirms boarding pass contains correct origin, destination, duration, and seat.
- `BookScreenTest`
  - Compose instrumentation test compiles and covers destination display, seat selection, boarding pass preview, and Start Focus Flight callback.
  - Not executed with `connectedDebugAndroidTest` because no device/emulator/AVD was available.

### Next Phase
- Implement the real Cockpit draft handoff and active-flight timer state.
- Add timestamp-based progress calculation, resilient active flight storage later, and arrival/diversion boundaries after the timer is stable.

## 2026-05-19 - First Cockpit Screen

### What Changed
- Added in-memory `ActiveFlight` state created from the selected `BoardingPass` when `Start Focus Flight` is tapped.
- Added timestamp-based active flight progress calculation using `startedAt`, `plannedArrivalAt`, and current system time.
- Replaced the Cockpit placeholder with a dark, focused cockpit UI showing route, countdown, progress, miles flown/remaining, ETA, seat, flight number, decorative altitude/speed/heading, Ambient Mode placeholder, and Divert action.
- Added temporary Arrived and Diverted result states without logbook writes.
- Added a diversion confirmation dialog.
- Kept persistence, Room, DataStore, notifications, audio, and logbook saving out of scope.

### Files Created or Edited
- `app/src/main/java/com/travelblock/app/domain/model/ActiveFlight.kt`
- `app/src/main/java/com/travelblock/app/domain/model/ActiveFlightStatus.kt`
- `app/src/main/java/com/travelblock/app/domain/model/BoardingPass.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/ActiveFlightProgress.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModelFactory.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/ActiveFlightProgressTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModelTest.kt`
- `AGENT_NOTES.md`

### Manual Test Flow
- Open Home.
- Select a departure route.
- Tap `Book <CODE> Flight`.
- Select a seat.
- Tap `Continue to Boarding Pass`.
- Tap `Start Focus Flight`.
- Confirm Cockpit opens with `Focus Mode`, destination code/name, countdown, route visualization, miles flown/remaining, ETA, seat, and decorative flight stats.
- Tap `Divert`, confirm the dialog, and verify the diverted placeholder state appears.
- For a very short test flight later, verify the screen changes to Arrived when the planned arrival time passes.

### Commands Run
- Baseline before changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

- After changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no emulator or device attached.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: no local AVDs listed.

### Tests Added
- `ActiveFlightProgressTest`
  - Start progress is near 0.
  - Halfway time gives about 50%.
  - After `plannedArrivalAt` is complete.
  - Remaining time never goes below zero.
- `CockpitViewModelTest`
  - Start flight creates active cockpit state.
  - Arrival time changes state to Arrived.
  - Divert confirmation changes state to Diverted.

### Known Limitations
- Active flight state is in memory only and will be lost if the process is killed.
- The countdown recomputes from timestamps while the screen is active, but no background service/notification exists yet.
- Arrival and diversion are placeholder result states and do not write to the logbook.
- Connected UI tests were not run because no emulator/device/AVD was available.

### Next Recommended Task
- Add resilient active-flight storage and restore behavior using the planned local persistence layer.
- Then add arrival/diversion result screens and logbook write boundaries once persistence exists.

## 2026-05-19 - Persistence Foundation

### What Changed
- Added AndroidX DataStore preferences behind `SettingsRepository`.
- Added Room database support behind `FlightLogRepository` and `PointsRepository`.
- Wired the app through a small `TravelBlockContainer` so screens share one airport repository, settings repository, flight log repository, points repository, and Room database instance.
- Home now reads the current airport from settings, defaults to `KOUN`, and observes the points balance.
- Cockpit now writes completed and diverted flight results to the logbook repository.
- Completed flights award placeholder points and move the current airport to the destination.
- Diverted flights write a diverted log with zero points and leave the current airport unchanged for now.
- Replaced the Logbook placeholder with a persisted flight history list and empty state.

### Files Created or Edited
- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/java/com/travelblock/app/TravelBlockContainer.kt`
- `app/src/main/java/com/travelblock/app/data/database/FlightLogDao.kt`
- `app/src/main/java/com/travelblock/app/data/database/FlightLogEntity.kt`
- `app/src/main/java/com/travelblock/app/data/database/FlightLogStatus.kt`
- `app/src/main/java/com/travelblock/app/data/database/PointsTransactionDao.kt`
- `app/src/main/java/com/travelblock/app/data/database/PointsTransactionEntity.kt`
- `app/src/main/java/com/travelblock/app/data/database/TravelBlockDatabase.kt`
- `app/src/main/java/com/travelblock/app/data/repository/DefaultFlightLogRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/DefaultPointsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/FlightLogRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/PointsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/DataStoreSettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/SettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/UserSettings.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModelFactory.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModelFactory.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookViewModelFactory.kt`
- `app/src/androidTest/java/com/travelblock/app/data/database/TravelBlockDatabaseTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `AGENT_NOTES.md`

### Persistence Architecture
- DataStore stores lightweight user preferences:
  - `homeAirportCode`, default `KOUN`
  - `currentAirportCode`, default `homeAirportCode`
  - `selectedTheme`, default `system`
  - `soundEnabled`, default `true`
  - `onboardingComplete`, default `true`
- Room stores app history and point accounting:
  - `FlightLogEntity`
    - `id`
    - `flightNumber`
    - `originCode`
    - `destinationCode`
    - `durationMinutes`
    - `distanceMiles`
    - `status`
    - `startedAt`
    - `endedAt`
    - `seat`
    - `pointsEarned`
  - `PointsTransactionEntity`
    - `id`
    - `amount`
    - `reason`
    - `createdAt`
    - `relatedFlightId`
- Points balance is derived from Room point transactions rather than duplicated in DataStore.
- Room schema export is disabled for this phase with `exportSchema = false`; add schema export and migrations before shipping a real production database.

### Tests Added
- `CockpitPersistenceTest`
  - Completed flight inserts a completed log.
  - Completed flight creates a points transaction.
  - Completed flight updates current airport to the destination.
  - Diverted flight inserts a diverted log.
  - Diverted flight does not move current airport.
- `TravelBlockDatabaseTest`
  - Room DAO smoke coverage for inserting flight logs and point transactions.
  - Confirms point balance can be calculated from persisted transactions.

### Commands Run
- Baseline before changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

- After changes:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug assembleDebugAndroidTest
```

- First result: failed in `CockpitPersistenceTest` because a flight initialized after its planned arrival was already in `Arrived` state before `tick()` could save the result.
- Fix: `CockpitViewModel.tick()` now saves a completed result when the current state is already `Arrived`.
- Final result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no emulator or device attached.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: no local AVDs listed.

### Manual Verification
- Open Home and confirm it still loads departures from the current airport.
- Select a route, tap `Book <CODE> Flight`, select a seat, continue to boarding pass, and tap `Start Focus Flight`.
- In Cockpit, let the flight complete or use Divert.
- Open Logbook and confirm the completed or diverted flight appears with flight number, route, duration, status, points, and date.
- Relaunch the app after a completed flight and confirm Home uses the completed destination as the current airport.

### Known Limitations
- No cloud sync, accounts, external services, notifications, or background active-flight restore were added.
- Diverted flights intentionally earn zero placeholder points and do not change current airport; final diversion product behavior is still open.
- Existing installs may need app data cleared if the Room schema changes during development because there is no migration strategy yet.
- `connectedDebugAndroidTest` was not run because no emulator/device/AVD is currently available, but the Android test APK was assembled successfully with `assembleDebugAndroidTest`.

### Next Recommended Task
- Add an active-flight restore model so an in-progress flight can survive process death.
- Add Room schema export and migration discipline once the schema stabilizes.
- Decide final points rules for completed and diverted flights before expanding rewards or store unlocks.

## 2026-05-20 - Self-Audit and Repair Pass

### Current Project Status
- Native Android Kotlin/Jetpack Compose app is still the only app target.
- Current implemented surface is ahead of the linear roadmap in a few places:
  - Phase 2 airport/domain engine exists.
  - Route-first Home exists.
  - Booking and boarding pass skeleton exists.
  - In-session Cockpit exists.
  - Phase 3 persistence foundation now exists for settings, flight logs, and points.
- App metadata now matches the persistence foundation phase with `versionName = "0.3.0-persistence"` and `versionCode = 300`.
- Store and Settings intentionally remain lightweight placeholders.
- `MANUAL_TEST_PLAN.md` now documents the current manual QA path.

### Audit Findings and Repairs
- Fixed a stale Home label that always said `From KOUN`; it now uses the current airport code.
- Removed default Android Studio sample tests that were not testing TravelBlock behavior.
- Removed old empty package marker files now that real code exists in those packages.
- Removed an unused navigation icon import.
- Added `.kotlin` to `.gitignore` because Gradle/Kotlin created local session/cache metadata.
- Confirmed no React Native, Expo, Flutter, or web tooling was added. Search hits were only documentation references, Android `exported`, Room `exportSchema`, and airport names containing matching substrings.
- Confirmed package structure remains coherent around `data`, `data.local`, `data.repository`, `data.database`, `data.settings`, `domain.engine`, `domain.model`, `ui.navigation`, `ui.theme`, and tab screens.

### Commands Run and Results
- Before cleanup:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat lintDebug
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: no device attached and no local AVDs listed, so `connectedDebugAndroidTest` was not run.

- After cleanup, an attempted parallel Gradle run hit Kotlin incremental cache contention from multiple Gradle tasks compiling the same module at once. This was repaired by stopping daemons and rerunning sequentially:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat --stop; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug; .\gradlew.bat assembleDebugAndroidTest
```

- Result: all commands `BUILD SUCCESSFUL`

### Known Bugs or Limits Remaining
- Active flight state is still in memory while a flight is running; process death restore is not implemented.
- Connected Android tests could not be executed without a device/emulator.
- Room schema export and migrations are not set up yet; development installs may need app data cleared after schema changes.
- Onboarding and airport picker are not implemented yet even though DataStore defaults support a home/current airport.
- Store and Settings are still placeholders.

### Recommended Next Phase
- Implement active-flight restore behavior before adding more UI breadth.
- Then add onboarding and airport picker so home/current airport defaults become user-controlled.
- Keep rewards/store work blocked until point rules are agreed.

### Manual Test Safety
- Safe for Alex to open and manually test as a debug build.
- Best manual path is documented in `MANUAL_TEST_PLAN.md`.
- The app is suitable for Home -> Booking -> Cockpit -> Logbook smoke testing, with the known caveat that active flights do not yet survive process death.

## 2026-05-20 - Route Authenticity Tuning

### What Changed
- Tuned route sorting so airports with recognizable larger-airport naming signals are prioritized inside availability groups.
- Added centralized airport-name signals for `international`, `world`, `continental`, `continential`, `intercontinental`, `global`, `regional`, `municipal`, `metro`, `metropolitan`, and `city`.
- Replaced the simple rounded cruise estimate with a more airline-like ETA calculation:
  - short-route cruise speed
  - taxi-out time
  - climb/descent time
  - 5-minute rounded display
- Home route cards now show separate `Travel` ETA and `Focus` time.
- Booking now carries the route ETA forward and uses it for displayed arrival time while keeping focus duration as the cockpit timer duration.
- Compose UI instrumentation tests were anchored to `MainActivity`; they are currently ignored on device because the physical-device test services setup reports no Compose hierarchy after an `androidx.test.services` appops error.

### Files Created or Edited
- `app/src/main/java/com/travelblock/app/domain/engine/AirportRouteEngine.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/RouteEngineConfig.kt`
- `app/src/main/java/com/travelblock/app/domain/model/DraftFlightBooking.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/AirportRouteEngineTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/book/BookingViewModelTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/home/HomeScreenTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `AGENT_NOTES.md`

### Tests Added or Updated
- Added a route-engine unit test confirming larger/recognizable airport names are prioritized among comparable route matches.
- Added a route-engine unit test confirming ETA includes airport operations rather than only raw cruise distance.
- Updated Home and Book tests for the new ETA fields.
- Marked the two Compose UI instrumentation tests ignored until the device-side Compose test harness issue is resolved.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug
```

- First run hit a stale Kotlin daemon/package-resolution issue.
- After `.\gradlew.bat --stop`, result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug lintDebug assembleDebugAndroidTest connectedDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`
- Device used: `SM-S918U - 16`
- Connected run still prints:
  - `appops set androidx.test.services MANAGE_EXTERNAL_STORAGE allow`
  - `Error: No UID for androidx.test.services in user 0`
- The Room instrumentation test runs; the two Compose UI tests are skipped with an explicit `@Ignore` reason.

### Known Issues
- Connected Compose UI tests need a more reliable activity/test-services setup before they can be re-enabled.
- The airport priority list is intentionally simple and name-based; future airport metadata such as passenger volume or scheduled service would be better if added to the dataset.
- Travel ETA and focus duration can differ by design: ETA is route realism, focus duration remains the product timer.

### Next Recommended Task
- Add a first-class route scoring object so distance fit, airport prominence, and route labels can be surfaced/debugged without burying all logic in sorting.
- Revisit connected Compose test infrastructure before expanding UI test coverage.

## 2026-05-20 - Flight Time Is the Study Timer

### What Changed
- Corrected the route model usage so calculated flight time is the actual study timer.
- Removed the separate `estimatedTravelMinutes` draft-booking field.
- Removed Home's separate `Travel` ETA display.
- Home now shows a single timer value from real-ish flight calculations, labeled as flight time.
- Booking arrival time now uses the same calculated flight duration that becomes the boarding pass focus duration and Cockpit planned arrival.
- Points now use the calculated flight duration rather than the selected chip duration.
- Duration chips remain the route-discovery control; returned routes can have calculated flight times that fit the selected travel window.

### Files Edited
- `app/src/main/java/com/travelblock/app/domain/model/DraftFlightBooking.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/book/BookingViewModelTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/home/HomeScreenTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `AGENT_NOTES.md`

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug lintDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat connectedDebugAndroidTest
```

- Result: failed because adb reported `No connected devices!`
- Follow-up `adb devices` showed no attached devices.
- Previous connected run was green with the two Compose UI tests skipped for the documented test-services harness blocker.

### Known Issues
- Connected device availability was unstable during this pass.
- The route calculation is still a simple local simulation model, not real-world schedule/block-time data.

## 2026-05-20 - Booking Flow and Seat Map Corrections

### What Changed
- Removed Booking from the bottom navigation tabs.
- Kept Booking as an internal full-screen route launched from Home.
- Hid the bottom navigation bar during the booking flow so Booking takes over the screen.
- Added a `Back to Departures` action in Booking.
- Replaced list-style seat/class selection with a cabin seat map.
- Seat choices are now row/seat positions from rows `3` through `18`, columns `A-F`, with an aisle between `C` and `D`.
- Seat map still uses placeholder point costs by seat zone:
  - Premium forward cabin
  - Extra legroom rows
  - Window seats
  - Standard seats
- Added seat content descriptions so UI tests can target specific seats.
- Tuned airport route ranking so larger airport naming signals stay primary and airports with both ICAO and IATA codes get an additional boost among comparable options.

### Files Edited
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockDestination.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/AirportRouteEngine.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/RouteEngineConfig.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/AirportRouteEngineTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `AGENT_NOTES.md`

### Tests Added or Updated
- Added a route-engine unit test for prioritizing comparable airports that have both ICAO and IATA codes.
- Updated the Book UI test to select a real seat by content description instead of tapping a fare/class list row.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug lintDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no connected devices, so `connectedDebugAndroidTest` was not rerun in this pass.

### Known Issues
- Seat map is still a simple single-cabin layout; it is now spatial, but not aircraft-type-specific.
- Booking is full-screen but still implemented as a Navigation Compose route, not a separate activity.

## 2026-05-20 - Arrival / Landed Flow

### Arrival Flow Summary
- Added a full-screen Arrival route that is shown when an active flight reaches its planned arrival timestamp.
- Cockpit still owns completion detection and persistence, so completed flight saving, point awards, and current-airport updates happen exactly once in the existing `CockpitViewModel` path.
- Added an `ArrivalSummary` model for passing landed-flight display data from Cockpit to Arrival without adding another database write path.
- Added a centralized `FlightRewardCalculator` using the v1 reward rule from `DATA_SPEC.md`.
- Arrival now shows destination details, route, flight number, focus duration, distance flown, points earned, seat, and completion status.
- Added post-flight actions:
  - `Continue Journey` returns to Home, where the persisted destination is now the current airport.
  - `Return Home` shows a clean placeholder for future return-home route planning.
  - `View Logbook` navigates to Logbook.

### Files Changed
- `app/src/main/java/com/travelblock/app/domain/engine/FlightRewardCalculator.kt`
- `app/src/main/java/com/travelblock/app/domain/model/ArrivalSummary.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockDestination.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/arrival/ArrivalScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `AGENT_NOTES.md`

### Completion, Points, and Current-Airport Behavior
- Completed flights are inserted into Room through `FlightLogRepository`.
- Completed flights create a points transaction through `PointsRepository`.
- Completed flights update DataStore current airport to the destination through `SettingsRepository`.
- Duplicate completion saves are guarded by `CockpitViewModel.resultSaved`.
- Diverted flights continue to save as diverted with zero points and do not move the current airport.

### Tests Added or Updated
- Added a persistence test that calls completion twice and verifies only one flight log and one points transaction are created.
- Updated completed-flight persistence assertions to verify the saved flight log also carries awarded points.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Baseline before changes: `BUILD SUCCESSFUL`
- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Baseline before changes: `BUILD SUCCESSFUL`
- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat lintDebug
```

- Baseline before changes: `BUILD SUCCESSFUL`
- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no connected devices, so `connectedDebugAndroidTest` was not run.

### Manual Test Steps
- Open Home.
- Select a route and tap `Book <CODE> Flight`.
- Select a seat, continue to boarding pass, and tap `Start Focus Flight`.
- Let the flight timer reach zero.
- Confirm Arrival opens with destination, route, flight number, focus duration, distance, points, seat, and completed status.
- Tap `Continue Journey` and confirm Home now routes from the landed destination.
- Repeat with another flight or use `View Logbook` to confirm the completed flight is visible.
- Tap `Return Home` on Arrival to confirm the placeholder card appears.

### Known Limitations
- Active flight state is still in memory while the flight is running; process-death restore remains a Phase 7 hardening item.
- Return-home route preparation is a placeholder only.
- Arrival is driven by the active Cockpit screen ticking; no background notification or service exists yet.
- Connected Android tests were not run because no device was attached.

### Next Recommended Task
- Add resilient active-flight restore before expanding the arrival/diversion surface further.
- Then implement a real return-home route builder and a flight-detail view in Logbook.

## 2026-05-20 - Real Diversion Flow

### Diversion Flow Summary
- Improved the Cockpit diversion confirmation so it shows route, focused time, miles flown, and a warning that completion rewards will be reduced or lost.
- Added a dedicated full-screen Diversion result route.
- Added timestamp-based diversion summaries with focused duration, percent complete, miles flown, miles skipped, points earned, intended destination, and actual diversion airport.
- Added `DiversionEngine`, which interpolates the current simulated flight position and finds the closest airport from the bundled airport dataset, excluding the intended destination.
- Diverted flights now persist with the actual diversion airport as the saved destination.
- Diverted flights update current airport to the actual diversion airport, not the intended destination.
- Added reduced diversion rewards using the documented rule:
  - under 25% complete earns 0 points
  - at or above 25% complete earns `floor(fullReward * progress * 0.5)`

### Files Changed
- `app/src/main/java/com/travelblock/app/domain/engine/AirportRouteEngine.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/DiversionEngine.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/FlightRewardCalculator.kt`
- `app/src/main/java/com/travelblock/app/domain/model/DiversionSummary.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockDestination.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitViewModelFactory.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/diversion/DiversionScreen.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/ActiveFlightProgressTest.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/DiversionEngineTest.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/FlightRewardCalculatorTest.kt`
- `app/src/test/java/com/travelblock/app/domain/model/DiversionSummaryTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `AGENT_NOTES.md`

### Persistence and Current-Airport Rule
- Completed flights still land at the intended destination.
- Diverted flights land at the nearest calculated diversion airport.
- Current airport changes to that diversion airport after a diverted flight.
- The intended destination is excluded as a diversion target.
- If no airport dataset is available, diversion falls back to the origin airport.
- Duplicate diverted logs remain guarded by `CockpitViewModel.resultSaved`.

### Tests Added or Updated
- Added diversion engine tests for nearest-airport selection and intended-destination exclusion.
- Added diversion summary tests for focused duration, miles flown, miles skipped, and percent complete.
- Added reward tests for early zero-point diversions, reduced later diversions, and clamped progress.
- Added active-flight progress test coverage for before-start clamping.
- Added persistence coverage for:
  - diverted status saved
  - reduced diversion points
  - nearest diversion airport saved as destination
  - current airport updates to diversion airport
  - duplicate diversion confirmation does not duplicate logs or point transactions

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- Baseline before changes: all `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug; .\gradlew.bat assembleDebugAndroidTest
```

- After changes: all `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no connected devices, so `connectedDebugAndroidTest` was not run.

### Manual Test Steps
- Open Home.
- Select a route and tap `Book <CODE> Flight`.
- Select a seat, continue to boarding pass, and tap `Start Focus Flight`.
- In Cockpit, tap `Divert`.
- Confirm the dialog shows route, focused time, miles flown, and reward warning.
- Tap `Stay Focused` and confirm the active flight continues.
- Tap `Divert` again, then `Divert Flight`.
- Confirm the Diversion screen shows intended destination, actual diversion airport, time focused, miles flown, miles skipped, percent complete, points, and diverted status.
- Tap `Return to Home` and confirm Home uses the diversion airport as current airport.
- Open Logbook and confirm the saved flight is marked `DIVERTED`.

### Known Limitations
- Diversion position uses simple route interpolation between origin and intended destination, not a full great-circle intermediate point.
- The nearest-airport rule uses airport coordinates only; it does not yet filter for runway length, airport operating status, or aircraft suitability.
- Room still lacks explicit fields for intended destination vs actual diversion airport, so the saved `destinationCode` is the actual diversion airport while the result screen preserves the intended destination in memory.
- Connected Android tests were not run because no device was attached.

### Next Recommended Task
- Add Room fields for intended destination, actual landed airport, miles flown, and diversion reason before building detailed logbook views.
- Replace linear route interpolation with a true great-circle intermediate point if diversion accuracy becomes user-visible on a map.

## 2026-05-20 - Logbook History and Detail

### Logbook Upgrade Summary
- Upgraded Logbook from a simple persisted list into a flight-history section with compact summary stats, status-aware cards, and detail views.
- Added summary totals for:
  - total flights
  - completed flights
  - diverted flights
  - total focus time
  - total miles
  - total points earned
- Logbook entries now show flight number, route, date/time, duration, distance, status, and points.
- Completed and diverted flights now use distinct visual status treatments.
- Empty state now says `No flights logged yet` and nudges the user to book a focus flight.
- Added a flight detail route and travel-document style detail screen with route, date/time, duration, distance, points, seat, and completion/diversion context.

### Files Changed
- `app/src/main/java/com/travelblock/app/data/database/FlightLogDao.kt`
- `app/src/main/java/com/travelblock/app/data/repository/FlightLogRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/DefaultFlightLogRepository.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockDestination.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/FlightLogDetailViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/FlightLogDetailViewModelFactory.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookViewModel.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/logbook/LogbookViewModelTest.kt`
- `AGENT_NOTES.md`

### Repository and Navigation
- Added `getFlightLogById` to `FlightLogDao`.
- Added `getFlightById` to `FlightLogRepository` and `DefaultFlightLogRepository`.
- Added `TravelBlockDestination.LogbookDetail` with a `flightId` route argument.
- Logbook cards navigate to detail through `TravelBlockApp`.
- Detail screen uses normal Navigation Compose back behavior through `popBackStack`.

### Tests Added or Updated
- Added `LogbookViewModelTest` coverage for:
  - summary stats from sample logs
  - completed/diverted counts and visual state flags
  - defensive newest-first sorting
  - detail lookup by id
- Updated the cockpit fake repository used in persistence tests to implement `getFlightById`.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- Baseline before changes: all `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- First after-change run failed at Kotlin compile because an `EmptyLogbookCard` call still used the old positional argument.
- Fixed the call to use `message = "Loading flight history..."`.
- Final result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug; .\gradlew.bat assembleDebugAndroidTest
```

- Result: all `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no connected devices, so `connectedDebugAndroidTest` was not run.

### Manual Test Steps
- Open Logbook before any saved flights and confirm the polished empty state appears.
- Complete or divert a focus flight.
- Open Logbook and confirm summary stats update.
- Confirm each flight card shows route, date/time, duration, distance, status, and points.
- Tap a completed flight and confirm the detail screen opens with boarding-pass style information.
- Tap a diverted flight and confirm the detail message explains the saved diversion destination behavior.
- Use Back to return from detail to the Logbook list.

### Known Limitations
- Flight records still do not store separate intended destination vs actual diversion airport, so detail views can only explain the current persistence shape.
- Summary stats are calculated in the ViewModel from currently loaded logs, which is fine for local MVP data but can become a DAO aggregate later.
- No map history, export, sharing, or filters were added in this pass.
- Connected Android tests were not run because no device was attached.

### Next Recommended Task
- Add Room schema fields for intended destination, actual landed airport, miles flown, and diversion reason.
- Add Logbook filters for all, completed, and diverted after the schema can express diversion details cleanly.

## 2026-05-20 - Rewards / Store Foundation

### Store System Summary
- Replaced the Store placeholder with a real airline-perks style Store screen.
- Added points balance display backed by the existing `PointsRepository`.
- Added unlockable cosmetics for:
  - boarding pass themes
  - aircraft icons
  - cabin ambience packs as a future audio placeholder
- Added Room persistence for unlocked cosmetics through `UnlockEntity` and `UnlockDao`.
- Added `StoreRepository` and `DefaultStoreRepository`.
- Added `StoreViewModel` purchase/equip logic.
- Purchases deduct points by inserting a negative points transaction.
- Duplicate purchases are blocked and do not double charge.
- Equipped boarding pass theme and aircraft icon ids are persisted through DataStore settings.

### Files Changed
- `app/src/main/java/com/travelblock/app/TravelBlockContainer.kt`
- `app/src/main/java/com/travelblock/app/data/database/TravelBlockDatabase.kt`
- `app/src/main/java/com/travelblock/app/data/database/UnlockDao.kt`
- `app/src/main/java/com/travelblock/app/data/database/UnlockEntity.kt`
- `app/src/main/java/com/travelblock/app/data/repository/DefaultStoreRepository.kt`
- `app/src/main/java/com/travelblock/app/data/repository/StoreRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/DataStoreSettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/SettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/UserSettings.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/StoreCatalog.kt`
- `app/src/main/java/com/travelblock/app/domain/model/StoreItem.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModelFactory.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreViewModelFactory.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/book/BookingViewModelTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/store/StoreViewModelTest.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/book/BookScreenTest.kt`
- `AGENT_NOTES.md`

### Purchase and Equip Rules
- Store unlocks require enough available points.
- A successful unlock writes an `UnlockEntity` and a negative `PointsTransactionEntity`.
- Re-buying an unlocked item returns an already-unlocked state and does not charge again.
- Boarding pass themes and aircraft icons can be equipped only after unlocking.
- Cabin ambience is listed as a future pack but does not equip yet.
- Booking seat upgrades now check available points before selection.
- Standard seats are free.
- Paid seats are selectable only when the user has enough points.
- Seat upgrade points are not deducted yet; selection only holds the upgrade until a future booking-confirmation phase.

### Tests Added or Updated
- Added `StoreViewModelTest` coverage for:
  - purchase succeeds with enough points
  - purchase fails with insufficient points
  - duplicate purchase does not double charge
  - equip only works for unlocked items
  - successful purchases create a points transaction
- Updated `BookingViewModelTest` for paid-seat point checks and free-seat default selection.
- Updated the ignored Compose booking test to pass a point balance.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- Baseline before changes: all `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat lintDebug
```

- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebugAndroidTest
```

- After changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug lintDebug
```

- Final verification after the Store insufficient-points button UX adjustment: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebugAndroidTest
```

- Final Android test APK build after the same adjustment: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

- Result: no connected devices, so `connectedDebugAndroidTest` was not run.

### Known Limitations
- Room database version is now `2` and uses destructive fallback for this development phase; existing local app installs may reset database data after schema changes.
- Equipped boarding pass theme and aircraft icon are persisted but only lightly surfaced; applying distinct boarding-pass visuals and cockpit aircraft icon rendering should be a follow-up.
- Cabin ambience packs are catalog placeholders until audio/ambient mode exists.
- Seat upgrade selection checks points but does not spend them yet.

### Next Recommended Task
- Apply equipped cosmetics to the boarding pass and cockpit route visualization.
- Add a proper booking confirmation boundary if seat-upgrade spending should happen before flight start.
- Replace destructive development migration with explicit Room migrations once the schema settles.

## 2026-05-20 - Settings and Airport Picker

### Settings Summary
- Replaced the Settings placeholder with a real `Cabin Controls` screen.
- Added sections for:
  - Pilot profile placeholder
  - Home airport
  - Current airport
  - Theme preference
  - Sound toggle
  - Haptics toggle
  - Reset local data
  - About TravelBlock
- Added a reusable airport picker dialog for home and current airports.
- Airport search matches code, ICAO, IATA, name, city, state/region, and country using the real bundled airport dataset.
- Airport picker cards show code, name, location, and distance from the current airport when available.
- Changing home airport prompts whether to also move current airport.
- Changing current airport immediately updates the persisted current airport, which Home uses as the route origin.
- Reset local data requires confirmation and clears Room tables plus DataStore preferences, returning defaults to KOUN.
- Added haptics preference support to DataStore settings.
- Updated package metadata to `versionName = "0.11.0-cabin-polish"` and `versionCode = 1100`.

### Files Changed
- `app/build.gradle.kts`
- `app/src/main/java/com/travelblock/app/TravelBlockContainer.kt`
- `app/src/main/java/com/travelblock/app/data/settings/DataStoreSettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/SettingsRepository.kt`
- `app/src/main/java/com/travelblock/app/data/settings/UserSettings.kt`
- `app/src/main/java/com/travelblock/app/domain/engine/AirportSearchEngine.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsViewModelFactory.kt`
- `app/src/test/java/com/travelblock/app/MainDispatcherRule.kt`
- `app/src/test/java/com/travelblock/app/domain/engine/AirportSearchEngineTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/home/HomeViewModelTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/settings/SettingsViewModelTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/cockpit/CockpitPersistenceTest.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/store/StoreViewModelTest.kt`
- `AGENT_NOTES.md`

### Reset Rule
- Reset clears all Room tables through `TravelBlockDatabase.clearAllTables()`.
- Reset clears DataStore preferences through `SettingsRepository.resetToDefaults()`.
- Defaults are restored by the existing `UserSettings` defaults:
  - home airport: `KOUN`
  - current airport: `KOUN`
  - theme: `system`
  - sound enabled: `true`
  - haptics enabled: `true`
  - onboarding complete: `true`
- This is a local development reset, not an account/cloud reset.

### Tests Added or Updated
- Added `AirportSearchEngineTest` coverage for search by code/IATA, city, and airport name.
- Added `SettingsViewModelTest` coverage for:
  - changing home airport persists separately
  - changing current airport persists separately
  - reset local data restores defaults
- Updated `HomeViewModelTest` to verify current-airport setting changes the Home route origin.
- Added `MainDispatcherRule` for ViewModel tests that collect settings flows.
- Updated existing fake `SettingsRepository` implementations for theme, sound, haptics, and reset.

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- Baseline before changes: all `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- First after-change run failed because `SettingsCard` used `Column` as a receiver type instead of `ColumnScope`.
- Fixed the compile error.
- Second run failed in new flow-backed ViewModel tests because `Dispatchers.Main` was not controlled.
- Added `MainDispatcherRule`.
- Final result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test assembleDebug lintDebug assembleDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

- Result: `Pixel_10_Pro`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat installDebug
```

- Result: `BUILD SUCCESSFUL`; installed on `emulator-5554`.

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat connectedDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`
- Existing Compose UI tests remained skipped by annotation, but the instrumentation test task ran successfully on `Pixel_10_Pro(AVD) - 17`.

### Emulator Smoke Test
- Started the `Pixel_10_Pro` AVD through Android CLI.
- Installed and launched `com.travelblock.app/.MainActivity`.
- Verified Home opened and showed KOUN initially.
- Opened Settings from bottom navigation.
- Verified `Cabin Controls`, home/current airport sections, theme chips, and sound toggle were visible.
- Opened the Home Airport picker.
- Searched for `KTUL`.
- Verified `KTUL / Tulsa International Airport / Tulsa, Oklahoma, US` appeared with distance from current airport.
- Selected KTUL and confirmed `Move Current Too`.
- Returned to Home and verified the current airport card and departure board changed to `KTUL` / `From KTUL`.

### Known Limitations
- Theme preference is persisted but not yet applied globally to force light/dark mode.
- Sound and haptics toggles are persisted for future hooks; no audio or vibration behavior exists yet.
- Reset keeps `onboardingComplete = true` because this project does not yet have onboarding screens.
- Airport picker is implemented as a dialog; a full-screen picker may be more comfortable on smaller phones later.

### Next Recommended Task
- Apply persisted theme mode to `TravelBlockTheme`.
- Add a real onboarding flow using the same airport picker.
- Add settings-aware sound/haptic hooks when audio and haptics are implemented.

## 2026-05-20 - UI Refinement and Travel Polish

### Visual Changes
- Added a shared Compose component layer for TravelBlock visual primitives:
  - `AirportCard`
  - `RouteCard`
  - `DurationChip`
  - `BoardingPassCard`
  - `SectionHeader`
  - `PointsPill`
  - `EmptyState`
  - `StatusChip`
- Updated Home to use shared airport/departure/points components while keeping the route-first departure-board behavior.
- Updated Book to use shared empty state, section header, and boarding pass card while preserving the full-screen booking flow.
- Polished bottom navigation colors so selected destinations use the soft travel-blue treatment.
- Updated Logbook, Store, and Settings headers/status treatments for a more coherent clean travel-app feel.
- Kept Cockpit visually distinct and dark; no new neon styling was added to normal screens.
- Updated version metadata to `versionName = "0.12.0-ui-refinement"` and `versionCode = 1200`.
- Expanded `MANUAL_TEST_PLAN.md` with visual checks for Home, Book, Boarding Pass, Cockpit, Arrival, Diversion, Logbook, Store, and Settings.

### Files Created or Edited
- `app/build.gradle.kts`
- `app/src/main/java/com/travelblock/app/ui/components/TravelBlockComponents.kt`
- `app/src/main/java/com/travelblock/app/ui/navigation/TravelBlockBottomBar.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/store/StoreScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/settings/SettingsScreen.kt`
- `MANUAL_TEST_PLAN.md`
- `AGENT_NOTES.md`

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- Baseline before changes: all `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- After UI component changes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug; .\gradlew.bat lintDebug
```

- After UI component changes: both `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat installDebug
```

- Result: `BUILD SUCCESSFUL`; installed on `Pixel_10_Pro(AVD) - 17`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat connectedDebugAndroidTest
```

- Result: `BUILD SUCCESSFUL`
- Existing Compose UI tests remained skipped by annotation, but the connected instrumentation task ran successfully on `Pixel_10_Pro(AVD) - 17`.

### Emulator QA
- Used the Android emulator QA workflow with `adb` on `emulator-5554`.
- Installed and launched `com.travelblock.app/.MainActivity`.
- Verified Home renders the polished `TravelBlock` header, points pill, current airport card, duration chips, route cards, and bottom navigation.
- Verified Store renders the shared section header, points pill, status chips, and airline-perks catalog treatment.
- Captured screenshots to:
  - `app/build/reports/ui-polish/home.png`
  - `app/build/reports/ui-polish/store.png`

### Known Limitations and Remaining Polish
- Active flight state remains in memory while flying; this pass intentionally did not harden timer persistence.
- Several screens still have local card/metric helpers that can be gradually replaced with shared components in later polish passes.
- Arrival and Diversion retain their current custom result styling; they are visually aligned enough for this pass but can be unified further.
- Compose UI tests are still annotated to skip, so visual QA currently depends on emulator smoke testing and manual checks.

### Next Recommended Task
- Apply persisted light/dark theme preference globally.
- Add explicit content descriptions/test tags to shared cards and buttons before the release-candidate QA pass.
- Consider adding screenshot tests once the UI direction settles.

## 2026-05-20 - Release Smoke Test, Bug Bash, and Readiness Report

### Current Project Status
- Native Android Kotlin/Jetpack Compose app builds and installs successfully.
- Home, booking, boarding pass, cockpit, logbook, store, and settings foundations are present.
- The app is safe for Alex to open and manually test as a debug build.
- This is not yet production-release ready because active-flight process recovery, background behavior, onboarding, and fully enabled Compose UI regression tests still need work.

### Bugs Fixed
- Limited the Home departure preview to the top three ranked routes so the selected-flight booking action is easier to reach during normal phone use.
- Moved Cockpit diversion into a dedicated `Flight Controls` card so the action is not positioned at the risky lower edge near bottom navigation/gesture handling.

### Files Created or Edited
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/cockpit/CockpitScreen.kt`
- `MANUAL_TEST_PLAN.md`
- `RELEASE_READINESS.md`
- `AGENT_NOTES.md`

### Commands Run and Results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat clean
```

- Result: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test
```

- Result before fixes: `BUILD SUCCESSFUL`
- Result after fixes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug
```

- Result before fixes: `BUILD SUCCESSFUL`
- Result after fixes: `BUILD SUCCESSFUL`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat lintDebug
```

- Result before fixes: `BUILD SUCCESSFUL`
- Result after fixes: `BUILD SUCCESSFUL`
- Lint report: `app/build/reports/lint-results-debug.html`

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat connectedDebugAndroidTest
```

- Result before fixes: `BUILD SUCCESSFUL`
- Result after fixes: `BUILD SUCCESSFUL`
- Ran on `Pixel_10_Pro(AVD) - 17`.
- Existing Compose UI tests for Home and Book were reported as skipped by annotation, but the connected task completed successfully.

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat installDebug
```

- Result: `BUILD SUCCESSFUL`

### Emulator Smoke Test
- Used the Android emulator QA workflow with `Pixel_10_Pro(AVD) - 17`.
- Cleared app data and launched `com.travelblock.app`.
- Verified Home loads with KOUN, points, duration chips, route cards, and bottom navigation.
- Verified a KDFW path through route selection, booking review, cabin seat-map selection, boarding pass preview, and Cockpit start.
- Verified Cockpit displays selected flight data, countdown timer, route progress, miles, ETA, seat, flight number, ambient placeholder, and diversion controls.
- Created `RELEASE_READINESS.md` with current feature status, known gaps, command results, and manual-test recommendation.
- Updated `MANUAL_TEST_PLAN.md` with final release-smoke steps and current limitations.

### Known Bugs or Limitations Remaining
- Active flight state remains in memory while a flight is active.
- Arrival is awkward to verify manually because there is no developer-only short-flight option.
- Some connected Compose UI tests are still skipped by annotation.
- Room migrations are not formalized; clear app data after schema changes during development.
- Theme preference is not fully applied globally yet.

### Recommended Next Phase
- Add a developer-only short-flight/debug duration for fast Arrival verification.
- Replace skipped Compose tests with stable test-tag based UI tests.
- Harden active flight state across backgrounding/process death.
- Apply persisted theme mode globally.
- Add content descriptions and test tags to critical controls.

## 2026-05-20 - Stabilization and Usability Pass (Tasks 1–8)

### Summary
Completed the requested stabilization pass for real phone focus/study use: airport filtering, max-flight-time routing, realistic per-route timers end-to-end, seat availability, sticky primary actions, in-flight navigation rules, travel-day ritual (manifest → seats → boarding pass → gate call), focus objective/tag persistence, and notification foundation.

### What was fixed from Alex's nitpick list
- **Airports**: Route discovery uses `Airport.isUsableForTravelBlockRouting` (display/IATA code, ICAO, valid coordinates); repository filters at load; KOUN remains available.
- **Max flight time**: Duration chips are a ceiling; cards show each route's estimated minutes and buffer; booking/cockpit/rewards use route time; draft carries `maxFlightTimeMinutes`.
- **Seats**: Deterministic unavailable seats by booking seed; at least one free standard seat; unavailable seats not selectable.
- **Sticky buttons**: `StickyActionScaffold` / `StickyActionFooter` on Home, Book, boarding pass, gate call, Arrival.
- **Cockpit nav**: Cockpit hidden from normal bottom tabs; active-flight tabs are Cockpit/Logbook/Store/Settings; Home shows in-flight card; booking blocked while airborne.
- **Travel ritual**: Trip Manifest with optional objective, tags, security checklist, skip path; gate call transition; cockpit/arrival/logbook show focus cargo.
- **Notifications**: `NotificationHelper`, manifest permission, first-launch request, foreground landed alert; `docs/NOTIFICATION_PLAN.md` for background work.

### Files created or edited (high level)
- `app/src/main/java/com/travelblock/app/ui/components/TravelBlockComponents.kt` — sticky scaffold, boarding barcode
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`, `HomeViewModel.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/arrival/ArrivalScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/logbook/LogbookScreen.kt`, `FlightLogDetailViewModel.kt`, `LogbookUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `app/src/main/java/com/travelblock/app/util/NotificationHelper.kt`
- `app/src/main/java/com/travelblock/app/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/build.gradle.kts` — `0.13.0-stabilization` / `1250`
- `docs/NOTIFICATION_PLAN.md`
- `MANUAL_TEST_PLAN.md`, `RELEASE_READINESS.md`
- Tests: `TravelBlockNavigationTest.kt`, `HomeViewModelTest.kt` (draft duration), existing engine/booking tests

### Build/test commands and results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug lintDebug
```

- Result: **BUILD SUCCESSFUL**

### Manual test instructions
See `MANUAL_TEST_PLAN.md` — stabilization smoke flow (max time → book → manifest → seats → boarding → gate → cockpit → arrival/logbook → relaunch).

### Known bugs
- Active flight still in-memory only (process death loses in-flight state).
- Background landing notification not scheduled yet (foreground detection only).
- Room destructive migration enabled for dev schema changes.
- Compose UI instrumentation tests for Home/Book remain `@Ignore` for test-services harness.

### Not completed and why
- **WorkManager background notifications**: deferred to avoid destabilizing build; architecture documented in `docs/NOTIFICATION_PLAN.md`.
- **Active flight restore**: out of scope for this pass; requires persisted active-flight entity + resume UI.
- **Developer short-flight toggle**: not added; would help QA but not required for acceptance.

### Next recommended prompt/task
1. Persist and restore `ActiveFlight` across process death.
2. Schedule landing notification at `plannedArrivalAt` via WorkManager.
3. Add debug 2-minute flight option for arrival QA.
4. Re-enable Compose UI tests with stable test tags.

## 2026-05-20 - Travel Ritual Polish

### Summary
Polished the booking travel ritual for a more connected travel-day feel: compressed manifest, directional Compose transitions, physical boarding pass, immersive gate scan, and lightweight haptics—without changing the booking state machine or screen flow.

### What changed
- **Manifest compression**: Replaced separate FROM/TO cards + large flight summary with a single `CompactRouteDocket` line (e.g. `KOKC → KRQO · 5 min · 23 mi · TB422 · Gate O6`). Mission objective and cabin tags appear immediately under a compact check-in header. Security checks use a tight checklist card.
- **Travel-day motion**: `BookingFlowTransition` slides left/fade forward and right/fade backward between manifest and seats. `BookingRitualSurfaceTransition` animates check-in → boarding pass → gate call the same way.
- **Boarding pass**: `BoardingPassReveal` adds scale + slide; `BoardingPassCard` uses perforated divider, stronger elevation, barcode panel, objective/tag, and animated `READY TO BOARD` stamp.
- **Gate call**: Scan-line animation, boarding-group copy, cabin-doors copy, rows animate pending → active → complete; primary button disabled with `Preparing cabin…` until cleared, then `Start Focus Flight`.
- **Haptics**: `TravelBlockHaptics` + `rememberTravelBlockHaptics`; respects Settings `hapticsEnabled`; cues on seat select, boarding pass reveal, gate cleared, focus flight start.
- **Sticky CTA**: Increased `StickyActionFooterHeight` to 112dp so dual-button footers do not cover scroll content.

### Files created or edited
- `app/src/main/java/com/travelblock/app/ui/util/HapticFeedback.kt`
- `app/src/main/java/com/travelblock/app/ui/components/TravelRitual.kt`
- `app/src/main/java/com/travelblock/app/ui/components/TravelBlockComponents.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/TravelBlockApp.kt`
- `docs/TRAVEL_RITUAL.md`
- `MANUAL_TEST_PLAN.md`
- `AGENT_NOTES.md`

### Commands run and results

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug lintDebug
```

- Result: **BUILD SUCCESSFUL**

### Manual verification
See `MANUAL_TEST_PLAN.md` — Travel ritual polish checks (compact docket, boarding pass animation/stamp, gate sequence, sticky footer, haptics with toggle on in Settings).

## 2026-05-21 - Travel Ritual Immersion Tightening

### Summary
Tightened the Travel Ritual visual language so Book feels less like stacked generic Compose cards and more like a terminal-to-gate sequence.

### What changed
- Added `TravelDayBackdrop` with subtle runway/terminal linework behind the Book route.
- Reworked `BookingProgressRail` and `CompactRouteDocket` into dark terminal-board panels with amber status accents.
- Reworked manifest styling into a paper-like check-in document, renamed the objective field to focus cargo, and changed Security Check from checkbox rows into compact clearance tiles.
- Added a cabin-specific header before the seat map so Seat Selection feels like boarding the aircraft rather than another form section.
- Made the boarding pass more document-like with warm paper color, stronger shadow, subtle paper texture, passenger row, edge notches, and a dark scan panel.
- Rebuilt the gate sequence as a dark scanner console with grid/frame treatment, moving scan bar, gate status badge, and higher-contrast row states.
- Kept the existing booking state machine, haptic hooks, route data, sticky CTA behavior, and clean pre-flight vs dark Cockpit distinction.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/components/TravelRitual.kt`
- `app/src/main/java/com/travelblock/app/ui/components/TravelBlockComponents.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `docs/TRAVEL_RITUAL.md`
- `MANUAL_TEST_PLAN.md`
- `AGENT_NOTES.md`

### Verification
- `.\gradlew.bat test` — `BUILD SUCCESSFUL`

## 2026-05-21 - Travel Ritual De-Generic UI Pass

### Summary
Continued tightening the Book ritual after visual review showed several default Material/Compose surfaces still leaking through.

### What changed
- Replaced stock ritual footer `Button` / `OutlinedButton` controls with custom terminal-style primary and secondary action surfaces.
- Replaced the stock `OutlinedTextField` mission input with a document-style `FocusCargoField` that includes an aviation/form label and character count.
- Reworked the seat map from a light generic card into a dark cabin panel with an inner aircraft seating well, aisle labels, custom seat shapes, and dark-mode legend/status treatments.
- Kept Home clean and light, while making the Book ritual more visually distinct before handoff to the dark Cockpit.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `AGENT_NOTES.md`

### Verification
- `.\gradlew.bat test` — `BUILD SUCCESSFUL`
- `.\gradlew.bat assembleDebug` — `BUILD SUCCESSFUL`
- `.\gradlew.bat installDebug` — `BUILD SUCCESSFUL` on `emulator-5554`
- Emulator screenshots captured:
  - `app/build/reports/travel-ritual-manifest-v2.png`
  - `app/build/reports/travel-ritual-seat-v2.png`

## 2026-05-21 - Home Terminal and Seat Randomization Pass

### Summary
Improved two weak spots called out in visual review: the Home screen still looked too much like a generic Compose app, and seat availability looked too algorithmic.

### What changed
- Replaced Home's shared generic airport/route cards with Home-specific terminal components:
  - dark `Departure terminal` header with points manifest
  - warm-paper `Current gate` airport board
  - dark `Departure board` header
  - custom duration window chips
  - departure rows with dark metric strips and status badges
  - custom sticky `Book ... Flight` action surface
- Added a subtle Home terminal/runway backdrop to visually connect Home with the Book ritual.
- Replaced simple per-seat hash availability with deterministic per-flight cabin load generation:
  - row/zone load pressure
  - occasional adjacent pairs and clusters
  - different pressure for premium, extra-legroom, and rear cabin zones
  - guardrails to keep every row and both sides of the aisle usable
- Updated booking seat tests to assert behavior rather than the old fixed default seat.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookingViewModel.kt`
- `app/src/test/java/com/travelblock/app/ui/screen/book/BookingViewModelTest.kt`
- `AGENT_NOTES.md`

### Verification
- `.\gradlew.bat test` — `BUILD SUCCESSFUL`
- `.\gradlew.bat assembleDebug lintDebug installDebug` — `BUILD SUCCESSFUL`
- Emulator screenshots captured:
  - `app/build/reports/home-terminal-v3.png`
  - `app/build/reports/home-terminal-v3-loaded.png`
  - `app/build/reports/seat-randomized-v3.png`

## 2026-05-21 - Home Departure Batch Tightening

### Summary
Adjusted the Home screen after review showed the redesigned terminal surface made route choices feel buried, leaving the page reading like a single-airport card instead of a departure board.

### What changed
- Compressed the Home terminal header and current-gate board so departure options appear sooner.
- Changed Home route paging to load departures in batches of four, with additional airport departures revealed by scroll or the custom load-next-batch row.
- Added an explicit `visible/total` count to the departure board and copy that tells the user to scroll for the next batch.
- Replaced the generic load-more button with a custom terminal-style departure batch control.
- Kept the airport/current-gate context visible without letting it dominate the first viewport.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeUiState.kt`
- `AGENT_NOTES.md`
- `MANUAL_TEST_PLAN.md`

### Verification
- `.\gradlew.bat test assembleDebug lintDebug` — `BUILD SUCCESSFUL`

## 2026-05-21 - Home Terminal Interface Pass

### Summary
Rebuilt Home as a TravelBlock-specific airport terminal interface instead of stacked Compose-style cards with custom colors.

### What changed
- Replaced the top hero card with a compact `TerminalStatusHeader` showing TravelBlock, current airport, at-gate status, local time, and points.
- Reworked the current-airport area into `GateSignAirportPanel`, a flatter gate-sign layout with large airport code, ICAO, airport name, city/state, and status chips.
- Replaced the route-card list with a dark `DepartureBoard` monitor surface containing column headers, integrated max-time chips, compact rows, dividers, and loaded/total count.
- Reworked `DepartureBoardRow` so unselected rows stay compact and only the selected row reveals a `Proceed to Gate` detail area with route summary and reward.
- Added `TerminalActionFooter` copy with primary booking text plus secondary flight-time/reward context so the sticky CTA feels more integrated.
- Removed accidental runway/timeline background linework from Home and reduced palette noise around cream/orange surfaces.
- Kept existing Home route state, batching, route selection, and booking handoff logic intact.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `AGENT_NOTES.md`
- `MANUAL_TEST_PLAN.md`

### Verification
- `.\gradlew.bat test assembleDebug lintDebug` — `BUILD SUCCESSFUL`
- `.\gradlew.bat installDebug` — `BUILD SUCCESSFUL` on `emulator-5554`
- Emulator screenshot captured:
  - `app/build/reports/home-terminal-board-v4.png`

## 2026-05-21 - Clear-Sky Airline Visual System Pass

### Summary
Pivoted the normal app experience toward the new reference direction: clear-sky airline study app, with bright white/sky-blue screens and the cockpit remaining the dark focus-mode exception.

### Design note
TravelBlock visual direction: clear-sky airline study app. Normal screens use bright white/sky blue. Cockpit is the dark focus mode exception. Boarding pass uses ticket-paper styling.

### What changed
- Added brighter TravelBlock theme tokens for cloud white, sky blue, terminal blue, deep navy, runway amber, aluminum borders, ticket white, soft-sky selections, success, and muted text.
- Reworked Home into a reference-inspired departure dashboard:
  - sky-blue `DEPARTURES` hero with route/plane motif
  - compact next-flight panel inside the hero
  - compressed current-airport gate-sign strip
  - white/sky-blue departure board with duration controls and selected route expansion
  - integrated `Continue to Check-in` footer with route duration/reward context
- Reworked check-in/manifest toward the screenshot direction:
  - bright travel-day progress rail
  - blue departure docket
  - compact trip manifest header
  - lighter focus cargo field, cabin chips, and security tiles
  - compact secondary footer actions
- Reworked seat selection into a light cabin map with soft-blue seats, terminal-blue selected seats, gray unavailable seats, and amber only for premium/reward-style accents.
- Reworked boarding pass into a stronger ticket object:
  - blue TravelBlock Air route header
  - large route codes
  - ticket-white paper body
  - perforation/notches
  - printed barcode area
  - animated `READY TO BOARD` stamp
- Kept the gate scanner animation/sequence and haptic hooks intact, while keeping cockpit dark and visually distinct.
- Increased sticky footer scroll clearance so last content is not hidden under footer/nav.

### Files changed
- `app/src/main/java/com/travelblock/app/ui/theme/Color.kt`
- `app/src/main/java/com/travelblock/app/ui/theme/Theme.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/book/BookScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/components/TravelBlockComponents.kt`
- `app/src/main/java/com/travelblock/app/ui/components/TravelRitual.kt`
- `AGENT_NOTES.md`
- `MANUAL_TEST_PLAN.md`
- `RELEASE_READINESS.md`
- `docs/TRAVEL_RITUAL.md`

### Verification
- `.\gradlew.bat test assembleDebug lintDebug` — `BUILD SUCCESSFUL`
- `.\gradlew.bat installDebug` — `BUILD SUCCESSFUL` on `emulator-5554`
- Emulator screenshots captured:
  - `app/build/reports/clear-sky-home-loaded-v4.png`
  - `app/build/reports/clear-sky-book-v4.png`

### Still incomplete / next polish
- Home is much closer to the reference, but the departure board rows could still move further from table-like layout toward compact route tiles if we want even more screenshot parity.
- Manifest is visually aligned but still larger than the reference density on the emulator; another compression pass could reduce the progress rail/header footprint.
- Logbook, Store, Settings, Arrival, and Diversion were not fully redesigned in this pass.

## 2026-05-21 - Images 2.0 Demolition Plan

### UI pieces being replaced
- Replace the remaining table-like Home departure board with custom route tiles that feel closer to the reference: airport image panel, route arc, seat/reward/status data, and integrated CTA context.
- Replace the default Material `NavigationBar` with a custom TravelBlock dock for normal screens.
- Replace the current Cockpit/Focus Mode card stack with a dark immersive flight deck: window/route hero, large timer, curved dotted route progress, compact instruments, focus cargo, and a custom control tray.
- Tighten check-in, seat, boarding pass, and gate call around airline-specific surfaces so they read as check-in docket, cabin map, ticket/pass, and gate scanner rather than generic Compose panels.

### Logic that must not be touched
- Preserve airport filtering, max-flight-time routing, selected-route booking handoff, points, route distance/time/reward data, seat availability, manifest/objective/tag data, boarding pass fields, active-flight timer/progress, arrival/diversion summaries, logbook persistence, settings/persistence, and existing unit tests.
- Keep ViewModel/domain seams intact unless a UI-only label/derived display value is needed.

### New components to create or replace
- Create custom TravelBlock dock items to replace Material bottom navigation.
- Create route tile visuals with a skyline/photo-like panel, mini route arc, status badge, seats, distance, time, and reward.
- Create cockpit-specific components: in-flight hero/window, route progress canvas, instrument strip, focus cargo tray, and flight-deck control tray.
- Reuse/refine existing travel ritual components where they already support the flow, but avoid generic card-stack composition.

### How we know this is not just a recolor
- Home should no longer read as a data table or stacked generic cards; it must look like a departures dashboard with custom route tiles and an integrated check-in CTA.
- Cockpit must be structurally different from the old layout: full dark immersive focus scene, no normal bottom nav, no Material card pile as the primary composition.
- Normal screens must stay bright white/sky blue with amber limited to accents, while Focus Mode is the dark exception.
- Manual flow remains complete: Home -> route -> Check-in -> Seat -> Boarding Pass -> Gate -> Focus Mode -> Arrival/Diversion -> Logbook.
## 2026-05-21 - Images 2.0 UI Demolition Plan

- Replace: the default Material bottom navigation, stacked-card Home composition, generic booking card surfaces, current route-list/table treatment, basic seat-grid feeling, current boarding/gate presentation where it still reads like ordinary cards, and the old dark cockpit card stack.
- Do not touch: airport filtering, max-flight-time route generation, draft booking state, deterministic seat availability, manifest/objective/tag data, boarding pass fields, active-flight timer/progress, arrival/diversion persistence, logbook/points/settings repositories, or existing tests except where UI labels must follow the new product language.
- Create: custom TravelBlock dock, clear-sky departure dashboard pieces, integrated check-in route docket, cabin-map seat visuals, ticket-like boarding pass/stamp/scan pieces, gate-call scan sequence, and a separate dark flight-deck cockpit tray with an immersive in-flight hero.
- Proof this is not a recolor: normal screens should read as bright airline operations UI with branded departure, check-in, ticket, gate, and cabin artifacts; Focus Mode should drop the normal dock entirely and use a bespoke dark flight-deck composition with window/route/timer/progress controls instead of the previous stacked cards.

## 2026-05-21 - Images 2.0 Clear-Sky UI Rebuild

### What changed

- Replaced the default Material `NavigationBar` with a custom white TravelBlock dock using compact runway-style selected states.
- Removed the normal dock during active Focus Mode by excluding the cockpit route from `Scaffold` bottom navigation.
- Reworked Home departures away from the prior table/list structure into custom bright flight strips with destination block, flight-time, distance, reward, status, selected route line, and an integrated `Continue to Check-in` footer.
- Rebuilt the cockpit screen around a dark in-flight composition: cabin-window hero, large timer, animated cloud drift, curved dotted route progress with moving plane marker, origin/destination labels, flight instruments, focus cargo, and a separate flight-deck tray with divert controls.
- Removed the unused legacy `AirportCard`, `DurationChip`, and `RouteCard` helpers and converted the visible gate boarding sequence plus boarding-pass shell from Material `Card` to custom TravelBlock `Surface` treatment so the rebuilt flow is not carrying the old route-card/chip vocabulary forward.
- Preserved the existing ViewModel/domain boundaries: route filtering, booking draft creation, seat generation, boarding pass data, active-flight timer/progress, arrival/diversion callbacks, points/logbook/settings repositories, and tests were not moved into UI state hacks.

### Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew test
.\gradlew assembleDebug
.\gradlew lintDebug
```

- `.\gradlew test`: BUILD SUCCESSFUL
- `.\gradlew assembleDebug`: BUILD SUCCESSFUL
- `.\gradlew lintDebug`: BUILD SUCCESSFUL

### Emulator smoke

- Installed on attached `emulator-5554` with `.\gradlew installDebug`: BUILD SUCCESSFUL.
- Launched `com.travelblock.app/.MainActivity`.
- Verified Home UI tree shows `DEPARTURES`, `Available departures`, flight strip fields (`FLIGHT TIME`, `DISTANCE`, `REWARD`), `CONTINUE TO CHECK-IN`, and the custom dock labels.
- Drove Home -> Check-in -> Seat -> Boarding Pass -> Gate -> Focus Mode.
- Verified Check-in UI tree shows `CHECK-IN DESK`, `DEPARTURE DOCKET`, `TRIP MANIFEST`, focus tags, and security checks.
- Verified Seat UI tree shows `CABIN ASSIGNMENT`, `CABIN MAP`, seat IDs, and unavailable-seat semantics.
- Verified Boarding Pass UI tree shows `TRAVELBLOCK AIR`, airport codes, `SCAN AT GATE`, `READY TO BOARD`, and `BOARD FLIGHT`.
- Verified Gate UI tree shows `Gate C12 now boarding`, `BOARDING PASS SCAN`, `Scanning boarding pass`, `Securing cabin`, `Cleared for departure`, and `START FOCUS FLIGHT`.
- Verified Focus Mode UI tree shows `FOCUS FLIGHT ACTIVE`, route progress, large timer, `ETA`, origin/destination, compact instruments, and `FLIGHT DECK` tray; normal dock labels were absent in Focus Mode.
- Arrival was not waited manually because the selected route uses the real 21-minute timer and there is no short-flight QA toggle yet; arrival/diversion persistence remains covered by the existing unit test suite.

## 2026-05-21 - Competitive Flight Experience Overhaul

### What changed

- Added a reusable spatial `RoutePreviewMap` component that shows origin, destination, curved route line, moving plane marker, distance, flight time, and optional progress.
- Put the route map directly into Home route selection when a departure is selected, so choosing a flight is no longer just reading row metrics.
- Added the same route map to Check-in/Booking below the departure docket, making the route object visible before manifest, seat, boarding pass, and gate scan.
- Rebuilt the cockpit hero into a route/map + cabin-window hybrid: cabin window, large remaining timer, ETA, spatial route preview with live progress, traveled miles/time, remaining miles/time, focus cargo, and custom flight-deck tray.
- Strengthened arrival by showing the completed route map and explicitly handing off to the next departure board from the landed airport.
- Made the Arrival screen explicitly reflect the existing `currentAirportCode = destination.code` persistence path, so Continue Journey makes sense as the next departure board from the landed airport.
- Kept normal planning/booking/arrival screens in the existing bright clear-sky direction and kept cockpit as the dark immersive exception.

### Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

- `.\gradlew.bat test`: BUILD SUCCESSFUL
- `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL

### Still fails against FocusFlight baseline

- Route preview is still custom-drawn, not a real map provider with geography, terrain, airport pins, or zoom/pan.
- Sound/announcement work is still missing; gate scan and cockpit remain visual/haptic only.
- Boarding pass has reveal/stamp/scan treatment, but ticket-stub tear-off/drag interaction is not implemented yet.
- Cockpit progress is real timestamp-derived progress, but it does not yet have ambient audio, cabin announcements, or a takeoff/landing transition.
- Arrival suggests the next journey by moving current airport and returning to Home, but it does not yet preselect a recommended next route card.

## 2026-05-21 - Home Departures Rebuild Plan

### Home components being deleted/replaced

- Stop rendering the current `DepartureBoard` / `DepartureBoardRow` route-card layout on Home.
- Remove the selected-row expansion pattern that placed a small fake route preview inside the chosen route.
- Replace the hero-sized stacked airport/status card treatment with a compact airport/journey zone: header, points pill, and a clean gate tile.
- Replace the generic sticky action button slab with a terminal action footer that sits below the route explorer and does not cover departure rows.
- Keep only compact departure rows/chips under the route board: Destination | Time | Distance | Reward | Status.

### Files intentionally not touched

- Do not touch booking, cockpit, arrival, persistence repositories, route engine, seat logic, logbook, manifest, or business logic.
- Keep `HomeViewModel` behavior unless a compile-only display field is needed.
- Keep route selection, max-time filter, load-more behavior, and booking callback semantics intact.

### Avoiding the previous fake-map-card mistake

- The route visualization becomes the primary Route Explorer board for Home, not a small component nested inside a selected route row.
- Route rows stay compact and never expand into cards; selecting a row only updates the big board and CTA summary.
- The Home screen is rebuilt as two zones: airport/journey zone and route explorer zone.

## 2026-05-21 - Home Departures Route Board Rebuild

### Files changed

- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `AGENT_NOTES.md`

### What changed

- Replaced the rendered Home screen with a new two-zone composition:
  - compact airport/journey zone with TravelBlock header, points pill, and current-airport gate tile
  - route explorer zone with one large spatial route board, duration tokens, selected destination anchor, origin anchor, curved route, plane marker, and integrated route metrics
- Removed the previous rendered route-card/list path from Home:
  - no `DepartureBoard`
  - no `DepartureBoardRow`
  - no selected route expansion
  - no embedded mini `RoutePreviewMap` inside selected route rows
- Replaced large route cards with compact departure rows:
  - Destination | Time | Distance | Reward | Status
  - selecting a row updates the large board and terminal CTA summary
- Reworked the Home CTA as a custom terminal action footer with route summary and safe-area padding.
- Left booking, cockpit, arrival, persistence, route engine, logbook, seat logic, manifest, and business logic untouched.

### Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

- `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL
- `.\gradlew.bat test`: BUILD SUCCESSFUL
- Source audit:
  - `HomeScreen.kt` has no `DepartureBoard`, `DepartureBoardRow`, `SelectedDepartureDetail`, `FlightStripMetrics`, `FlightMetricPill`, or `RoutePreviewMap` references.
  - Navigation source has no Material `NavigationBar`, `BottomNavigation`, or `NavigationBarItem` references.

### Known issues

- This is still a custom-drawn route board, not a real map SDK.
- No emulator screenshot pass was run in this turn; verification is compile/test plus source audit.

## 2026-05-21 - Home Map Board Correction

### What changed

- Replaced the custom-drawn Compose route board with a WebView-backed Leaflet/OpenStreetMap Home map surface.
- Added real origin/destination latitude and longitude fields to `HomeRouteCardUiModel`, populated from existing airport route data in `HomeViewModel`.
- The selected compact departure row now updates the real map route using airport coordinates rather than updating a drawn fake route board.
- Added Android `INTERNET` permission so the Home WebView can load OpenStreetMap tiles.

### Files changed

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeUiState.kt`
- `app/src/main/java/com/travelblock/app/ui/screen/home/HomeViewModel.kt`
- `app/src/androidTest/java/com/travelblock/app/ui/screen/home/HomeScreenTest.kt`
- `AGENT_NOTES.md`

### Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

- `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL
- `.\gradlew.bat test`: BUILD SUCCESSFUL
- Source audit: `HomeScreen.kt` no longer has `Canvas`, `Path`, `drawLine`, `drawPath`, or `RouteBoardCanvas`; it uses `AndroidView` + `WebView` for the Home map surface.

### Known issues

- This depends on network tile loading; offline fallback is not built yet.
- This uses Leaflet inside WebView instead of a native Android map SDK.
- No emulator screenshot pass was run in this correction.

## 2026-05-21 - Home Map Eye Candy Escalation

### What changed

- Upgraded the Home Leaflet/OpenStreetMap route board with layered visual effects:
  - glass overlay
  - animated scanning light
  - vignette frame
  - HUD metric cards
  - pulsing airport marker rings
  - glowing route underlay
  - animated floating plane marker
- The HUD now displays selected destination, airport name, route block time, and reward directly over the map.
- Added HTML escaping for dynamic route labels before injecting them into the WebView map document.

### Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

- `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL
- `.\gradlew.bat test`: BUILD SUCCESSFUL

### Known issues

- Still not a native Android map SDK; it is a WebView-hosted Leaflet map.
- No emulator screenshot pass was run in this turn.
