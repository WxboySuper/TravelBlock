# TravelBlock UX Spec

## UX North Star

TravelBlock should feel like an airline app for focus sessions.

The user is not managing a timer. The user is checking departures, boarding, flying, landing, and collecting a travel history.

## Visual Direction

### Normal app screens

Home, Book, Logbook, Store, and Settings should feel like a clean travel app:

- Calm backgrounds
- Soft cards
- Clear route cards
- Airport board language
- Crisp typography
- Restrained blues, slate, white, warm amber accents
- Optional dark theme, but not neon-heavy

### Cockpit screen

Cockpit should feel distinct:

- Darker background
- Larger timer
- Route arc or map
- Less clutter
- Ambient/focus controls
- Divert as a secondary action
- No normal pause button

The app should have visual contrast between travel planning and active focus.

## Navigation Model

v1.0 bottom navigation:

- Home
- Book
- Logbook
- Store
- Settings

Cockpit and boarding pass can be full-screen flows launched from Booking.

## Screen Specs

### Onboarding

Purpose: Explain the app and set the home airport.

Content:

- App name and short explanation
- A simple statement: focus sessions become flights
- Home airport picker
- Confirmation step

UX requirements:

- Search must be forgiving.
- KOUN should be easy to find.
- Do not overload the user with rewards, stores, or logbook before first setup.

### Home

Purpose: Show current location and immediately surface routes.

Main sections:

1. Top app bar
   - TravelBlock
   - Points pill
   - Settings access if not in bottom nav

2. Current airport card
   - Code
   - Name
   - City/state/country
   - Current status: At Gate
   - Optional small chips: Home Base, Current Airport, Return Available

3. Duration chips
   - 15m
   - 25m
   - 45m
   - 60m
   - Custom

4. Available Departures
   - Destination cards
   - Destination code and city
   - Estimated focus time
   - Distance
   - Points reward
   - Availability label

5. Compact stats row
   - Flights
   - Focus time
   - Miles
   - Streak or points

6. Continue Journey / Return Home
   - Show only when relevant

Important: The Home screen should not be mostly stats.

### Book

Purpose: Let the user deliberately plan a flight.

Content:

- Origin airport
- Duration selector
- Reachable airports list
- Route details
- Seat selection
- Booking review
- Boarding pass generation

UX requirements:

- Route cards should show why they are available.
- Too-far destinations can be hinted at, but should not dominate.
- The primary CTA should be clear and contextual.

### Boarding Pass

Purpose: Create a small ritual before focus begins.

Content:

- Passenger label
- Flight number
- Origin and destination
- Seat
- Gate
- Boarding group
- Departure and arrival time
- Focus duration
- Start Flight button

UX requirements:

- Should feel satisfying and polished.
- Should not delay the user with too many taps.

### Cockpit

Purpose: Keep the user in the focus session.

Content:

- Countdown timer
- Origin and destination
- Route progress
- Miles flown
- Miles remaining
- Estimated arrival
- Ambient mode state
- Divert action

UX requirements:

- No normal pause button.
- Divert requires confirmation.
- It should be legible from a glance.
- It should not be visually overwhelming.

### Arrival

Purpose: Celebrate completion and move the journey forward.

Content:

- Arrived at destination
- Focus time
- Distance flown
- Points earned
- Seat/ticket info
- Continue Journey
- Return Home
- View Logbook

UX requirements:

- Completion should feel satisfying.
- Current airport updates to destination.

### Diversion

Purpose: End early without breaking the simulation.

Content:

- Confirm diversion
- Show partial progress
- Save as diverted
- Explain reduced or no reward
- Return to Home or Logbook

UX requirements:

- Do not frame as failure.
- Make it feel like an aviation event.

### Logbook

Purpose: Make focus history visible.

Content:

- Flight list
- Completed/diverted status
- Date
- Origin/destination
- Duration
- Distance
- Points
- Detail screen
- Summary stats

UX requirements:

- Empty state should encourage first flight.
- Past flights should feel collectible.

### Store

Purpose: Let points buy cosmetics.

Content:

- Points balance
- Boarding pass themes
- Seat classes
- Aircraft icons
- Transaction history link or section

UX requirements:

- No real money.
- Keep store secondary.

### Settings

Purpose: Control preferences and data.

Content:

- Home airport
- Theme
- Sound
- Ambient mode
- Haptics
- Reset data
- About

UX requirements:

- Reset data requires confirmation.
- Settings should not be visually more important than the core loop.

## Android UI Requirements

- Use Material 3 patterns where practical.
- Use safe edge-to-edge layout.
- Respect system back behavior.
- Use accessible touch targets.
- Add content descriptions for icons.
- Use readable text sizes.
- Avoid tiny low-contrast labels.
- Design for Alex's actual Android phone first, then generalize.

## Current UI Lesson

The existing UI is clean and approachable, but too sparse. It communicates home base and rewards, but not enough of the core loop.

The main improvement is not simply making it darker. The main improvement is making route availability visible immediately.

The ideal Home screen should feel like a living departure board.
