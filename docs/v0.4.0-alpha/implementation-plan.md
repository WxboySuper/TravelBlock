# v0.4.0-alpha Implementation Plan

**Goal**: Converting focus time into destinations

**Status**: Not Started

**Timeline**: January 13-18 (5 days to completion before school starts Jan 19)

---

## Overview

This version focuses on building the core mechanics that transform user-selected focus time into a dynamic list of reachable airports. The Time Slider is the primary UI element users will interact with, and it must be intuitive, responsive, and tightly integrated with distance calculations from v0.2.0.

This version is split into **5 focused PRs** that can be developed with async agent support, allowing you to complete before school starts.

---

## PR Breakdown

### PR #1: Custom Slider Component 🎚️

**Branch**: `feature/time-slider-component`

**Size**: Medium (~250-350 lines)

**Priority**: High (Foundation for all other work)

**Dependencies**: None (UI-only, no backend)

**Description**: Build a custom, highly responsive slider component specifically designed for time selection (30m to 5h range) with 10-minute snap intervals.

#### Tasks (PR #1)

- [ ] Create `components/time-slider/TimeSlider.tsx`:
  - Range: 30 minutes to 5 hours (convert to consistent units: 1800-18000 seconds)
  - Snap to 10-minute intervals (600-second increments)
  - Visual feedback during drag (highlight, value display)
  - Smooth animations for snap behavior
  - Touch-friendly (larger hit area)
- [ ] Implement `components/time-slider/TimeSliderThumb.tsx`:
  - Custom drag handle
  - Haptic feedback on drag start/end
  - Visual indicators for current value
- [ ] Create `components/time-slider/TimeValue.tsx`:
  - Display human-readable time (e.g., "1h 23m")
  - Update in real-time as slider moves
  - Animated number transitions
- [ ] Use `react-native-gesture-handler` for smooth gestures
- [ ] Use `react-native-reanimated` for high-performance animations
- [ ] Add ThemedText and ThemedView styling
- [ ] Support both light and dark mode
- [ ] Add testID props for automated testing
- [ ] Add comprehensive JSDoc comments

#### Acceptance Criteria (PR #1)

- Slider is responsive and smooth (60fps on device)
- Snap intervals work correctly (10-minute snaps)
- Value updates display instantly without lag
- Haptic feedback works on device
- Dark/light mode both look correct
- Gesture handling is natural and intuitive
- TypeScript strict mode passes
- No console warnings

#### Example Prompt for PR #1 (Async Agent)

```text
Create a custom time slider component for selecting focus durations:
1. Implement TimeSlider component with 30min-5hr range
2. Add 10-minute snap intervals using Reanimated
3. Create TimeValue display showing "Xh YYm" format
4. Add haptic feedback on drag events
5. Use gesture-handler for smooth touch interactions
6. Style with ThemedText/ThemedView for dark/light mode
7. Ensure 60fps smooth animations
8. Add testID props for testing

Reference react-native-reanimated for animation patterns and react-native-gesture-handler for gestures.
```

---

### PR #2: Snap-to-Interval Logic ⏱️

**Branch**: `feature/slider-snap-logic`

**Size**: Small (~100-150 lines)

**Priority**: High (Core mechanic)

**Dependencies**: PR #1

**Description**: Implement the mathematical logic that snaps slider values to 10-minute intervals and provides utility functions for time value manipulation.

#### Tasks (PR #2)

- [ ] Create `utils/timeSlider.ts` with:
  - `snapToInterval(value: number, interval: number): number` - Generic snap function
  - `formatTimeValue(seconds: number): string` - Convert seconds to "Xh YYm" format
  - `parseTimeValue(formatted: string): number` - Parse formatted time back to seconds
  - `getTimeInRange(value: number, min: number, max: number): number` - Constrain within bounds
  - `secondsToMinutes(seconds: number): number`
  - `minutesToSeconds(minutes: number): number`
- [ ] Create TypeScript types in `types/time.ts`:
  - `TimeValue` interface
  - `TimeRange` interface
  - `TimeUnit` enum (seconds, minutes, hours)
- [ ] Add comprehensive unit tests in `__tests__/utils/timeSlider.test.ts`
  - Test snap accuracy
  - Test time formatting and parsing
  - Test edge cases (zero, negative, max values)
  - Test rounding behavior
- [ ] Add JSDoc comments with examples

#### Acceptance Criteria (PR #2)

- All snapping calculations are accurate
- Time formatting is consistent and readable
- Unit tests achieve 100% coverage
- No floating-point rounding errors
- TypeScript strict mode passes
- Ready to integrate with PR #3

#### Example Prompt for PR #2

```text
Implement time slider utility functions:
1. Create snap-to-interval logic for 10-minute intervals
2. Implement time formatting (seconds → "Xh YYm")
3. Implement time parsing (formatted string → seconds)
4. Handle edge cases and boundary conditions
5. Write comprehensive unit tests with 100% coverage
6. Add TypeScript interfaces for time values

Ensure accuracy and consistency across all conversions.
```

---

### PR #3: Radius Filtering Logic 📍

**Branch**: `feature/radius-filtering`

**Size**: Medium (~200-250 lines)

**Priority**: High (Critical mechanic)

**Dependencies**: PR #2, v0.2.0's AirportService and distance calculations

**Description**: Implement the algorithm that maps slider time values to reachable airports based on flight distance and time.

#### Tasks (PR #3)

- [ ] Create `services/radiusService.ts` with:
  - `getDestinationsInTimeRange(origin: Airport, timeInSeconds: number, tolerance?: number): Airport[]`
    - Convert time to estimated flight distance
    - Use average cruise speed (~450 mph) to estimate reachable airports
    - Filter all airports within +/-tolerance (default 5%)
  - `calculateMaxDistance(timeInSeconds: number): number` - Time → distance conversion
  - `getDestinationsByFlightTime(origin: Airport, maxFlightTime: number): Airport[]`
  - `estimateFlightTime(distance: number): number` - Distance → time conversion
- [ ] Integrate with `AirportService` from v0.2.0:
  - Use `getAirportsWithinDistance()` for efficient filtering
  - Handle edge cases (no airports, too short/long flights)
- [ ] Create TypeScript types in `types/radius.ts`:
  - `RadiusFilter` interface
  - `FlightEstimate` interface
- [ ] Add comprehensive unit tests in `__tests__/services/radiusService.test.ts`
  - Test various time ranges (30m, 1h, 3h, 5h)
  - Test distance calculations (Manhattan to LA = ~2.5h, etc.)
  - Test edge cases (very close airports, no destinations)
  - Verify accuracy within 5-10% of real flight times
- [ ] Add performance notes in comments (optimization opportunities)

#### Acceptance Criteria (PR #3)

- Destinations are filtered correctly based on time
- Distance-to-time conversions are realistic (~450 mph cruise)
- All tests pass with 100% coverage
- Performance is acceptable (filtering <100ms for typical queries)
- Integrates seamlessly with v0.2.0 AirportService
- TypeScript strict mode passes

#### Assumptions & Notes

- Average cruise speed: 450 mph (common for commercial flights)
- Tolerance: Allow ±5% variation (user might fly faster/slower)
- Include takeoff/landing time: ~20-30 minutes total (add to calculations)

#### Example Prompt for PR #3 (Async Agent)

```text
Implement radius filtering to map time to reachable airports:
1. Create radiusService with getDestinationsInTimeRange function
2. Convert focus time to estimated flight distance (450 mph cruise speed)
3. Use AirportService to filter airports within distance
4. Handle takeoff/landing time (~20-30 min total)
5. Implement estimateFlightTime for distance-to-time conversion
6. Add tolerance for variation (±5%)
7. Write tests covering various time ranges and edge cases
8. Optimize for performance (filter in <100ms)

Ensure realistic flight time estimates and integrate with v0.2.0 services.
```

---

### PR #4: Available Destinations Display 🛫

**Branch**: `feature/destinations-list`

**Size**: Medium (~250-300 lines)

**Priority**: High (User-facing feature)

**Dependencies**: PR #1, PR #3

**Description**: Build the UI components that display available destinations based on the current slider position, including filtering, sorting, and dynamic updates.

#### Tasks (PR #4)

- [ ] Create `components/destinations/DestinationsList.tsx`:
  - FlatList of available airports
  - Display: Airport name, city, country
  - Display: Distance and estimated flight time
  - Touch feedback and navigation
  - Loading state while calculating
  - Empty state when no destinations
  - Virtualized for performance
- [ ] Create `components/destinations/DestinationCard.tsx`:
  - Individual destination card
  - Show airport info (ICAO, IATA)
  - Show distance in miles/km
  - Show flight time
  - Styled with theme support
  - Touchable with haptic feedback
- [ ] Implement sorting:
  - Default: by distance (closest first)
  - Option: by name
  - Option: by time
- [ ] Create `hooks/useDestinations.ts`:
  - Hook that wraps radius service
  - Manages loading and error states
  - Memoizes results appropriately
  - Refetch on time changes
- [ ] Add performance optimizations:
  - Memoize list items
  - Lazy render
  - Efficient re-renders on slider changes
- [ ] Add testID props for UI testing

#### Acceptance Criteria (PR #4)

- List updates smoothly as slider moves
- Destinations display correct distance/time information
- Empty state shows helpful message
- Performance is smooth (60fps scrolling)
- Dark/light mode both work
- Touch interactions feel responsive
- Ready to integrate into main app flow

#### Example Prompt for PR #4 (Async Agent)

```text
Create the available destinations list UI:
1. Build DestinationsList component using FlatList
2. Create DestinationCard for individual airports
3. Display: name, city, distance, flight time
4. Add sorting options (distance, name, time)
5. Create useDestinations hook for state management
6. Implement loading and empty states
7. Optimize list rendering with memoization
8. Add smooth animations for list updates
9. Support dark/light mode

Integrate with radiusService from PR #3 and ensure smooth 60fps scrolling.
```

---

### PR #5: Time Slider Integration & Full Feature Integration 🔌

**Branch**: `feature/time-slider-integration`

**Size**: Medium (~200-250 lines)

**Priority**: High (Complete feature)

**Dependencies**: PR #1-4

**Description**: Integrate all slider components, connect to home airport, and create the main time-slider screen that brings everything together.

#### Tasks (PR #5)

- [ ] Create `screens/TimeSliderScreen.tsx`:
  - Display current home airport (from v0.3.0)
  - TimeSlider component
  - Available destinations list below slider
  - Selection flow: choose destination and proceed
  - Loading states and error handling
- [ ] Implement selection flow:
  - User selects destination
  - Confirm selection
  - Store selected destination and time for v0.5.0 (Flight Engine)
  - Navigate to next screen (pending implementation)
- [ ] Create app state management:
  - Create `store/flightStore.ts` or `context/FlightContext.tsx`
  - Store: home airport, selected destination, flight time
  - Persist to AsyncStorage for recovery
  - Provide hooks: `useFlightState()`, `setDestination()`, etc.
- [ ] Add error handling:
  - No home airport set (should not happen; handled in v0.3.0)
  - No destinations found
  - Network errors
- [ ] Integrate navigation:
  - Route from home → time slider screen
  - After selection → pending v0.5.0 cockpit screen
  - Add back navigation
- [ ] Add performance monitoring:
  - Log time to first destination display
  - Monitor re-render frequency
  - Add comments about optimization opportunities
- [ ] Add UI tests with testID props

#### Acceptance Criteria (PR #5)

- Time slider screen displays correctly
- Slider updates destination list smoothly
- Destination selection works without errors
- Selected flight data is stored and accessible for v0.5.0
- Error states are handled gracefully
- Navigation flows are smooth
- TypeScript strict mode passes
- Ready to integrate with v0.5.0 Flight Engine
- App state is persisted and restored correctly

#### Example Prompt for PR #5 (Async Agent)

```text
Integrate the time slider into the main app:
1. Create TimeSliderScreen combining all components
2. Display home airport and available destinations
3. Implement selection flow and confirmation
4. Create flight state management (home airport, destination, time)
5. Persist selected flight data to AsyncStorage
6. Add error handling for edge cases
7. Implement navigation to/from time slider screen
8. Ensure smooth performance with memoization

Connect everything from PRs #1-4 and prepare state for v0.5.0 Flight Engine.
```

---

## Testing Strategy

### Unit Tests

- Time snap calculations (PR #2)
- Radius filtering logic (PR #3)
- Time formatting/parsing (PR #2)

### Integration Tests

- Slider → Destinations list sync (PR #4, #5)
- Selection → State management (PR #5)
- Navigation flow (PR #5)

### Manual Testing Checklist

- [ ] Drag slider smoothly across entire range (30m to 5h)
- [ ] Verify destinations update as slider moves
- [ ] Test snap behavior (should feel responsive)
- [ ] Verify haptic feedback works on device
- [ ] Test with no home airport (should show error/redirect)
- [ ] Test with very few destinations (e.g., 30m range)
- [ ] Test with many destinations (e.g., 5h range)
- [ ] Verify dark/light mode rendering
- [ ] Test on different screen sizes
- [ ] Verify selection saves state correctly
- [ ] Test app restart with saved flight state

---

## Timeline Estimate

- **PR #1** (Slider component): 1-1.5 days — Can use async agent
- **PR #2** (Snap logic): 0.5 days — Async agent recommended
- **PR #3** (Radius filtering): 1-1.5 days — **Async agent strongly recommended** (complex logic)
- **PR #4** (Destinations list): 1-1.5 days — Can use async agent for component
- **PR #5** (Integration): 1 day — You should handle for architecture/review

**Total**: ~5-6 days with standard development, **~3-4 days with async agents** handling PRs #1-4

**Recommended Approach for Jan 13-18 Timeline**:

- Assign PRs #1-3 to async agent on Jan 13-14
- Review and test during Jan 15-16
- Handle PR #4 on Jan 16-17 (or have agent do it, you review)
- Complete PR #5 integration on Jan 17-18
- Buffer day on Jan 18 for final testing before school

---

## Notes

- All components should integrate with v0.3.0's home airport selection
- State created in PR #5 will be critical for v0.5.0 Flight Engine
- Distance calculations from v0.2.0 are assumed working and tested
- Performance is critical: slider must feel responsive at 60fps
- Consider adding analytics to track which time ranges are most popular
- Keep UI simple and focused; avoid over-designing

---

## Success Criteria for v0.4.0-alpha

✅ Users can interact with time slider smoothly

✅ Slider snaps to 10-minute intervals correctly

✅ Destinations list updates based on slider position

✅ Distance and time calculations are realistic

✅ Destination selection works and saves state

✅ App performs well on device (60fps interactions)

✅ Code is well-tested and documented

✅ Ready to integrate with v0.5.0 (Flight Engine)

---

## Integration Points

- ⬅️ **v0.2.0**: Uses `AirportService`, `calculateDistance()` utility
- ⬅️ **v0.3.0**: Uses home airport from `storageService`
- 📍 **v0.4.0**: Creates flight state (time, destination)
- ➡️ **v0.5.0**: Consumes flight state, implements cockpit timer

---

## Async Coding Agent Opportunities

**All 5 PRs are well-suited for async agent work:**

1. **PR #1 (Slider Component)** — Component implementation with standard patterns
2. **PR #2 (Snap Logic)** — Straightforward math utilities and tests
3. **PR #3 (Radius Filtering)** — Well-defined algorithm; agent can implement, you verify logic
4. **PR #4 (Destinations List)** — Standard UI component with FlatList patterns
5. **PR #5 (Integration)** — You should handle this for architectural decisions

**Recommended Setup**:

- Send PRs #1-3 to agent on Jan 13 with detailed specs
- Review results on Jan 15-16
- Have agent do PR #4 on Jan 16 if needed
- Complete PR #5 yourself on Jan 17-18 to ensure quality

**Expected Time Savings**: 4-5 hours with agent handling implementation details

---

### Last Updated

January 4, 2026
