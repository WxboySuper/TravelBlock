# TravelBlock Manual Test Plan

## Setup

- Install the debug build on an Android emulator or physical Android device.
- If testing persistence from a clean state, clear app storage before launch.
- If testing persistence across launches, do not clear app storage between steps.
- Recommended Windows setup:
  - Set `JAVA_HOME` to `C:\Program Files\Android\Android Studio\jbr`.
  - Use `.\gradlew.bat installDebug` to install the debug APK.
  - If `adb` is not on PATH, use `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.
- On Android 13+, accept the notification permission prompt if you want landing alerts while the app is open.

## Stabilization Smoke Flow (2026-05-20)

1. Launch the app.
   - Expected: Home opens without crashing; departure-board layout with current airport and max-time chips.

2. Confirm max flight time behavior.
   - Select `45m`.
   - Expected: flight strips show realistic flight times at or below 45 minutes (not all labeled 45m).
   - Expected: buffer line shows minutes under your limit when applicable.

3. Select a realistic route (e.g. ~37m if shown).
   - Tap `Continue to Check-in` (sticky footer visible without scrolling past all content).

4. Trip Manifest (optional).
   - Enter objective: `Test homework block.`
   - Select tag: `Homework`.
   - Optionally check security items, or tap `Skip manifest and choose seat`.

5. Seat selection.
   - Expected: some seats appear unavailable (muted); unavailable seats cannot be selected.
   - Select an available standard or upgrade seat.
   - Tap `Generate Boarding Pass`.

6. Boarding pass.
   - Expected: polished pass with route codes, seat, gate, boarding group, actual flight time, max window, objective/tag, decorative barcode.
   - Tap `Board Flight`.

7. Gate call.
   - Expected: brief boarding sequence, then `Start Focus Flight` in sticky footer.

8. Cockpit / in-flight nav.
   - Expected: Focus Mode opens without the normal TravelBlock dock; Home booking is blocked while airborne.
   - Expected: countdown uses the route's actual duration; focus cargo shows objective/tag if provided.
   - Booking from Home should be blocked while airborne.

9. Divert (optional).
   - Tap `Divert` in Flight Controls, confirm.
   - Expected: Diversion screen; Logbook shows diverted entry; current airport moves to diversion airport.

10. Complete another flight (optional).
    - Book a short route, let timer reach zero.
    - Expected: Arrival screen with focus cargo; notification may appear if permission granted and app was active at landing.
    - `Continue Journey` returns Home from new current airport.

11. Logbook.
    - Open a flight detail.
    - Expected: objective/tag visible when saved.

12. Restart app (persistence).
    - Force close and relaunch.
    - Expected: points, current airport, and logbook entries remain.

## Visual / UX Checks

- Primary actions stay visible in sticky footers on Home, Book, boarding pass, gate call, and Arrival.
- Bottom navigation does not cover primary buttons.
- Cockpit tab is not in normal pre-flight bottom nav.
- Empty departure state suggests trying a longer max flight time.
- Home should read as a clear-sky airline departure dashboard: bright header, compact current-airport/gate panel, white departure board, blue route accents, and compact route rows.
- Home should not feel like a single-airport page: the first loaded batch should show multiple airport departures, the board should show a loaded/total count, and scrolling should reveal more batches.
- The `LOAD NEXT DEPARTURE BATCH` control should appear when more airport departures are available and should add another batch without changing the selected duration.
- Home clear-sky terminal interface:
  - Expected: top area is a compact terminal status header, not a large hero card.
  - Expected: current airport panel reads like a bright gate sign with large airport code, ICAO, name/location, and compact status chips.
  - Expected: Available Departures is one white/sky-blue departure-board surface with header, max-time chips, column labels, and monitor-style rows.
  - Expected: route options are custom flight strips, not floating route cards or table rows.
  - Expected: selecting a row expands only that row with `Proceed to Gate`, route summary, placeholder gate assignment, and reward.
  - Expected: sticky footer shows booking action plus tiny flight-time/reward context and does not cover board rows or bottom nav.
  - Expected: no accidental vertical runway/timeline line appears behind chips or content.
- Seat availability should look like a real partially booked cabin, not a repeating row pattern; every row should retain usable seats on both sides of the aisle.

## Clear-Sky Visual Direction (2026-05-21)

Design note: TravelBlock visual direction is a clear-sky airline study app. Normal screens use bright white/sky blue. Cockpit is the dark focus mode exception. Boarding pass uses ticket-paper styling.

1. **Home dashboard**
   - Expected: Home feels bright, custom, and airline-study themed, not like stacked Material cards.
   - Expected: route selection still updates the row expansion and bottom action footer.
   - Expected: max-flight-time filtering still changes the available route set and keeps the loaded/total count honest.
   - Expected: the bottom action footer reads `Continue to Check-in` with route duration and reward context, and does not overlap bottom navigation.

2. **Check-in manifest**
   - Book a flight from Home.
   - Expected: route docket appears as a compact blue itinerary strip (`ORIGIN -> DEST · min · mi · flight · Gate`).
   - Expected: progress rail, focus cargo, cabin tags, and security tiles are light/sky-blue, not dark terminal slabs.
   - Expected: objective/tag/checklist state still works and remains visible with reduced scrolling.
   - Expected: primary footer reads `Check-in Complete`; secondary returns to departures.

3. **Seat cabin map**
   - Expected: seat selection is mostly light, with soft-blue seats, blue selected seat, gray unavailable seats, and amber used only for premium/upgrade emphasis.
   - Expected: unavailable seats remain unselectable and realistic.
   - Expected: `Generate Boarding Pass` stays reachable.

4. **Boarding pass animation**
   - After generating pass.
   - Expected: pass animates in with fade/slide/scale.
   - Expected: pass reads as a TravelBlock Air ticket object: white ticket surface, blue header, large route codes, punched/notched paper feel, perforation, barcode area, and `READY TO BOARD` stamp.
   - Expected: objective and tag appear when entered on manifest.

5. **Gate scan sequence**
   - Tap `Board Flight`.
   - Expected: gate tile and scanner area feel like a bright-to-blue boarding transition.
   - Expected: copy includes boarding group at gate and cabin-doors message.
   - Expected: three rows progress pending -> active -> complete.
   - Expected: primary button shows `Preparing cabin...` and is disabled until cleared, then `Start Focus Flight`.

6. **Cockpit contrast**
   - Start Focus Flight.
   - Expected: cockpit remains dark/deep navy and feels like leaving the bright terminal into focus mode.
   - Expected: route duration, objective/tag, and active-flight booking block still work.

7. **Haptics** (device with vibration; Settings -> Haptics on)
   - Select a seat: light tap.
   - Boarding pass appears: click-style pulse.
   - Gate clears: confirm-style pulse.
   - Start Focus Flight: stronger pulse.
   - Turn Haptics off in Settings and repeat: no vibration (app should not crash).

## Travel Ritual Polish (2026-05-20)

1. **Manifest compactness**
   - Book a flight from Home.
   - Expected: single route docket line at top (`ORIGIN → DEST · min · mi · flight · Gate`).
   - Expected: Book uses terminal/runway backdrop details instead of a plain generic app background.
   - Expected: progress rail and route docket read as bright airline-app panels with blue route accents.
   - Expected: focus objective appears as a document-style `FOCUS CARGO` field, not a stock Material text field.
   - Expected: mission objective field and cabin tag chips visible with minimal scrolling before security check.
   - Expected: security items appear as compact clearance tiles, not three tall separate blocks.

2. **Travel-day transitions**
   - Continue to seats, then back to manifest.
   - Expected: forward step slides/fades leftward; back step slides/fades rightward.
   - Expected: seat map appears as a light cabin-map panel with an aisle, row labels, and custom seat shapes, not a generic grid.
   - Expected: sticky footer actions use terminal-style custom surfaces, not stock Material buttons.
   - Generate boarding pass → Board Flight → gate.
   - Expected: each full-screen phase animates forward (not an instant cut).

3. **Boarding pass**
   - After generating pass.
   - Expected: card animates in (fade + slight scale/slide).
   - Expected: ticket-paper treatment with subtle texture, edge notches, dashed/perforated divider, stronger card shadow, barcode panel, `READY TO BOARD` stamp.
   - Expected: objective and tag shown when entered on manifest.

4. **Gate call**
   - Tap Board Flight.
   - Expected: bright-to-blue gate scanner console with scan line, scanner grid/frame, and gate status badge.
   - Expected: copy includes boarding group at gate and cabin-doors message.
   - Expected: three rows progress pending → active → complete.
   - Expected: primary button shows `Preparing cabin…` and is disabled until cleared, then `Start Focus Flight`.

5. **Sticky CTA**
   - On manifest, seats, boarding pass, and gate steps with two footer buttons.
   - Expected: last scroll item is not hidden under the sticky footer.

6. **Haptics** (device with vibration; Settings → Haptics on)
   - Select a seat: light tap.
   - Boarding pass appears: click-style pulse.
   - Gate clears: confirm-style pulse.
   - Start Focus Flight: stronger pulse.
   - Turn Haptics off in Settings and repeat: no vibration (app should not crash).

## Current Known Limits

- Active flight state is still in memory while flying (no process-death restore).
- Landing notifications fire when arrival is detected in foreground; background scheduling is documented in `docs/NOTIFICATION_PLAN.md`.
- Room uses destructive migration during development; clear app data after schema bumps if needed.
- No developer short-flight toggle yet for fast arrival QA.

## Images 2.0 Clear-Sky Rebuild Acceptance (2026-05-21)

1. **Home departures dashboard**
   - Expected: Home reads as a branded TravelBlock clear-sky departures dashboard, not stacked generic cards.
   - Expected: points, current airport/gate status, max-flight-time selector, route strips, realistic flight time, distance, reward, and selected route status are visible.
   - Expected: route selection changes the selected flight strip and the sticky CTA reads `Continue to Check-in` with a route summary.

2. **TravelBlock dock**
   - Expected: normal app screens use the custom white TravelBlock dock, not the default Material bottom navigation.
   - Expected: active Focus Mode does not show the normal dock.

3. **Check-in to gate**
   - Expected: Check-in keeps objective, tag, and security checklist in compact airline check-in styling with route docket.
   - Expected: seat selection reads as a light cabin map with aisle, row labels, blue selected seat, gray unavailable seats, soft blue standard seats, and amber only for premium/upgrades.
   - Expected: boarding pass reads as a TravelBlock Air ticket with route codes, gate/group/times, barcode/scan area, and READY TO BOARD stamp.
   - Expected: gate call shows gate, boarding group, scan motion, scanning/secure/cleared sequence, and then enables `Start Focus Flight`.

4. **Focus Mode**
   - Expected: cockpit is the dark immersive exception, with cabin-window hero, large timer, curved/dotted route progress, moving plane marker, origin/destination labels, focus cargo, compact instruments, and a custom flight-deck tray.
   - Expected: diversion and arrival still persist to Logbook through the existing cockpit flow.

## Commands (verification)

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
```

## Emulator Smoke Evidence (2026-05-21)

- Installed and launched debug build on attached `emulator-5554`.
- Drove Home -> Check-in -> Seat -> Boarding Pass -> Gate -> Focus Mode.
- UI tree evidence confirmed the redesigned Home departure strips, route docket, cabin assignment, boarding pass ticket fields, gate scan sequence, and dark flight-deck Focus Mode.
- Focus Mode showed `FOCUS FLIGHT ACTIVE`, route progress, large timer, instruments, and `FLIGHT DECK`; the normal TravelBlock dock was not present there.
- Arrival was not manually waited because the route used a real 21-minute timer; use existing unit tests for arrival persistence coverage until a short-flight QA toggle exists.
