# TravelBlock UI Lessons

## Current UI Strengths

The current UI direction is approachable and clean.

Strengths:

- It feels more like a real app than a flashy prototype.
- The light travel-app direction is promising.
- Home base is clear.
- Points are visible.
- The primary action is obvious.
- The UI is not buried under neon effects.

## Current UI Weaknesses

The current home screen is too quiet.

It mostly says:

- Here is your home base.
- Here are your points.
- Start a new journey.

But TravelBlock should say:

- You are at this airport.
- These routes are available now.
- Pick a focus duration.
- Book the flight.
- Continue your journey.

## Main Design Lesson

The app does not need to become darker to become better.

It needs to become more route-first.

## Home Screen Direction

Home should become a departure board.

Recommended structure:

1. Header
   - TravelBlock
   - greeting or pilot label
   - points pill

2. Current airport card
   - airport code
   - airport name
   - city/region
   - At Gate status
   - edit option if needed

3. Duration chips
   - 15m
   - 25m
   - 45m
   - 60m
   - custom

4. Available Departures
   - route cards visible directly on Home

5. Compact stats
   - flights
   - focus time
   - miles
   - streak or points

6. Journey action
   - Continue Journey
   - Return Home
   - Book selected route

## Route Card Requirements

Each route card should show:

- Destination code
- Destination city/name
- Duration
- Distance
- Reward
- Label, such as Best Match or Short Hop
- Clear selected state

Route cards should be tappable and should lead into booking review or route details.

## Visual Style Direction

Use a restrained travel theme:

- Light or soft neutral backgrounds for normal screens
- Navy, slate, white, and soft blue
- Amber for reward or gate details
- Green/teal for available/ready states
- Airport-board typography details in small doses
- Rounded cards, but not everything needs to be a giant card

Avoid:

- Neon overload
- Cyberpunk HUD everywhere
- Giant empty stats cards
- Tiny gray text
- Overly decorative cockpit visuals outside Cockpit

## Cockpit Direction

Cockpit can be special:

- Darker
- Immersive
- Calmer
- Large timer
- Route arc or map
- Miles flown/remaining
- Estimated arrival
- Divert secondary action

Cockpit should feel like focus mode, not a dashboard.

## Generated Image Takeaways

The generated three-screen concept had useful structure:

- Home showed available departures.
- Booking emphasized reachable airports.
- Cockpit focused on the live session.

But the visual style should be softened for the actual app. Keep the route-first thinking, not the full neon look.

## Android Feel

Use native Android patterns:

- Material 3 top app bars where helpful
- Bottom navigation
- Modal bottom sheets for small selections
- Large touch targets
- Proper back behavior
- Edge-to-edge layout
- Content descriptions

## Design North Star

Home is a terminal.

Book is a departure board.

Boarding pass is the ritual.

Cockpit is the focus chamber.

Logbook is the passport.

Store is airline perks.

Settings are cabin controls.
