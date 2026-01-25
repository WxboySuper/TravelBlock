# v0.6.0-alpha Implementation Plan

**Goal**: Showing the journey

**Status**: Not Started

**Timeline**: February 2-16 (14 days, school pace with async agent support)

---

## Overview

v0.6.0 adds visual route mapping to the flight simulation. Users see their flight path on a map in real-time, with a plane marker animating along the route. This version requires integrating `react-native-maps` and implementing geodesic polyline rendering with real-time plane position updates.

This version is split into **4 focused PRs** with moderate async agent support.

---

## PR Breakdown

### PR #1: Maps Initialization & Configuration 🗺️

**Branch**: `feature/maps-integration`

**Size**: Small-Medium (~150-200 lines)

**Priority**: High (Foundation)

**Dependencies**: None (setup PR)

**Description**: Set up react-native-maps, configure API keys, and create the basic map component.

#### Tasks (PR #1)

- [ ] Install and configure `react-native-maps`
- [ ] Add Google Maps API key for Android in `app.json`
- [ ] Create `constants/maps.ts`:
  - API key constants
  - Default map center (use home airport)
  - Default zoom levels
  - Map styling for light/dark mode
- [ ] Create `components/map/MapContainer.tsx`:
  - Basic map display
  - Region initialization (home airport center)
  - Light/dark mode styling
  - Platform-specific handling (Android vs iOS)
- [ ] Create TypeScript types in `types/maps.ts`:
  - `MapRegion` interface
  - `MapViewRef` interface
- [ ] Add appropriate permissions in `app.json`
- [ ] Test on physical device (map loads without errors)

#### Acceptance Criteria (PR #1)

- Maps library loads correctly
- API keys are configured properly
- Map displays home airport region
- Dark/light mode switching works
- No console warnings about configuration
- Ready for route overlay (PR #2)

#### Example Prompt for PR #1 (Async Agent Optional)

```text
Set up react-native-maps integration:
1. Install and configure react-native-maps
2. Add Google Maps API key to app.json (Android)
3. Create MapContainer component for base map display
4. Implement light/dark mode styling
5. Set default region to home airport
6. Add zoom level constants and configuration
7. Ensure platform compatibility (Android/iOS)
8. Add required permissions

Make it ready for polyline and marker overlays in subsequent PRs.
```

---

### PR #2: Route Polyline Rendering 🛫

**Branch**: `feature/route-polyline`

**Size**: Medium (~200-250 lines)

**Priority**: High (Visual core)

**Dependencies**: PR #1, v0.5.0 flight state

**Description**: Render the flight path as a geodesic polyline from origin to destination airport.

#### Tasks (PR #2)

- [ ] Create `services/routeService.ts`:
  - `generateRoutePolyline(origin: Airport, destination: Airport): LatLng[]`
    - Generate great-circle route points
    - ~50 waypoints along geodesic for smooth curve
    - Use Haversine to interpolate points
  - `getBoundingBox(route: LatLng[]): BoundingBox` - For map centering
  - `getRouteDistance(route: LatLng[]): number` - Total distance
- [ ] Create `components/map/RoutePolyline.tsx`:
  - Display polyline on map
  - Styling: route color, width, gradient (optional)
  - Different style for completed vs remaining route
- [ ] Implement route visualization:
  - Green line for completed portion
  - Blue/gray for remaining portion
  - Update as flight progresses (dynamic split point)
- [ ] Add map centering and zoom:
  - Fit entire route in view
  - Appropriate zoom level
  - Smooth camera animation
- [ ] Add TypeScript types in `types/maps.ts`:
  - `LatLng` interface
  - `BoundingBox` interface
- [ ] Add unit tests for route generation
- [ ] Add UI tests with testID props

#### Acceptance Criteria (PR #2)

- Polyline renders correctly from origin to destination
- Route follows great-circle (geodesic) path
- Dynamic updating shows completed vs remaining
- Map centers and zooms appropriately
- Colors and styling are clear
- Performance is acceptable (smooth rendering)
- Works in light/dark mode

#### Example Prompt for PR #2

```text
Implement geodesic route rendering:
1. Create routeService to generate great-circle waypoints between airports
2. Generate ~50 interpolated points along the geodesic path
3. Create RoutePolyline component using Maps SDK
4. Implement dynamic coloring (green for completed, blue for remaining)
5. Update polyline split point based on flight progress
6. Implement map centering/zoom to fit entire route
7. Add smooth camera animations
8. Write unit tests for route generation

Ensure geodesic accuracy and smooth visual updates during flight.
```

---

### PR #3: Plane Marker & Real-Time Position Updates 🛩️

**Branch**: `feature/plane-marker-animation`

**Size**: Medium (~200-250 lines)

**Priority**: High (Interactive element)

**Dependencies**: PR #1, PR #2, v0.5.0 flight state

**Description**: Add an animated plane marker that moves along the route in real-time as the user focuses.

#### Tasks (PR #3)

- [ ] Create custom plane marker icon:
  - Simple airplane icon pointing in direction of travel
  - Rotate based on bearing from current to next waypoint
  - Use SVG or image asset
- [ ] Create `components/map/PlaneMarker.tsx`:
  - Render marker on map
  - Update position based on flight progress
  - Rotate to match route direction
  - Animate position updates smoothly
- [ ] Implement position calculation:
  - Use flight progress % to determine position along route
  - Linear interpolation between waypoints
  - Calculate bearing for marker rotation
- [ ] Add real-time updates:
  - Subscribe to flight progress updates
  - Update marker position every 100ms (or when progress changes)
  - Smooth animations using Reanimated if available
- [ ] Add altitude representation (cosmetic):
  - Slight glow effect during climbing phase
  - Normal glow during cruising
  - Reduced glow during descending
  - Or: altitude label above marker
- [ ] Optimize for performance:
  - Throttle updates if needed
  - Use native thread animations when possible
- [ ] Add TypeScript types:
  - `PlaneMarkerProps` interface
  - `CoordinateProgress` interface

#### Acceptance Criteria (PR #3)

- Plane marker animates smoothly along route
- Marker rotates correctly with route direction
- Updates are smooth and not jittery
- Visual feedback for altitude changes (cosmetic)
- Performance is acceptable during long flights
- Marker is visible at all zoom levels

#### Example Prompt for PR #3 (Async Agent)

```text
Implement plane marker animation on the map:
1. Create custom plane marker icon (SVG or image)
2. Build PlaneMarker component that updates position in real-time
3. Calculate marker position based on flight progress (linear interpolation)
4. Rotate marker to match bearing along route
5. Implement smooth animation between position updates
6. Add altitude visualization (glow effect or label)
7. Subscribe to flight progress updates
8. Optimize for 60fps smooth animations

Use react-native-reanimated for high-performance animations if available.
```

---

### PR #4: Map View Modes & Integration 📱

**Branch**: `feature/map-view-modes`

**Size**: Medium (~200-250 lines)

**Priority**: Medium

**Dependencies**: PR #1-3

**Description**: Add map view type switching (satellite/terrain/standard), integrate with cockpit screen, and implement full map experience during flight.

#### Tasks (PR #4)

- [ ] Implement map view types:
  - Standard (default)
  - Satellite
  - Terrain (if available)
  - Toggle buttons on map interface
- [ ] Create `components/map/MapViewControls.tsx`:
  - View type selector
  - Zoom controls
  - Re-center button
  - Optional: following mode toggle (auto-center on plane)
- [ ] Integrate into CockpitScreen:
  - Add full-screen map option (swipe up or button)
  - Transition animations between cockpit and map view
  - Keep timer visible in corner during map view
  - Allow gestures to zoom/pan (only when not auto-following)
- [ ] Implement "following" mode:
  - Auto-center map on plane marker
  - Smooth camera follow
  - Optional: toggle to manual pan mode
- [ ] Add performance optimization:
  - Lazy load map if starting in cockpit view
  - Destroy map when transitioning away
  - Memory management for long flights
- [ ] Add UI tests with testID props

#### Acceptance Criteria (PR #4)

- Map view types switch smoothly
- Cockpit → Map transitions are smooth
- Following mode keeps plane centered smoothly
- Manual panning works when needed
- Performance is acceptable on device
- Timer remains visible and accurate in map view
- Ready to integrate with v0.7.0 (audio)

#### Example Prompt for PR #4 (Async Agent)

```text
Integrate map views and create full map experience:
1. Implement map type switching (standard, satellite, terrain)
2. Add view control buttons to map interface
3. Integrate map into cockpitScreen with swipe/button to expand
4. Implement "following mode" where map auto-centers on plane
5. Allow manual panning when not following
6. Add zoom controls
7. Keep timer visible during map view
8. Implement smooth transitions between cockpit and map

Ensure smooth performance and intuitive gesture handling.
```

---

## Testing Strategy

### Unit Tests

- Route polyline generation and accuracy
- Position interpolation calculations
- Bearing calculations for marker rotation

### Integration Tests

- Full flight on map (start → progress → arrival)
- Map transitions (cockpit ↔ map)
- Following mode behavior

### Manual Testing Checklist

- [ ] Route polyline renders correctly from home to destination
- [ ] Route looks like realistic flight path (great-circle)
- [ ] Plane marker animates smoothly along route
- [ ] Marker rotates with route direction
- [ ] Map view types switch correctly
- [ ] Following mode auto-centers on plane
- [ ] Manual panning works when not following
- [ ] Zoom controls function
- [ ] Map performs well on device (60fps)
- [ ] Long flights (5h) don't cause memory issues
- [ ] Timer stays visible and accurate in map view
- [ ] Light/dark mode both look good

---

## Timeline Estimate

- **PR #1** (Maps setup): 1 day
- **PR #2** (Polyline): 1-1.5 days
- **PR #3** (Plane marker): 1-1.5 days
- **PR #4** (Integration): 1 day

**Total**: ~4.5-5 days

**Recommended Approach**:

- Start PR #1 immediately after v0.5.0 (Feb 2)
- Queue PRs #2-3 for async agent by Feb 4
- Work on PR #4 while agents implement polyline/marker
- Final integration by Feb 14, buffer day for testing/fixes

---

## Notes

- Maps library performance is critical; test on actual device often
- Geodesic calculations must be accurate for realistic routes
- Battery drain from GPS and map rendering is a consideration
- Consider adding satellite view for visual interest during long flights
- Map might not always be needed; consider lazy loading for performance

---

## Success Criteria for v0.6.0-alpha

✅ Route polyline renders correctly as geodesic path

✅ Plane marker animates smoothly along route in real-time

✅ Map view modes work (standard, satellite, terrain)

✅ Following mode keeps plane centered appropriately

✅ Performance is smooth (60fps during flight)

✅ No memory leaks during long flights

✅ Map integrates cleanly with cockpit screen

✅ Ready for v0.7.0 (audio integration)

---

## Integration Points

- ⬅️ **v0.5.0**: Consumes flight state and progress
- 📍 **v0.6.0**: Map visualization layer
- ➡️ **v0.7.0**: Adds audio cues
- ➡️ **v0.9.0**: Shows past flight routes on logbook map

---

### Last Updated

January 4, 2026

### Author

Development Team
