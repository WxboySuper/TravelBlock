# Old Feature Map

This file maps old/prototype features to the native Android rebuild. It intentionally uses high-level references instead of porting code.

## Feature Map

| Feature | Old state | Native rebuild decision | Notes |
|---|---|---|---|
| Android-first app | Existing app is Expo/React Native Android-first | Keep goal, change stack | Rebuild in Kotlin + Compose. |
| Onboarding | Implemented | Keep and redesign | Should be concise and airport-focused. |
| Home airport selection | Implemented | Keep | Must persist locally. |
| Home airport persistence | Implemented | Keep | Use DataStore. |
| Destination discovery by time | Implemented | Keep and strengthen | This is the core differentiator. Build tests. |
| Airport search | Implemented or partially implemented | Keep | Search by code, ICAO, city, name, keywords. |
| Booking flow | Implemented | Keep and polish | Make it feel like travel booking, not form filling. |
| Booking review | Implemented | Keep | Include route, time, seat, flight number, gate, group. |
| Seat selection | Implemented | Keep as MVP | Paid/upgraded seats can wait for reward phase. |
| Check-in / boarding pass | Implemented | Keep | Boarding pass is a ritual. |
| Cockpit timer | Implemented | Keep and rebuild | Use timestamp-based truth. |
| Map visualization | Implemented with React Native maps | Rebuild carefully | For v1, route arc or simplified map can be enough before complex map integration. |
| Diversion flow | Implemented or partially implemented | Keep | Diversion replaces pause. |
| Arrival handling | Partially implemented | Rebuild | Needs dedicated landed/pivot flow. |
| Continue Journey | Planned | Build | Important because current airport changes. |
| Return Home | Planned | Build | Needed to close loops. |
| Logbook/history | Planned or placeholder | Build | Use Room. |
| Analytics | Planned | Build minimal stats | Avoid overbuilding charts before core flow. |
| Audio immersion | Planned | Add late | Cabin polish after core loop. |
| Rewards/points | Planned | Build after logbook | Points need completed-flight records first. |
| Store | Planned | Build late | Cosmetics only, no real money. |
| Expo/EAS workflows | Existing | Discard | Native Android build replaces it. |
| AsyncStorage/local KV | Existing | Replace | Use DataStore and Room. |

## Keep / Redesign / Discard

### Keep

- Flight-focus loop
- Airport selection
- Duration to destination mechanic
- Booking and boarding pass ritual
- Cockpit session
- Diversion concept
- Logbook direction

### Redesign

- Home screen
- Route cards
- Visual system
- Persistence architecture
- Timer model
- Rewards economy

### Discard

- Expo-specific build system
- React Native UI components
- Cross-platform assumptions
- Any web-first layout assumptions

## Feature Priority

Highest priority:

1. Airport engine
2. Home as departure board
3. Booking flow
4. Resilient cockpit timer
5. Arrival/diversion
6. Logbook

Lower priority:

1. Store
2. Cosmetics
3. Audio
4. Advanced analytics
5. Complex map rendering

## Notes for Agents

Do not search for React Native equivalents. Implement native Android versions of the product behavior.
