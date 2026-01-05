# v0.2.0-alpha Implementation Plan

**Goal**: Teaching the app geography

**Status**: In Progress

## Overview

This version focuses on building the airport data layer and selection interface. The work is divided into 4 focused PRs that can be developed and reviewed independently.

---

## PR Breakdown

### PR #1: Distance Calculation Utilities 📐

**Branch**: `feature/distance-calculations`

**Size**: Small (~100-150 lines)

**Priority**: High (Foundation for all other work)

**Description**: Implement the mathematical foundation for calculating distances between coordinates using the Great Circle Distance formula (Haversine).

#### Tasks (PR #1)

- [ ] Create `utils/` directory if it doesn't exist
- [ ] Implement `utils/distance.ts` with the following functions:
  - `calculateDistance(lat1, lon1, lat2, lon2): number` - Returns distance in miles
  - `calculateDistanceKm(lat1, lon1, lat2, lon2): number` - Returns distance in kilometers
  - `degreesToRadians(degrees: number): number` - Helper function
- [ ] Add TypeScript types for coordinates (`Coordinates` interface)
- [ ] Add comprehensive unit tests in `__tests__/utils/distance.test.ts`
  - Test known distances (NYC to LA, London to Paris, etc.)
  - Test edge cases (same location, antipodal points, equator crossings)
- [ ] Add JSDoc comments for all public functions

#### Acceptance Criteria (PR #1)

- All tests pass with 100% coverage
- Functions accurately calculate real-world distances (within 1% margin)
- TypeScript strict mode passes
- No external dependencies added (use pure JavaScript Math)

#### Example Prompt for Implementation (PR #1)

```text
Implement the Haversine formula for calculating great circle distance between two lat/lon coordinates. Create a utils/distance.ts file with:
1. A calculateDistance function that takes two coordinate pairs and returns miles
2. A calculateDistanceKm function for kilometers
3. Full TypeScript types and interfaces
4. Comprehensive unit tests covering edge cases

Use the Earth's radius as 3,959 miles (6,371 km). Ensure accuracy within 1% for real-world distances.
```

---

### PR #2: Airport Service Layer 🛫

**Branch**: `feature/airport-service`

**Size**: Medium (~200-300 lines)

**Priority**: High (Required for search functionality)

**Dependencies**: PR #1 (distance calculations)

**Description**: Create a service layer to load, query, and filter the airports.json dataset.

#### Tasks (PR #2)

- [ ] Create `services/` directory
- [ ] Implement `services/airportService.ts` with:
  - `loadAirports(): Promise<AirportData>` - Lazy load airports.json
  - `searchAirports(query: string): Airport[]` - Search by name, city, ICAO, IATA
  - `getAirportsWithinDistance(origin: Coordinates, maxDistance: number): Airport[]`
  - `getAirportByICAO(icao: string): Airport | null`
  - `getAirportsByCountry(country: string): Airport[]`
- [ ] Create TypeScript interfaces:
  - `Airport` - Single airport structure
  - `AirportData` - Dictionary of airports
- [ ] Implement efficient search algorithms (consider fuzzy matching)
- [ ] Add caching mechanism for loaded data
- [ ] Add unit tests in `__tests__/services/airportService.test.ts`
- [ ] Document performance considerations in comments

#### Acceptance Criteria (PR #2)

- Service loads airports.json efficiently (lazy loading)
- Search returns results in <100ms for typical queries
- Distance calculations integrate correctly with PR #1
- All search functions return sorted results (by relevance/distance)
- 90%+ test coverage

#### Example Prompt for Implementation (PR #2)

```text
Create an AirportService that wraps the airports.json data. Implement:
1. Lazy loading of the airport dataset
2. Search functionality (name, city, ICAO, IATA codes)
3. Distance-based filtering using the calculateDistance utility
4. Efficient in-memory caching
5. Full TypeScript types matching the airports.json structure

The service should handle 50,000+ airports efficiently and return search results quickly.
```

---

### PR #3: Select Airport Modal - UI Components 🎨

**Branch**: `feature/airport-modal-ui`

**Size**: Medium (~250-350 lines)

**Priority**: Medium

**Dependencies**: None (can work in parallel with PR #2)

**Description**: Build the visual components for the airport selection modal without backend integration.

#### Tasks (PR #3)

- [ ] Create `components/airport/` directory
- [ ] Implement `components/airport/SelectAirportModal.tsx`:
  - Modal wrapper with proper animations
  - Search input with clear button
  - Loading state indicator
  - Empty state message
- [ ] Implement `components/airport/AirportListItem.tsx`:
  - Display airport name, city, country
  - Display ICAO/IATA codes
  - Show distance from origin (when applicable)
  - Touchable with haptic feedback
  - Themed styling (dark/light mode support)
- [ ] Implement `components/airport/AirportSearchBar.tsx`:
  - Search input with icon
  - Clear button
  - Debounced input handling
- [ ] Use existing themed components where possible
- [ ] Add storybook or preview screens for testing UI in isolation
- [ ] Ensure responsive design for various screen sizes

#### Acceptance Criteria (PR #3)

- Modal opens/closes smoothly with animations
- Components follow existing design system (theme, spacing, typography)
- Dark mode and light mode both look good
- Haptic feedback works on physical devices
- No console warnings or errors
- Accessible (screen reader friendly)

#### Design Notes (PR #3)

- Use `expo-router` modal pattern
- Leverage `react-native-gesture-handler` for swipe-to-close
- Use `ThemedText` and `ThemedView` from existing components
- Reference `app/modal.tsx` for modal pattern examples

#### Example Prompt for Implementation (PR #3)

```text
Create the UI components for an airport selection modal:
1. SelectAirportModal - Full-screen modal with search and list
2. AirportListItem - Displays airport info in a touchable list item
3. AirportSearchBar - Search input with debouncing

Use the existing ThemedText and ThemedView components. Add smooth animations, haptic feedback, and support both light/dark modes. Modal should follow Expo Router modal patterns.
```

---

### PR #4: Select Airport Modal - Integration & Logic 🔌

**Branch**: `feature/airport-modal-integration`

**Size**: Medium (~200-250 lines)

**Priority**: Medium

**Dependencies**: PR #2, PR #3

**Description**: Connect the UI components to the airport service and implement the full search functionality.

#### Tasks (PR #4)

- [ ] Integrate `AirportService` into `SelectAirportModal`
- [ ] Implement search functionality:
  - Debounced search (300ms delay)
  - Display search results in real-time
  - Handle empty results gracefully
- [ ] Add distance calculations:
  - Calculate distance from user's origin to each airport
  - Display distances in appropriate units (miles/km based on user preference)
  - Sort results by distance when relevant
- [ ] Implement selection handling:
  - Pass selected airport back to parent component
  - Close modal on selection with animation
  - Add confirmation haptic feedback
- [ ] Add error handling:
  - Handle failed data loading
  - Network error states
  - Invalid search inputs
- [ ] Performance optimization:
  - Virtualized list for large result sets (FlatList with optimization)
  - Memoize expensive calculations
  - Lazy render list items
- [ ] Add integration tests

#### Acceptance Criteria (PR #4)

- Search works smoothly without lag
- Distance calculations display correctly
- Modal integrates cleanly with app navigation
- Error states are handled gracefully
- List scrolls smoothly with 1000+ results
- Selection callbacks work correctly
- TypeScript has no errors

#### Example Prompt for Implementation (PR #4)

```text
Integrate the AirportService into the SelectAirportModal components:
1. Connect search bar to airport service search functionality
2. Display search results with calculated distances
3. Implement debounced search (300ms)
4. Handle selection and pass data to parent
5. Add error boundaries and loading states
6. Optimize list rendering with FlatList

Ensure smooth performance even with large datasets.
```

---

## Testing Strategy

### Unit Tests

- Distance calculation accuracy
- Airport service search algorithms
- Component rendering and props

### Integration Tests

- Modal open/close flow
- Search → Results → Selection flow
- Distance calculations in UI

### Manual Testing Checklist

- [ ] Run on physical Android device via Expo Go
- [ ] Test search with various queries (partial names, ICAO codes, cities)
- [ ] Verify distance calculations against known airport distances
- [ ] Test dark mode and light mode
- [ ] Test on different screen sizes
- [ ] Verify haptic feedback on device
- [ ] Test with poor network conditions
- [ ] Verify memory usage with large result sets

---

## Timeline Estimate

- **PR #1**: 2-4 hours (1 day)
- **PR #2**: 4-6 hours (1-2 days)
- **PR #3**: 6-8 hours (2 days) - Can run parallel to PR #2
- **PR #4**: 4-6 hours (1-2 days)

**Total**: ~5-7 days with sequential development, ~3-5 days with parallel work

## Notes

- The airports.json file contains 50,000+ airports, so performance is critical
- Consider adding analytics to track which airports are most searched
- Keep the modal design flexible for future enhancements (filters, sorting options)
- Ensure the distance calculation is accurate enough for flight simulation purposes
- Consider adding unit preferences (miles vs kilometers) early, even if not in scope

---

## Success Criteria for v0.2.0-alpha

✅ User can open a modal to search for airports

✅ Search works across name, city, and airport codes

✅ Distance from current location is displayed for each airport

✅ Distance calculations are accurate (Haversine formula)

✅ UI is responsive and performs well on device

✅ Code is well-tested and documented

✅ Ready to integrate with v0.3.0 (Home Airport selection)

---

## Related Roadmap Items

- ⬅️ **v0.1.0**: Project initialization (Complete)
- 📍 **v0.2.0**: Airport data layer (Current)
- ➡️ **v0.3.0**: Home Base selection (Next - will use this modal)
- ➡️ **v0.4.0**: Time Slider (Will use distance calculations)

---

### Last Updated

January 4, 2026

### Author

Development Team
