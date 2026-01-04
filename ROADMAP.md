# TravelBlock Roadmap

This roadmap outlines the flight plan for TravelBlock, a flight-simulator-based focus app. The goal is to build a fully functional travel simulation where focus time translates to real-world distance between airports.

## Versioning Guide

Format: vX.Y.Z (e.g., v0.1.0, v0.2.0, v1.0.0)

Major (X): Fundamental changes to the app structure (New Transport Modes).

Minor (Y): New features, visuals, or significant integrations (increments often!).

Patch (Z): Bug fixes or polish.

Pre-1.0: Use v0.Y.0 for the initial build. Release early, test on device often.

Philosophy: "Code on PC, Run on Phone." Every checkmark is a tangible app update.

## v0.1.0-alpha: The Hello World (Expo)

Goal: Get the app running on your physical Android device.

[x] Install Node.js & Expo CLI.

[x] Initialize project with npx create-expo-app travel-block.

[x] Install "Expo Go" on Android device.

[x] Scan QR Code and verify the app runs.

## v0.2.0-alpha: Airport Data Layer

Goal: Teaching the app geography.

[x] Create airports.json dataset (ICAO, Name, Lat, Lon).

[ ] Implement utility functions to calculate distance between two coordinates (Great Circle Distance).

[ ] Create a "Select Airport" modal with search functionality.

## v0.3.0-alpha: The Home Base

Goal: Setting the starting point.

[ ] Implement "First Run" experience: Set Home Airport.

[ ] Use AsyncStorage to persist the user's Home Airport.

[ ] Allow finding Home Airport via GPS (optional permission).

## v0.4.0-alpha: The Time Slider (Core Mechanic)

Goal: Converting focus time into destinations.

[ ] Implement a custom Slider component (30m to 5h range).

[ ] Add "Snap to 10m intervals" logic.

[ ] The "Radius" Logic: When slider moves, filter airports.json to find destinations that match the flight time (e.g., 1h 03m flight shows up under 1h tab).

[ ] Display a list of "Available Destinations" dynamically based on the slider position.

## v0.5.0-alpha: The Flight Engine (Real-Time)

Goal: The simulation logic.

[ ] Create the "Cockpit" view (Active Timer).

[ ] Implement Real-Time Logic: 1 second of focus = 1 second of flight.

[ ] No Pause Rule: Remove pause button. Stopping the timer "Diverts" the flight (Fail state).

[ ] Link Timer to Distance: As time counts down, update "Miles Remaining" and "Miles Flown."

[ ] Handle "Arrival": When timer hits 0, trigger "Landed" state.

## v0.6.0-alpha: Map Visualization

Goal: Showing the journey.

[ ] Install react-native-maps.

[ ] Render a Google Map centered on the route (Satellite/Terrain/Plain views).

[ ] Draw a Polyline (Geodesic curve) from Origin to Destination.

[ ] Add a "Plane Icon" marker that updates position along the line in real-time.

## v0.7.0-alpha: Audio Immersion

Goal: Making it feel like a flight.

[ ] Add "Seatbelt Chime" sound on start.

[ ] Add "Landing Gear" sound on finish.

[ ] Implement periodic "Cabin Noise" or "Pilot Announcement" sounds (configurable).

## v0.8.0-alpha: The Pivot Point (Post-Flight)

Goal: What happens after you land.

[ ] Implement "Landed" screen (Where am I now?).

[ ] Option A: "Continue Journey" (Set current airport as new Origin, reset slider).

[ ] Option B: "Return Home" (Auto-select Home Airport as destination).

## v0.9.0-alpha: The Logbook & Analytics

Goal: History tracking.

[ ] Create a local database for "Past Flights."

[ ] Log every completed session (Origin, Dest, Duration, Date).

[ ] Create a "Logbook" screen to view flight history.

[ ] Add Charts: "Where you flew" (Map view of all past routes).

## v0.10.0-alpha: Final Polish

[ ] Refine UI (Dark mode cockpit).

[ ] Test on multiple screen sizes.

[ ] Create App Icon.

## v1.0.0: The Flight Simulator (Release) ✈️

Goal: A complete, gamified focus flight simulator.

[ ] Create standalone APK (EAS Build).

[ ] Deploy to phone.

## Post-v1.0: New Modes

### v1.Y.0: Flight Mode Maintenance & Driving Prep

[ ] Maintenance: Bug fixes for Flight Mode.

[ ] View Expansion: Add "Window Seat" view (Cloud animations).

[ ] Driving Foundation: Begin researching road network APIs for v2.0.

## v2.0.0: Driving Mode 🚗

Goal: A new transport mode for shorter/different focus sessions.

[ ] New Mechanic: "Drive Time" instead of Flight Time.

[ ] New Speed Logic: 60mph average (slower progress).

[ ] New Destinations: Cities/Landmarks instead of Airports.

[ ] New View: Road map view.

### v2.Y.0: Driving Maintenance & Chase Prep

[ ] Maintenance: Bug fixes for Flight & Driving modes.

[ ] Chase Foundation: Research radar data integration for mobile.

## v3.0.0: Storm Chase Mode 🌪️

Goal: High-intensity focus for advanced users.

[ ] New Icon: Chase Vehicle (Dominator).

[ ] Target Logic: Chasing a moving target (Simulated Storm) instead of a fixed destination.

[ ] Map Overlay: Simulated Radar Blobs on the map.

### v3.Y.0+: Long Term Maintenance

Goal: Stability for the complete suite.

[ ] Maintenance of all three modes.

[ ] Performance optimizations.
