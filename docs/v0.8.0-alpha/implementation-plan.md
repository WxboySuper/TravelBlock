# v0.8.0-alpha Implementation Plan

**Goal**: What happens after you land

**Status**: Not Started

**Timeline**: February 28 - March 14 (14 days, ends before spring break)

---

## Overview

v0.8.0 implements post-flight flows. After landing, users see a summary screen and choose between continuing their journey or returning home. This sets up the foundation for repeated play sessions and the logbook feature in v0.9.0.

This version is split into **4 focused PRs** with moderate async agent support.

---

## PR Breakdown

### PR #1: Landed Screen UI 🛬

**Branch**: `feature/landed-screen`

**Size**: Medium (~200-250 lines)

**Priority**: High (Visual foundation)

**Dependencies**: v0.5.0 arrival data

**Description**: Build the post-flight summary screen showing where the user landed and flight statistics.

#### Tasks (PR #1)

- [ ] Create `screens/LandedScreen.tsx`:
  - Display arrival airport (name, city, country, ICAO/IATA)
  - Show flight statistics:
    - Total distance traveled
    - Total flight time
    - Average speed
    - Altitude reached (cosmetic: max altitude during flight)
  - Display a map preview of the route completed
  - "You've arrived!" celebration message/animation
  - Next action buttons (Continue Journey, Return Home)
- [ ] Create themed components:
  - `components/landed/AirportSummary.tsx` - Arrival airport info
  - `components/landed/FlightStats.tsx` - Statistics display
  - `components/landed/RoutePreview.tsx` - Small map showing route
- [ ] Implement celebration animation:
  - Confetti effect or similar celebration
  - Smooth transition from cockpit screen
  - Optional sound effect (landing chime from v0.7.0)
- [ ] Add ThemedText, ThemedView styling
- [ ] Support dark/light mode
- [ ] Add testID props for testing

#### Acceptance Criteria (PR #1)

- Landed screen displays correctly after flight completion
- Statistics are accurate and well-formatted
- Celebration animation is appropriate (not excessive)
- Navigation buttons are clear and accessible
- Dark/light mode both look good
- Route preview maps displays correctly

#### Example Prompt for PR #1

```text
Create the post-flight landing screen:
1. Build LandedScreen showing arrival airport details
2. Display flight statistics (distance, time, speed, altitude)
3. Show small map preview of completed route
4. Add celebration animation (confetti or similar)
5. Add "You've arrived!" messaging
6. Include Continue Journey and Return Home buttons
7. Style with ThemedText/ThemedView
8. Support dark/light mode

Make it celebratory but not excessive.
```

---

### PR #2: Continue Journey Flow ⛓️

**Branch**: `feature/continue-journey`

**Size**: Medium (~150-200 lines)

**Priority**: High (Core mechanic)

**Dependencies**: PR #1, v0.3.0/v0.4.0 state management

**Description**: Implement the "Continue Journey" option: set current landed airport as new origin and return to time slider.

#### Tasks (PR #2)

- [ ] Update flight state management:
  - Create new function: `setOriginAndReturn(airport: Airport): void`
  - Update home context: current airport becomes new temporary origin
  - Store for session (don't change saved home airport)
- [ ] Implement navigation flow:
  - User taps "Continue Journey"
  - Confirmation dialog (optional): "Depart from [Airport]?"
  - Update flight state with new origin
  - Navigate back to TimeSliderScreen
- [ ] Update home airport display:
  - Show current location as origin (visually different from home airport)
  - Include indicator that this is "current location" not "home"
- [ ] Handle edge cases:
  - Landed at unknown airport (shouldn't happen; fallback to home)
  - No airports within reach from current location (show error)
- [ ] Add TypeScript types:
  - `ContinueJourneyState` interface
  - Origin type: home vs current location

#### Acceptance Criteria (PR #2)

- User can continue journey from landed airport
- New time slider selection starts from correct origin
- State management correctly tracks current vs home airport
- Edge cases handled gracefully
- Flow is intuitive and smooth
- No data loss or state corruption

#### Example Prompt for PR #2

```text
Implement continue journey feature:
1. Create function to set landed airport as new origin
2. Update flight state management (origin vs home distinction)
3. Implement navigation: Landed Screen → Time Slider (new origin)
4. Add confirmation dialog before departure
5. Show current location vs home airport visually differently
6. Handle edge cases (unknown airport, no reachable destinations)
7. Ensure state doesn't corrupt home airport setting

Allow users to chain multiple flights together.
```

---

### PR #3: Return Home Flow 🏠

**Branch**: `feature/return-home`

**Size**: Small-Medium (~150-200 lines)

**Priority**: High (Core mechanic)

**Dependencies**: PR #1, v0.3.0 home airport state

**Description**: Implement the "Return Home" option: auto-select home airport as destination and navigate to time slider.

#### Tasks (PR #3)

- [ ] Implement return home logic:
  - Get current landed airport
  - Get saved home airport
  - Calculate distance between them
  - Estimate flight time
  - Pre-select home airport as destination
- [ ] Create navigation flow:
  - User taps "Return Home"
  - Optional: show "Return to [Home Airport]?" confirmation
  - Navigate to TimeSliderScreen with home airport pre-selected
  - Show distance and estimated time
- [ ] Add pre-flight UI state:
  - TimeSliderScreen should show destination pre-selected
  - Slider can be adjusted if desired (different focus time)
  - Clear indication of current origin and pre-selected destination
- [ ] Handle edge cases:
  - Home airport not set (shouldn't reach here; fallback or error)
  - Very long distance home (show warning?)
  - No route home (shouldn't happen; show error)

#### Acceptance Criteria (PR #3)

- User can return to home airport with one tap
- Home airport is correctly pre-selected
- Flight time estimate is accurate
- Navigation flow is smooth
- Edge cases handled appropriately

#### Example Prompt for PR #3

```text
Implement return home feature:
1. Create logic to calculate return journey to home airport
2. Pre-select home airport as destination
3. Implement navigation: Landed Screen → Time Slider (home preset)
4. Show distance and estimated time to home
5. Allow user to adjust focus time before departure
6. Add optional confirmation dialog
7. Handle edge cases (home not set, no route)

Ensure smooth return journey setup.
```

---

### PR #4: Post-Flight State Management & Navigation 🔄

**Branch**: `feature/post-flight-integration`

**Size**: Medium (~150-200 lines)

**Priority**: High (Integration)

**Dependencies**: PR #1-3, v0.5.0 arrival handling

**Description**: Integrate post-flight flows with app navigation, ensure proper state transitions, and handle continuous play sessions.

#### Tasks (PR #4)

- [ ] Create post-flight state context:
  - Store `postFlightState`: { origin, destination, flightData }
  - Provide context/hooks for post-flight screens
  - Manage transitions between flights
- [ ] Implement navigation architecture:
  - CockpitScreen → LandedScreen (on arrival)
  - LandedScreen → TimeSliderScreen (continue or return)
  - Handle navigation stack properly (no back button during flight)
- [ ] Create state persistence:
  - Save completed flight data for logbook (v0.9.0)
  - Store in list of completed flights
  - Prepare data structure for analytics
- [ ] Implement session management:
  - Session can have multiple flights (continue journey)
  - Session tracking for analytics
  - Session data passed to logbook
- [ ] Add error handling:
  - Navigation errors
  - State corruption
  - Invalid transitions
- [ ] Add integration tests:
  - Full flight → landing → continue journey → new flight
  - Full flight → landing → return home → new flight
  - App backgrounding during post-flight flow

#### Acceptance Criteria (PR #4)

- Post-flight flows integrate cleanly with app navigation
- Continuous play sessions work correctly
- State is properly maintained across flights
- Completed flights are recorded correctly for logbook
- Navigation prevents accidental exits
- Error states are handled gracefully
- Ready for v0.9.0 logbook integration

#### Example Prompt for PR #4 (Async Agent)

```text
Integrate post-flight flows and state management:
1. Create post-flight state context for navigation
2. Implement proper navigation architecture
3. Wire up continue journey and return home flows
4. Store completed flight data for logbook
5. Implement session tracking (multiple flights per session)
6. Add state persistence for crash recovery
7. Handle app backgrounding during post-flight
8. Write integration tests for multi-flight sessions

Ensure smooth transitions and proper data flow for logbook integration.
```

---

## Testing Strategy

### Unit Tests

- Distance calculations for return journey
- State transitions

### Integration Tests

- Full flight → landing → continue journey → new flight
- Full flight → landing → return home → new flight
- Multi-flight session persistence
- Navigation stack management

### Manual Testing Checklist

- [ ] Landed screen displays after flight completion
- [ ] Statistics are accurate
- [ ] Continue Journey: new origin is set correctly
- [ ] Return Home: home airport is pre-selected
- [ ] Can chain multiple flights together
- [ ] Navigation doesn't have back button issues
- [ ] State persists correctly across flights
- [ ] Completed flights are recorded
- [ ] Dark/light mode work on all screens
- [ ] Edge cases handled (no home, very long distance, etc.)

---

## Timeline Estimate

- **PR #1** (Landed screen): 1-1.5 days
- **PR #2** (Continue journey): 1 day
- **PR #3** (Return home): 0.5-1 day
- **PR #4** (Integration): 1-1.5 days

**Total**: ~4-5 days

**Recommended Approach**:

- Start PR #1 on Feb 28 (after v0.7.0)
- Queue PR #4 for async agent by Mar 3-4
- Work on PRs #2-3 while agent handles integration
- Final integration by Mar 12, buffer day for testing before spring break

---

## Notes

- Keep post-flight flow simple and not overwhelming
- Players should feel encouraged to start another flight (loop back to time slider)
- Session concept important for future analytics
- State management here sets pattern for v0.9.0 logbook

---

## Success Criteria for v0.8.0-alpha

✅ Landed screen displays flight summary correctly

✅ Continue Journey allows chaining flights

✅ Return Home simplifies going back

✅ Post-flight state management works reliably

✅ Completed flights recorded for logbook

✅ Navigation is smooth and intuitive

✅ Ready for v0.9.0 logbook integration

---

## Integration Points

- ⬅️ **v0.5.0**: Provides arrival data and landing trigger
- ⬅️ **v0.7.0**: Audio cues for landing
- 📍 **v0.8.0**: Post-flight flows and state management
- ➡️ **v0.9.0**: Consumes completed flight logs
- ➡️ **v0.10.0**: Uses post-flight state for analytics

---

### Last Updated

January 4, 2026

### Author

Development Team
