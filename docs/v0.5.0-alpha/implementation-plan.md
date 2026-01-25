# v0.5.0-alpha Implementation Plan

**Goal**: The simulation logic

**Status**: Not Started

**Timeline**: January 19 - February 2 (14-15 days, school pace with async agent support)

---

## Overview

This is the most complex version yet. v0.5.0 implements the core flight simulation engine: a real-time cockpit timer where 1 second of user focus = 1 second of flight progress. Users select destinations from v0.4.0's time slider, start a timer, and watch their progress accumulate. Stopping the timer results in "diverting" (failing) the flight.

This version is split into **6 focused PRs** with heavy async agent support, allowing steady progress during the school year.

**Async Agent Critical**: PRs #2-4 are excellent candidates for async agent work. This allows you to handle architecture/testing while agents implement complex real-time logic.

---

## PR Breakdown

### PR #1: Cockpit Screen UI Layout 🎨

**Branch**: `feature/cockpit-screen-layout`

**Size**: Medium (~250-300 lines)

**Priority**: High (Visual foundation)

**Dependencies**: None (UI-only)

**Description**: Build the cockpit screen UI with all visual elements: timer display, progress indicators, distance tracking, and status information.

#### Tasks (PR #1)

- [ ] Create `screens/CockpitScreen.tsx`:
  - Large, readable timer display (MM:SS or MM:SS format)
  - Route information: Origin → Destination
  - Distance tracking: Miles Remaining / Miles Flown
  - Progress bar (visual representation of journey)
  - Altitude indicator (cosmetic, increases with time)
  - Status indicator (climbing, cruising, descending phases)
  - Start button (enabled when not in flight)
  - Emergency stop button (red, always visible during flight)
  - Map preview (small, will be integrated in v0.6.0)
- [ ] Create themed components:
  - `components/cockpit/TimerDisplay.tsx` - Large, animated number display
  - `components/cockpit/DistanceIndicator.tsx` - Miles flown/remaining
  - `components/cockpit/ProgressBar.tsx` - Visual journey progress
  - `components/cockpit/StatusBadge.tsx` - Flight phase indicator
- [ ] Implement animations:
  - Timer pulse effect every second
  - Progress bar smooth updates
  - Phase transitions (climbing → cruising → descending)
  - Smooth number animations for distance
- [ ] Use ThemedText, ThemedView, and existing design system
- [ ] Support dark/light mode (dark mode should look like "night flying")
- [ ] Add testID props for UI testing

#### Acceptance Criteria (PR #1)

- All UI elements display correctly
- Layout is clean and easy to read during focus session
- Animations are smooth and not distracting
- Dark mode looks appropriate for night flying
- Responsive design works on various screen sizes
- No console warnings
- Ready for integration with timer logic (PR #2)

#### Example Prompt for PR #1

```text
Create the cockpit screen UI for the flight simulator:
1. Build CockpitScreen with timer display (MM:SS)
2. Show route (Origin → Destination)
3. Display distance tracking (Miles Flown / Miles Remaining)
4. Add progress bar showing journey progress
5. Add altitude indicator and flight phase badge
6. Include Start button and red Emergency Stop button
7. Add map preview area (placeholder for v0.6.0)
8. Implement smooth animations for transitions
9. Support dark/light mode with night-flying aesthetic

Reference existing design system (ThemedText, ThemedView). Make it look like a real cockpit dashboard.
```

---

### PR #2: Real-Time Timer Engine ⏱️

**Branch**: `feature/timer-engine`

**Size**: Large (~300-400 lines)

**Priority**: High (Core mechanic)

**Dependencies**: v0.4.0 flight state

**Description**: Implement the real-time timer that drives the entire flight simulation. This is the heart of the app: managing time, calculating progress, and coordinating state updates.

#### Tasks (PR #2)

- [ ] Create `services/flightTimerService.ts` with:
  - `startFlight(flightState: FlightState): Promise<void>` - Begin timer
  - `pauseFlight(): void` - NOT a pause; this is "divert" (fail state)
  - `resumeFlight(): void` - Resume after accidentally stopping (recovery window)
  - `stopFlight(): void` - Emergency stop (full divert)
  - `updateFlightState(): void` - Internal: update progress each tick
  - `getFlightProgress(): FlightProgress` - Current status
  - Event emitters for: tick, phase-change, arrival, divert
- [ ] Implement timer mechanics:
  - Use `react-native` `setInterval` or `react-native-timer` library
  - Tick every 100ms for smooth updates (not every second; improves smoothness)
  - Calculate progress as elapsed time / total flight time
  - Update distance flown based on progress percentage
  - Handle app backgrounding (pause timer, resume when foreground)
- [ ] Create TypeScript types in `types/flight.ts`:
  - `FlightState` - Home, destination, total flight time
  - `FlightProgress` - Elapsed time, distance flown, remaining
  - `FlightPhase` enum (climbing, cruising, descending)
- [ ] Implement divert logic:
  - User stops timer → flight diverts (fails)
  - Option to resume within 30-second window before full fail
  - After 30 seconds, flight marked failed and discarded
- [ ] Add error handling:
  - Invalid flight state
  - Timer already running
  - Mathematical edge cases (division by zero, negative times)
- [ ] Add comprehensive tests in `__tests__/services/flightTimerService.test.ts`
  - Test timer starts/stops correctly
  - Test progress calculations
  - Test phase transitions
  - Test divert logic
  - Test backgrounding/foregrounding

#### Acceptance Criteria (PR #2)

- Timer runs reliably for entire flight duration
- Progress calculations are accurate
- Divert mechanics work as intended
- App backgrounding/foregrounding handled correctly
- All edge cases tested
- No memory leaks (timers cleaned up)
- 95%+ test coverage
- TypeScript strict mode passes

#### Example Prompt for PR #2 (Async Agent - HIGHLY RECOMMENDED)

```text
Implement the core flight timer engine:
1. Create flightTimerService that manages real-time flight progress
2. Implement startFlight, pauseFlight (divert), resumeFlight, stopFlight
3. Use setInterval with 100ms ticks for smooth progress updates
4. Calculate flight progress: elapsed time / total flight time
5. Update distance flown: (progress %) × total distance
6. Implement divert logic: stopping timer triggers fail state with 30s recovery window
7. Handle app backgrounding (pause timer, resume when foreground)
8. Emit events: tick, phase-change, arrival, divert
9. Write comprehensive tests for all scenarios

Ensure accurate calculations, smooth updates, and proper state management.
```

---

### PR #3: Distance & Progress Tracking 📊

**Branch**: `feature/distance-progress-tracking`

**Size**: Medium (~200-250 lines)

**Priority**: High (Critical for display)

**Dependencies**: PR #2, v0.2.0 distance calculations

**Description**: Implement the logic that calculates and tracks distance flown, remaining distance, and provides the data for UI display and analytics.

#### Tasks (PR #3)

- [ ] Create `services/progressCalculator.ts` with:
  - `calculateDistanceFlown(progress: number, totalDistance: number): number` - Distance covered
  - `calculateDistanceRemaining(progress: number, totalDistance: number): number` - Distance left
  - `calculateProgressPercentage(elapsed: number, total: number): number` - Journey progress %
  - `getFlightPhase(elapsed: number, total: number): FlightPhase` - Climbing/Cruising/Descending
  - `calculateETA(remaining: number, avgSpeed: number): number` - Time to arrival
  - `calculateAverageSpeed(distance: number, time: number): number` - Actual speed
- [ ] Implement flight phases:
  - Climbing (0-10% of flight time): altitude increases, speed ramping
  - Cruising (10-80% of flight time): constant altitude and speed
  - Descending (80-100% of flight time): altitude decreases, speed reduces
- [ ] Create distance unit conversion:
  - Display in miles or km (user preference from v0.3.0 context)
  - Accurate conversions and rounding
- [ ] Add comprehensive tests in `__tests__/services/progressCalculator.test.ts`
  - Test distance calculations
  - Test phase transitions
  - Test edge cases (very short/long flights)
  - Test unit conversions
- [ ] Add JSDoc comments with examples

#### Acceptance Criteria (PR #3)

- All calculations are accurate to 99%+
- Phase transitions occur at correct times
- Distance tracking matches real flight routes (verified against v0.2.0)
- Unit conversions are correct
- 100% test coverage
- TypeScript strict mode passes

#### Example Prompt for PR #3

```text
Implement distance and progress tracking:
1. Create progressCalculator service for real-time calculations
2. Implement calculateDistanceFlown and calculateDistanceRemaining
3. Calculate progress percentage (elapsed / total time)
4. Implement flight phases: climbing (0-10%), cruising (10-80%), descending (80-100%)
5. Add phase-based indicator updates (altitude, speed changes)
6. Calculate ETA based on current speed and remaining distance
7. Support distance unit conversion (miles ↔ km)
8. Write comprehensive tests for all calculations

Ensure mathematical accuracy and realistic flight simulation.
```

---

### PR #4: Flight State Management & Integration 🔄

**Branch**: `feature/flight-state-management`

**Size**: Medium (~250-300 lines)

**Priority**: High (Connects everything)

**Dependencies**: PR #2, PR #3

**Description**: Integrate timer and progress tracking into app-wide state management, ensure data flows correctly to UI, and handle complex state transitions.

#### Tasks (PR #4)

- [ ] Create or enhance `store/flightStore.ts` (or context):
  - State: `currentFlight` (active flight state)
  - State: `flightProgress` (real-time progress)
  - State: `flightStatus` (running, paused, diverted, landed)
  - Actions: `startFlight()`, `pauseFlight()`, `resumeFlight()`, `stopFlight()`, `handleArrival()`
- [ ] Create `hooks/useFlightSimulation.ts`:
  - Hook that provides flight state and controls
  - Handles timer subscription/unsubscription
  - Re-renders component on timer ticks
  - Cleanup on unmount (stop timer if active)
- [ ] Implement state persistence:
  - Save in-flight state to AsyncStorage in case of app crash
  - Resume flight if app was closed mid-flight (recovery)
- [ ] Add error boundaries:
  - Catch errors during flight
  - Gracefully handle state corruption
  - Provide user feedback
- [ ] Create integration tests in `__tests__/integration/flightSimulation.test.ts`
  - Test full flight lifecycle: start → progress → arrival
  - Test divert and recovery flow
  - Test app backgrounding and recovery
  - Test error scenarios

#### Acceptance Criteria (PR #4)

- State flows correctly from timer to UI
- UI updates smoothly on every tick
- State persists correctly across app restarts
- Error scenarios are handled gracefully
- Recovery mechanics work as expected
- No memory leaks or lingering timers
- 90%+ integration test coverage
- TypeScript strict mode passes

#### Example Prompt for PR #4 (Async Agent - RECOMMENDED)

```text
Implement flight state management and integration:
1. Create/enhance flight state store with currentFlight, flightProgress, flightStatus
2. Implement actions: startFlight, pauseFlight (divert), resumeFlight, stopFlight, handleArrival
3. Create useFlightSimulation hook for component consumption
4. Subscribe to timer events and update state on ticks
5. Persist state to AsyncStorage for crash recovery
6. Implement recovery flow: resume flight if app was closed mid-flight
7. Add error boundaries and graceful error handling
8. Write integration tests for full flight lifecycle

Ensure smooth state flow and proper cleanup on component unmount.
```

---

### PR #5: Arrival & Landed State Transition 🛬

**Branch**: `feature/arrival-handling`

**Size**: Medium (~200-250 lines)

**Priority**: High (End-of-flight logic)

**Dependencies**: PR #2, PR #3, PR #4

**Description**: Implement the logic that detects when a flight arrives at its destination and transitions to a "landed" state, preparing for v0.8.0's post-flight flows.

#### Tasks (PR #5)

- [ ] Create `services/arrivalService.ts` with:
  - `handleArrival(flightState: FlightState): ArrivaleResult` - Process arrival
  - `createFlightLog(flight: CompletedFlight): void` - Log flight for v0.9.0
  - `triggerArrivalSequence(): void` - Trigger arrival animations/sounds
  - `transitionToLanded(destination: Airport): void` - Move to landed state
- [ ] Implement arrival detection:
  - Monitor progress; when >= 100%, trigger arrival
  - Small buffer window (100.1% to handle floating point)
  - Validate arrival hasn't been triggered already (prevent double-trigger)
- [ ] Create arrival effects:
  - Stop timer (flight is complete)
  - Show arrival confirmation
  - Trigger arrival sound (from v0.7.0 audio service)
  - Calculate final stats (actual distance, actual time, speed)
  - Store completed flight for logbook (v0.9.0)
- [ ] Create TypeScript types:
  - `CompletedFlight` - Flight record for logbook
  - `ArrivalResult` - Outcome data
- [ ] Add comprehensive tests in `__tests__/services/arrivalService.test.ts`
  - Test arrival detection at 100% progress
  - Test edge cases (99.9%, 100.1%)
  - Test flight logging
  - Test state transitions

#### Acceptance Criteria (PR #5)

- Arrival is detected reliably at 100% progress
- Arrival sequence executes smoothly (sound, UI transition)
- Completed flight data is logged correctly for v0.9.0
- No double-triggers or edge case issues
- 95%+ test coverage
- TypeScript strict mode passes
- Ready to integrate with v0.8.0's post-flight flows

#### Example Prompt for PR #5

```text
Implement flight arrival and landed state:
1. Create arrivalService that detects and handles arrival
2. Monitor flight progress; trigger arrival when >= 100%
3. Handle edge cases (floating point, double-trigger prevention)
4. Create flight log entry for logbook (v0.9.0)
5. Trigger arrival sound (integrate with v0.7.0 audio)
6. Calculate final stats (distance, time, average speed)
7. Transition to landed state
8. Write comprehensive tests for arrival detection

Prepare data structure for v0.9.0 flight logging and v0.8.0 post-flight flows.
```

---

### PR #6: Cockpit Screen Integration & Full Flight Experience 🚀

**Branch**: `feature/cockpit-integration`

**Size**: Medium (~250-300 lines)

**Priority**: High (Complete experience)

**Dependencies**: PR #1-5

**Description**: Wire everything together: connect CockpitScreen to all services, implement UI updates, handle user interactions, and create a smooth flight experience.

#### Tasks (PR #6)

- [ ] Integrate CockpitScreen with flight state:
  - Subscribe to flight progress updates
  - Update timer display every tick
  - Update distance indicators in real-time
  - Update progress bar
  - Update phase indicators
- [ ] Implement user interactions:
  - Start button: initiate flight from selected destination
  - Stop button: emergency stop (divert flight)
  - Resume button: appear if flight diverted (30s window)
  - Handle back/navigation during flight (prevent accidental exit)
- [ ] Add visual feedback:
  - Timer pulses/highlights on each tick
  - Distance updates animate smoothly
  - Phase transitions have visual cues
  - Arrival triggers screen transition animation
- [ ] Add haptic feedback:
  - Tick feedback (subtle vibration each second)
  - Phase change feedback
  - Arrival feedback (strong haptic burst)
- [ ] Implement navigation:
  - CockpitScreen appears after starting from time slider
  - Prevent back navigation during active flight (modal-like)
  - On arrival, transition to landed screen (placeholder for v0.8.0)
- [ ] Add error handling:
  - Invalid flight state (shouldn't happen, but handle gracefully)
  - Timer failure (fall back to local state)
  - App backgrounding/foregrounding
- [ ] Add performance monitoring:
  - Log frame drops
  - Monitor battery usage
  - Log any lag during flight
- [ ] Add UI tests with testID props

#### Acceptance Criteria (PR #6)

- Cockpit screen updates smoothly at 60fps during flight
- All user interactions work correctly
- Haptic feedback is appropriate and non-intrusive
- Navigation prevents accidental exits
- Error states are handled gracefully
- App backgrounding/foregrounding works without issues
- Arrival transitions smoothly to landed state
- Performance is acceptable on device
- No console warnings or errors
- Ready for v0.6.0 (map integration)

#### Example Prompt for PR #6 (Async Agent)

```text
Integrate the cockpit screen with the flight simulation:
1. Connect CockpitScreen to flight state (progress, timer, distance)
2. Implement real-time UI updates from timer ticks
3. Add smooth animations for progress bar and distance updates
4. Implement Start button (initiate flight) and Stop button (divert)
5. Add Resume option within 30s of diverted flight
6. Implement haptic feedback (tick, phase change, arrival)
7. Handle app backgrounding/foregrounding
8. Prevent back navigation during active flight
9. Transition to landed state on arrival
10. Add error handling and recovery

Ensure smooth 60fps experience and proper state management.
```

---

## Testing Strategy

### Unit Tests

- Timer calculations (PR #2)
- Distance/progress calculations (PR #3)
- Arrival detection (PR #5)

### Integration Tests

- Full flight lifecycle: start → progress → arrival (PR #6)
- App backgrounding and recovery (PR #4)
- Divert and recovery flow (PR #2, #4)
- State persistence (PR #4)

### Manual Testing Checklist

- [ ] Start a flight and watch timer count down correctly
- [ ] Verify distance flown increases smoothly
- [ ] Check phase transitions (climbing → cruising → descending)
- [ ] Stop flight mid-flight; verify divert state
- [ ] Resume flight within 30 seconds; verify it resumes
- [ ] Let flight complete; verify arrival state
- [ ] Close and reopen app mid-flight; verify recovery
- [ ] Test various flight durations (30m, 2h, 5h)
- [ ] Verify haptic feedback on device
- [ ] Test on different screen sizes
- [ ] Monitor battery/CPU usage during long flights
- [ ] Test with poor network connection (shouldn't affect)
- [ ] Verify dark/light mode during flight

---

## Timeline Estimate

- **PR #1** (Cockpit UI): 1-1.5 days — Standard development
- **PR #2** (Timer engine): 1.5-2 days — **Async agent strongly recommended**
- **PR #3** (Progress tracking): 1 day — Standard or async
- **PR #4** (State management): 1-1.5 days — **Async agent recommended**
- **PR #5** (Arrival handling): 1 day — Standard development
- **PR #6** (Integration): 1-1.5 days — Can use async for most

**Total**: ~7-9 days with standard development, **~4-5 days with async agents** handling PRs #2-4

**Recommended Approach for Jan 19-Feb 2 Timeline**:

- Assign PRs #2-3 to async agent on Jan 19-20 (immediately after v0.4.0 completes)
- Work on PR #1 (UI) while agents implement complex logic
- Review PR #2-3 results on Jan 22-24
- Have agents start PR #4 on Jan 23-24
- Work on PRs #5-6 while awaiting reviews
- Final integration by Feb 1, buffer day for fixes

---

## Notes

- This is the most complex version so far; async agent support is critical
- Timer reliability is essential; test thoroughly on actual device
- State management patterns established here will be reused for post-flight and analytics
- Consider adding telemetry to understand app usage patterns
- Battery usage is important for long flights; monitor and optimize if needed
- Ensure timer continues ticking even if app goes to background (critical for real app)

---

## Success Criteria for v0.5.0-alpha

✅ Timer runs reliably for entire flight duration

✅ Real-time distance/progress updates are smooth (60fps)

✅ Divert mechanics work correctly with 30-second recovery

✅ App backgrounding/foregrounding handled transparently

✅ Arrival detected reliably and transitions smoothly

✅ Flight data logged for future analytics

✅ Cockpit experience feels immersive and responsive

✅ No memory leaks or timer issues

✅ Ready to integrate with v0.6.0 (map visualization)

---

## Integration Points

- ⬅️ **v0.4.0**: Consumes flight state (destination, time)
- 📍 **v0.5.0**: Core simulation engine
- ➡️ **v0.6.0**: Adds map visualization to cockpit
- ➡️ **v0.7.0**: Adds audio cues to flight events
- ➡️ **v0.8.0**: Consumes arrival data for post-flight flows
- ➡️ **v0.9.0**: Consumes flight logs for logbook

---

## Async Coding Agent Opportunities

**Highly suitable versions for async agent work:**

1. **PR #2 (Timer Engine)** — Complex logic but well-defined requirements
2. **PR #3 (Progress Tracking)** — Math-heavy; agent excels at calculations
3. **PR #4 (State Management)** — Integration work; good for agents
4. **PR #6 (Integration)** — UI/integration layer; agents can handle

**Recommended Setup**:

- Send PR #2 to agent immediately after v0.4.0 (Jan 19-20)
- While agent works on #2, you handle PR #1 (UI)
- Queue PRs #3-4 for agent on Jan 23-24
- You focus on PRs #5-6 while agents iterate
- Review and integrate by Jan 31

**Expected Time Savings**: 5-6 hours with agent handling implementation

---

### Last Updated

January 4, 2026

### Author

Development Team
