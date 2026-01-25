# v0.3.0-alpha Implementation Plan

**Goal**: Setting the starting point

**Due**: January 11, 2026

**Status**: Not Started

## Overview

This version focuses on establishing a persistent home airport that serves as the origin point for all flight simulations. The work is divided into 4 focused PRs that can be developed and reviewed independently, with opportunities for async coding agent acceleration.

---

## PR Breakdown

### PR #1: AsyncStorage Service Layer & Home Airport Persistence 💾

**Branch**: `feature/home-airport-persistence`

**Size**: Small (~100-150 lines)

**Priority**: High (Foundation for all other work)

**Description**: Create a service layer to manage home airport persistence using AsyncStorage, establishing the data persistence pattern for future use.

#### Tasks (PR #1)

- [ ] Create `services/storageService.ts` with:
  - `saveHomeAirport(airport: Airport): Promise<void>` - Save home airport to AsyncStorage
  - `getHomeAirport(): Promise<Airport | null>` - Retrieve saved home airport
  - `clearHomeAirport(): Promise<void>` - Clear saved home airport
  - `hasHomeAirport(): Promise<boolean>` - Check if home airport is set
- [ ] Create TypeScript types in `types/storage.ts`:
  - `StorageKey` enum for constant key names
  - Type-safe storage interface
- [ ] Implement error handling and fallback patterns
- [ ] Add comprehensive unit tests in `__tests__/services/storageService.test.ts`
  - Test saving and retrieving airports
  - Test clearing data
  - Test null/empty states
  - Test error scenarios (storage full, permission denied)
- [ ] Add JSDoc comments for all public functions
- [ ] Document storage strategy in comments (key naming, data format)

#### Acceptance Criteria (PR #1)

- All tests pass with 100% coverage
- AsyncStorage integration is transparent and error-safe
- TypeScript strict mode passes
- Follows React Native best practices for async storage
- Keys are well-documented and follow naming conventions
- Ready to be used by other components

#### Example Prompt for PR #1 (Async Agent Eligible)

```text
Create a storage service layer using AsyncStorage for managing home airport persistence:
1. Implement saveHomeAirport, getHomeAirport, clearHomeAirport, and hasHomeAirport functions
2. Create TypeScript types and interfaces for storage operations
3. Add error handling and graceful fallbacks
4. Write comprehensive unit tests covering all scenarios
5. Document the storage key strategy

Ensure the service is robust and follows React Native patterns for async storage.
```

---

### PR #2: GPS Location Service & Nearest Airport Finder 📍

**Branch**: `feature/gps-location-service`

**Size**: Medium (~200-250 lines)

**Priority**: High (Optional but valuable feature)

**Dependencies**: PR #1 (for distance calculations from v0.2.0)

**Description**: Create a location service to request GPS permissions, get user coordinates, and find the nearest airport using distance calculations.

#### Tasks (PR #2)

- [ ] Create `services/locationService.ts` with:
  - `requestLocationPermission(): Promise<boolean>` - Request GPS permission
  - `getCurrentLocation(): Promise<Coordinates | null>` - Get user's current coordinates
  - `getNearestAirport(coordinates?: Coordinates): Promise<Airport | null>` - Find closest airport
  - `hasLocationPermission(): Promise<boolean>` - Check current permission status
- [ ] Implement permission handling:
  - Use `expo-location` for permission and location APIs
  - Handle permission denied gracefully
  - Provide user-friendly error messages
  - Store permission state
- [ ] Integrate with `AirportService` from v0.2.0:
  - Use `getAirportsWithinDistance` to find nearby airports
  - Sort results and return nearest
- [ ] Add TypeScript types in `types/location.ts`:
  - `LocationPermissionStatus` enum
  - Location-related interfaces
- [ ] Add comprehensive unit tests in `__tests__/services/locationService.test.ts`
  - Mock permission scenarios (granted, denied, not determined)
  - Test coordinate retrieval
  - Test nearest airport calculation
  - Test error handling
- [ ] Add JSDoc comments with permission requirement notes

#### Acceptance Criteria (PR #2)

- Permission requests are handled according to platform guidelines
- Location retrieval returns accurate coordinates
- Nearest airport calculation is correct
- All permission states are handled gracefully
- Tests cover both success and failure scenarios
- Properly integrates with v0.2.0 distance utilities
- No console warnings or errors

#### Example Prompt for PR #2 (Async Agent Eligible)

```text
Create a GPS location service with permission handling:
1. Implement requestLocationPermission using expo-location
2. Implement getCurrentLocation to get user coordinates
3. Implement getNearestAirport that uses AirportService and distance calculations
4. Add comprehensive error handling and graceful fallbacks
5. Write tests covering all permission states and scenarios
6. Use TypeScript types for location data

Integrate properly with existing AirportService and ensure permission prompts are user-friendly.
```

---

### PR #3: First Run Experience - UI & Navigation 🚀

**Branch**: `feature/first-run-experience`

**Size**: Medium (~250-300 lines)

**Priority**: High

**Dependencies**: PR #1 (requires storage service)

**Description**: Build the visual and navigational components for the first-run experience that guides users to set their home airport.

#### Tasks (PR #3)

- [ ] Create `screens/FirstRunScreen.tsx`:
  - Welcome page explaining home airport concept
  - Visual hierarchy and clear call-to-action
  - Skip/Start buttons for different user journeys
- [ ] Create `components/home-airport/HomeAirportWizard.tsx`:
  - Multi-step wizard component
  - Step 1: Welcome/Introduction
  - Step 2: Choose between manual search or GPS detection
  - Step 3: Show appropriate method (search modal or GPS button)
  - Step 4: Confirmation screen
- [ ] Implement `components/home-airport/HomeAirportSetupStep.tsx`:
  - Reusable step component with progress indicator
  - Navigation between steps
  - Back/Next/Finish buttons
- [ ] Create `hooks/useFirstRunFlow.ts`:
  - Manage wizard state
  - Track completion status
  - Handle step transitions
- [ ] Implement navigation:
  - Use Expo Router to integrate first run flow
  - Detect first run using storage service
  - Conditionally show first run experience
  - Document navigation pattern in comments
- [ ] Use existing themed components (ThemedText, ThemedView)
- [ ] Add animations for step transitions
- [ ] Ensure responsive design for various screen sizes
- [ ] Add accessibility features (labels, testID for testing)

#### Acceptance Criteria (PR #3)

- First run experience displays on initial app launch
- Wizard steps transition smoothly with animations
- All UI elements are styled consistently with existing design system
- Dark mode and light mode both look good
- Responsive design works on various screen sizes
- No console warnings or errors
- Accessible (screen reader friendly, proper labels)
- Ready to integrate with airport selection modal (v0.2.0) and location service (PR #2)

#### Design Notes (PR #3)

- Reference `app/modal.tsx` for modal integration patterns
- Use `expo-router` conditional navigation
- Follow existing app navigation patterns
- Keep design clean and onboarding flow simple (3-4 steps max)
- Use ThemedText and ThemedView for consistency

#### Example Prompt for PR #3 (Async Agent Eligible)

```text
Create the first-run experience UI:
1. Build a FirstRunScreen that appears on initial app launch
2. Create a HomeAirportWizard component with multi-step flow
3. Step 1: Welcome screen explaining home airport concept
4. Step 2: Choose between manual search or GPS detection
5. Step 3: Show appropriate selection method
6. Step 4: Confirmation screen
7. Implement step transitions with smooth animations
8. Use existing ThemedText and ThemedView components
9. Add accessibility features and testID props

Reference existing navigation patterns and ensure responsive design.
```

---

### PR #4: Home Airport Management - Edit & Display 🏠

**Branch**: `feature/home-airport-management`

**Size**: Medium (~200-250 lines)

**Priority**: Medium

**Dependencies**: PR #1, PR #3

**Description**: Add the ability to view, edit, and change the home airport after initial setup, integrating with the app's main navigation.

#### Tasks (PR #4)

- [ ] Create `components/home-airport/HomeAirportDisplay.tsx`:
  - Display current home airport
  - Show airport name, city, country
  - Display ICAO/IATA codes
  - Touchable to open edit options
  - Themed styling with clear visual hierarchy
- [ ] Create `screens/HomeAirportSettingsScreen.tsx`:
  - Display current home airport details
  - "Change Home Airport" button
  - Confirmation dialog before changing
  - Loading states during update
  - Success/error feedback
- [ ] Implement editing flow:
  - Launch SelectAirportModal (from v0.2.0) for airport selection
  - Validate new selection
  - Update persistent storage
  - Refresh UI with new airport
  - Show confirmation toast/feedback
- [ ] Create `hooks/useHomeAirport.ts`:
  - Custom hook to access and manage home airport
  - Provides `homeAirport`, `setHomeAirport`, `loading`, `error`
  - Handles persistence and state synchronization
  - Auto-refresh on app resume
- [ ] Add integration with app tabs:
  - Consider home airport display in appropriate tab
  - Add settings entry to access home airport management
- [ ] Implement error handling:
  - Handle missing home airport (fallback UI)
  - Handle storage read/write errors
  - Network error handling
- [ ] Add performance optimization:
  - Memoize components appropriately
  - Lazy load home airport data
  - Cache home airport in app state
- [ ] Add UI tests with testID props

#### Acceptance Criteria (PR #4)

- Home airport displays correctly in app UI
- Users can change home airport through settings
- Changes persist across app restarts
- Error states are handled gracefully
- All TypeScript types are correct
- Performance is acceptable (no unnecessary re-renders)
- Integrates cleanly with v0.2.0 SelectAirportModal
- Ready for v0.4.0 (Time Slider will reference this)

#### Example Prompt for PR #4 (Async Agent Eligible)

```text
Integrate home airport management into the app:
1. Create HomeAirportDisplay component to show current home airport
2. Create HomeAirportSettingsScreen for editing home airport
3. Implement useHomeAirport hook for state management and persistence
4. Allow users to change home airport through modal selection (from v0.2.0)
5. Add confirmation dialogs and error handling
6. Implement auto-refresh on app resume
7. Add toast notifications for successful updates
8. Optimize rendering with memoization

Ensure clean integration with existing app navigation and no regressions.
```

---

## Testing Strategy

### Unit Tests

- Storage service (save, retrieve, clear, error scenarios)
- Location service (permission handling, coordinate retrieval, nearest airport calculation)
- Home airport hook (state updates, persistence, error handling)

### Integration Tests

- First run flow end-to-end (welcome → selection → confirmation → persistence)
- Home airport update flow (edit → select → confirm → display update)
- GPS permission + nearest airport discovery flow
- Storage persistence across app restarts

### Manual Testing Checklist

- [ ] Run first-time user flow on physical Android device
- [ ] Verify home airport persists across app restart
- [ ] Test GPS permission requests (allow, deny, not determined)
- [ ] Test manual airport selection flow
- [ ] Verify home airport displays correctly in app
- [ ] Test changing home airport from settings
- [ ] Test dark mode and light mode
- [ ] Test on different screen sizes
- [ ] Verify accessibility with screen reader
- [ ] Test with poor/no network connection
- [ ] Verify error handling in all failure scenarios
- [ ] Check for memory leaks with repeated permission requests

---

## Timeline Estimate

- **PR #1**: 2-3 hours (1 day) — Can use async agent for unit tests
- **PR #2**: 3-4 hours (1-2 days) — Can use async agent for implementation
- **PR #3**: 4-6 hours (1-2 days) — Good candidate for async agent
- **PR #4**: 3-4 hours (1 day) — Can use async agent for hook and integration

**Total**: ~4-5 days with sequential development

**With Async Agents**: Can accelerate significantly; PRs #1, #2, #3, and #4 are all suitable for async agent work, potentially reducing total time to 2-3 days with proper oversight.

## Notes

- All components should integrate cleanly with v0.2.0's SelectAirportModal and AirportService
- The home airport becomes the origin point for v0.4.0's Time Slider feature
- Consider storing user preferences (distance unit, metric vs imperial) alongside home airport for future versions
- GPS functionality is optional but recommended for better UX
- Storage service pattern established here will be reused for future persistent data (settings, logbook entries, etc.)
- Ensure all permission requests are non-blocking and graceful
- First run experience should be skippable but encourage completion

---

## Success Criteria for v0.3.0-alpha

✅ First-time users see onboarding flow on app launch

✅ Users can select home airport via search modal or GPS detection

✅ Home airport is persisted across app sessions

✅ Users can change home airport from settings

✅ GPS functionality is optional with graceful fallbacks

✅ All UI is responsive and follows design system

✅ Code is well-tested and documented

✅ Ready to integrate with v0.4.0 (Time Slider will use home airport as origin)

---

## Related Implementation Items

- ⬅️ **v0.2.0**: Airport data layer (Completed/In Progress)
  - Depends on: `AirportService`, `SelectAirportModal`, `calculateDistance()`
- 📍 **v0.3.0**: Home Base (Current)
  - Provides: Persistent home airport, first-run experience, location service
- ➡️ **v0.4.0**: Time Slider (Next)
  - Will use: Home airport as origin point, distance calculations for filtering destinations
- ➡️ **v0.5.0**: Flight Engine (Future)
  - Will use: Home and destination airports for simulation

---

## Async Coding Agent Opportunities

This version has excellent opportunities for async agent acceleration:

1. **PR #1 (Async Agent - Recommended)**
   - High-confidence unit tests and storage service implementation
   - Clear requirements and expected behavior
   - Agent can implement, test, and validate independently
   - **Estimated Time Saved**: 2-3 hours

2. **PR #2 (Async Agent - Recommended)**
   - Well-defined GPS and permission handling patterns
   - Clear integration points with v0.2.0 services
   - Testable in isolation
   - **Estimated Time Saved**: 2-3 hours

3. **PR #3 (Async Agent - Recommended)**
   - UI components follow established patterns (ThemedText, ThemedView)
   - Clear wizard flow structure
   - Can reference existing modal implementations
   - **Estimated Time Saved**: 3-4 hours

4. **PR #4 (Async Agent - Recommended)**
   - Hook implementation is straightforward
   - Clear integration with v0.2.0 modal
   - Well-defined state management requirements
   - **Estimated Time Saved**: 2-3 hours

**Total Potential Time Savings**: 9-13 hours with agent assistance across all PRs.

**Recommended Approach**: Use agents for PRs #1, #2, and #4 initially, handle PR #3 (UI/UX) manually or with agent, then review and integrate. This allows you to stay busy with school while agents handle implementation, then focus on code review and testing.

---

## Migration Notes from v0.2.0

- The storage service pattern (PR #1) will be reused for future features (logbook, settings)
- Ensure SelectAirportModal from v0.2.0 is accessible and can be launched from first-run flow
- Distance calculations from v0.2.0 should be working before starting PR #2
- Consider creating a shared types file for common data structures (Airport, Coordinates) to avoid duplication

---

### Last Updated

January 4, 2026

### Author

Development Team
