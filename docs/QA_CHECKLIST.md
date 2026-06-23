# TravelBlock QA Checklist

## Smoke Test

- App launches.
- No crash on startup.
- Bottom navigation works.
- Back behavior is reasonable.
- App works on physical Android phone or emulator.

## Onboarding

- New install shows onboarding.
- User can search for KOUN.
- User can select home airport.
- User reaches Home after selection.
- Returning user skips onboarding.

## Home

- Current airport is visible.
- Duration chips change available departures.
- Route cards show destination, time, distance, reward, and label.
- Empty state appears if no routes exist.
- Stats are compact and accurate.

## Booking

- User can select route.
- User can select seat.
- Booking review displays correct route.
- Boarding pass displays correct fields.
- Start Flight enters Cockpit.

## Cockpit

- Timer counts down.
- Progress updates.
- Miles flown and remaining update.
- No normal pause button exists.
- Divert action asks for confirmation.
- Closing and reopening app preserves progress.

## Arrival

- Completed flight shows arrival screen.
- Flight saves to logbook.
- Points are awarded.
- Current airport updates to destination.
- Continue Journey works.
- Return Home works.

## Diversion

- Diversion confirmation appears.
- Diverted flight saves to logbook.
- Partial progress is recorded.
- Points rule is applied.
- User can return to Home.

## Logbook

- Completed flights display.
- Diverted flights display.
- Filters work.
- Detail screen opens.
- Summary stats are correct.
- Data persists after restart.

## Store and Rewards

- Points balance displays.
- Completed flight increases points.
- Purchase subtracts points.
- Transaction is recorded.
- Unlock persists after restart.
- Purchase fails gracefully if balance is too low.

## Settings

- Home airport can be edited.
- Sound toggle persists.
- Ambient toggle persists.
- Haptics toggle persists.
- Theme preference persists.
- Reset data requires confirmation.
- Reset data returns app to onboarding.

## Accessibility and UI

- Text is readable.
- Contrast is acceptable.
- Touch targets are large enough.
- Important icons have content descriptions.
- UI works on small Android screens.
- Cockpit is not visually overwhelming.
