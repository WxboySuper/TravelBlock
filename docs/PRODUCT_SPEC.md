# TravelBlock Product Spec

## One-Sentence Pitch

TravelBlock is an Android-first airline-themed focus app where every focus session becomes a simulated flight from one airport to another.

## Core Promise

The app should make focus time feel like travel progress.

A user should not feel like they are starting a generic timer. They should feel like they are checking available departures, booking a route, taking off, staying focused during the flight, landing, and building a personal logbook of focus journeys.

## Target User

TravelBlock is for people who want a focus timer that feels playful, goal-driven, and less sterile than a Pomodoro app. It is especially suited to users who enjoy maps, travel, aviation, collecting progress, and lightweight simulation.

## Platform Scope

v1.0 is Android-only.

Reasons:

- The primary developer has Android hardware.
- The primary developer uses Windows.
- iOS development and testing are not practical right now.
- Native Android avoids the Expo/React Native friction from the prototype.

## Product Loop

The v1.0 loop is:

1. User starts at a current airport.
2. User chooses a focus duration.
3. App shows reachable destinations.
4. User selects a route.
5. User selects a seat and generates a boarding pass.
6. User starts the flight.
7. During focus, the cockpit shows time, route progress, miles flown, and miles remaining.
8. User either lands or diverts.
9. App saves the result to the logbook.
10. App updates points and current-airport state.
11. Available routes change based on the new current airport.

## v1.0 Principles

### Route-first

The main screen should prioritize available routes. Stats are useful, but secondary.

### Local-first

The app should work without an account, server, or network dependency.

### Android-first

The app should feel native on Android, with good touch targets, readable typography, edge-to-edge layout, and predictable Android behavior.

### Travel app, not cockpit everywhere

Home and booking should feel like a clean airline/travel app. Cockpit mode can be darker and more immersive.

### Timer as simulation state

The timer should be derived from timestamps, not a fragile in-memory countdown.

### No punishment-heavy design

Diversion should feel like a flight event, not a shame screen. The app should encourage focus without being cruel.

## v1.0 Feature Scope

### Included

- Onboarding
- Home airport selection
- Current airport state
- Airport search
- Reachable destination discovery
- Booking flow
- Seat selection MVP
- Boarding pass
- Active cockpit
- Resilient timer
- Arrival flow
- Diversion flow
- Logbook
- Points rewards
- Store for point-based cosmetics
- Settings
- Local persistence

### Excluded

- Accounts
- Cloud sync
- iOS
- Web
- Real flight APIs
- Real weather APIs
- Real-money purchases
- Social features
- Complex aircraft simulation
- Live air traffic
- Push notifications unless absolutely needed after core loop stabilizes

## Key Product Decisions

### Home is a departure board

The old home screen idea of showing home base and rewards is not enough. Home should answer:

- Where am I?
- What flights can I take from here?
- How long do they require?
- What will I earn?

### Current airport matters

After a completed flight, the user should be located at the destination. This makes the app feel like a journey instead of isolated timers.

### Return Home matters

Because current airport changes, the app should support a clean return-home path.

### Airport availability matters

The app's differentiator is believable route availability. Short sessions should not offer cross-country routes. Longer sessions should unlock farther airports.

### Rewards support the loop, not replace it

Rewards should add retention and charm, but the core reward is the journey itself.

## Success Criteria for v1.0

TravelBlock v1.0 is successful if:

- A new user can set a home airport and understand the concept quickly.
- Home immediately shows useful route options.
- The user can book and complete a focus flight without confusion.
- The active flight survives app closure and restart.
- Arrival and diversion both save correctly.
- Logbook makes past focus feel visible and satisfying.
- The app feels like a real travel app, not a generic timer with airport labels.
