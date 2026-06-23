# TravelBlock Release Readiness

Date: 2026-05-21 (clear-sky visual pass)

## Can Alex install/run on phone?

**Yes** — install `app/build/outputs/apk/debug/app-debug.apk` or run `.\gradlew.bat installDebug` with USB debugging enabled.

## Is it usable for a real focus block?

**Yes, with caveats.** The core loop is cohesive: max-time departure board → trip manifest → seats → boarding pass → gate call → cockpit → arrival/diversion → logbook. Use realistic route durations for actual study timers. Process death during an active flight is not restored yet.

## What features are currently safe?

| Area | Status |
|------|--------|
| Airport filtering (IATA/display + ICAO + coordinates) | Safe |
| Max flight time route discovery | Safe |
| Per-route realistic flight timer through booking/cockpit/rewards | Safe |
| Seat map with deterministic unavailable seats | Safe |
| Trip manifest / focus objective & tag | Safe (optional) |
| Boarding pass + gate call transition | Safe |
| Clear-sky Home / check-in / seat / pass visual direction | Safe |
| Custom TravelBlock dock on normal screens | Safe |
| Dark flight-deck Focus Mode without normal dock | Safe |
| Cockpit timer from timestamps | Safe while app stays alive |
| Arrival / diversion persistence | Safe |
| Logbook + detail with focus fields | Safe |
| Store / settings / airport picker | Safe |
| Foreground landing notification (permission granted) | Safe |

## What features are incomplete?

- Active flight restore after process kill or long backgrounding
- Background landing notification via WorkManager/AlarmManager
- Global theme application from settings
- Audio hooks
- Onboarding flow
- Seat upgrade point charging at booking confirm
- Return-home route builder
- Developer short-flight debug mode

## Biggest risks before continued development

1. **Timer loss on process death** — user backgrounds or OS kills app mid-flight.
2. **No background landing alert** — user may miss arrival if not in TravelBlock (plan in `docs/NOTIFICATION_PLAN.md`).
3. **Destructive Room migration** — schema changes wipe local data until migrations are formalized.
4. **Manual arrival QA is slow** — shortest routes still require real minutes.

## Build and test results (this pass)

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
```

- `.\gradlew.bat test`: **BUILD SUCCESSFUL**
- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat lintDebug`: **BUILD SUCCESSFUL**
- `.\gradlew.bat installDebug` on attached `emulator-5554`: **BUILD SUCCESSFUL**
- Package: `versionName = 0.13.0-stabilization`, `versionCode = 1250`

## Emulator smoke result

- Home -> Check-in -> Seat -> Boarding Pass -> Gate -> Focus Mode was driven on `emulator-5554`.
- UI tree evidence confirmed the clear-sky departures dashboard, custom dock on normal screens, route docket, cabin-map seat semantics, TravelBlock Air boarding pass, gate scan sequence, and dark undocked flight deck.
- Manual arrival was not waited because the selected route timer was 21 minutes and no short-flight QA toggle exists yet; arrival/diversion persistence remains covered by unit tests.

## Visual direction note

TravelBlock visual direction: clear-sky airline study app. Normal screens use bright white/sky blue. Cockpit is the dark focus mode exception. Boarding pass uses ticket-paper styling.

This pass moved Home away from table/list departures into custom flight strips, replaced the default bottom navigation with a TravelBlock dock, and rebuilt Focus Mode as an undocked dark flight deck with cabin-window hero, route arc, instruments, focus cargo, and flight controls. Check-in, seats, boarding pass, and gate remain on the bright airline ritual path. Cockpit intentionally remains dark so starting a focus flight feels like leaving the terminal.

Known visual follow-up: Logbook, Store, Settings, Arrival, and Diversion still need the same clear-sky component identity pass.

## Recommendation

Safe for Alex to run daily focus/study sessions on a physical phone as a **debug usability build**. Treat as pre-release: document flight outcomes in Logbook, avoid relying on mid-flight app kills, and grant notification permission if foreground landing alerts are wanted.
