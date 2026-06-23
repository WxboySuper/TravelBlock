# TravelBlock Reference Summary

## What the Existing App Proved

The existing Expo/React Native version proved that TravelBlock has a strong core idea:

- Focus sessions can be framed as simulated flights.
- Home airport selection gives the app a personal starting point.
- Destination discovery based on focus duration is the key mechanic.
- Booking, seat selection, boarding pass, cockpit timer, and diversion flow create a coherent aviation loop.
- Android-first development is the practical path for Alex.

The public README describes the current app as an Expo Router app for a flight-simulator-style focus loop with home airport, flight duration, route booking, boarding, cockpit view, and diversion support. It also says onboarding, home airport persistence, destination discovery, booking flow, and cockpit simulation are implemented, while logbook/history, analytics, audio immersion, and rewards remain upcoming.

## What Should Survive

Keep these concepts:

- Home airport setup.
- Current airport state.
- Airport search.
- Duration-based destination discovery.
- Booking flow.
- Seat selection as a lightweight ritual.
- Boarding pass before starting focus.
- Cockpit timer.
- Diversion instead of pause.
- Post-flight continuation idea.
- Logbook as proof of completed focus.
- Points and cosmetics as secondary reward loop.

## What Should Be Redesigned

### Home screen

The current UI direction is clean, but too sparse. Home should not be mostly home base, rewards, and a generic CTA. It should immediately show available departures.

### Rewards placement

Rewards matter, but should not dominate the first screen before the user understands the flight loop.

### Navigation

The app needs navigation that reflects the product:

- Home
- Book
- Logbook
- Store
- Settings

### Visual identity

Avoid full neon cockpit styling across the whole app. Use a cleaner travel-app style for normal screens, and reserve darker immersive styling for Cockpit.

## What Should Be Dropped or Deferred

Drop for v1:

- Expo/EAS dependency.
- Cross-platform assumptions.
- Real flight APIs.
- Real weather APIs.
- Accounts.
- Cloud sync.
- iOS.
- Real-money purchases.
- Complex transport modes beyond flight.

## Important Product Lesson

TravelBlock is not a timer app with aviation decoration.

TravelBlock is a travel simulation wrapped around focus. The route engine is the soul.

## Native Rebuild Intent

The native Android rebuild should use Kotlin and Jetpack Compose. The old app should not be migrated file-by-file. Concepts should be rebuilt in native Android patterns.

## Risk Areas

- Agents may overbuild UI before route logic works.
- Agents may create a generic Pomodoro app.
- Agents may make Home a stats dashboard.
- Agents may use an in-memory timer instead of timestamp truth.
- Agents may add unnecessary cloud/network features.
- Agents may make the app too neon and less travel-themed.

## Best Next Step

Create the native Android shell, then implement airport and route logic before deep UI polish.
