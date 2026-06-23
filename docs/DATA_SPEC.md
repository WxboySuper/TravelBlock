# TravelBlock Data Spec

## Persistence Philosophy

TravelBlock v1.0 is local-first.

No account, no sync, no backend, no network requirement. User progress should live on the device.

Use:

- DataStore for preferences and lightweight state.
- Room for structured records and history.

## DataStore Preferences

Suggested keys:

```text
firstLaunchCompleted: Boolean
homeAirportCode: String?
currentAirportCode: String?
themePreference: String
soundEnabled: Boolean
ambientModeEnabled: Boolean
hapticsEnabled: Boolean
equippedBoardingPassThemeId: String
equippedAircraftIconId: String
equippedSeatClassId: String
```

## Room Tables

### flights

Purpose: Store completed, diverted, and active historical records.

Fields:

```text
id: String
flightNumber: String
originCode: String
destinationCode: String
startedAt: Long
plannedArrivalAt: Long
completedAt: Long?
durationMinutes: Int
distanceMiles: Double
milesFlown: Double
status: String
seatId: String?
seatClassId: String?
pointsEarned: Int
diversionReason: String?
createdAt: Long
updatedAt: Long
```

Allowed status values:

```text
PLANNED
ACTIVE
COMPLETED
DIVERTED
ABANDONED
```

### point_transactions

Purpose: Audit all points changes.

Fields:

```text
id: String
type: String
amount: Int
balanceAfter: Int
relatedFlightId: String?
relatedStoreItemId: String?
note: String
createdAt: Long
```

Allowed type values:

```text
EARN_FLIGHT
EARN_BONUS
SPEND_COSMETIC
ADJUSTMENT
RESET
```

### unlocked_cosmetics

Purpose: Store point-based purchases.

Fields:

```text
id: String
cosmeticType: String
cosmeticId: String
unlockedAt: Long
pricePaid: Int
```

Allowed cosmetic types:

```text
BOARDING_PASS_THEME
AIRCRAFT_ICON
SEAT_CLASS
```

## Domain Models

### Airport

```text
code: String
icao: String
iata: String?
name: String
city: String
stateOrRegion: String?
country: String
latitude: Double
longitude: Double
keywords: List<String>
isHomeCandidate: Boolean
```

Search should use code, ICAO, IATA, name, city, state/region, country, and keywords.

### RouteOption

```text
origin: Airport
destination: Airport
distanceMiles: Double
estimatedDurationMinutes: Int
selectedDurationMinutes: Int
rewardPoints: Int
availabilityLabel: String
availabilityScore: Double
isReturnHome: Boolean
```

### FlightPlan

```text
flightNumber: String
origin: Airport
destination: Airport
durationMinutes: Int
distanceMiles: Double
departureTime: Long
plannedArrivalTime: Long
seat: SeatOption
gate: String
boardingGroup: String
estimatedReward: Int
```

### SeatOption

```text
id: String
label: String
seatClass: String
pointsCost: Int
isUnlocked: Boolean
```

### UserProgress

```text
homeAirportCode: String
currentAirportCode: String
pointsBalance: Int
totalFlights: Int
totalCompletedFlights: Int
totalDivertedFlights: Int
totalFocusMinutes: Int
totalMiles: Double
streakDays: Int
```

## Route Availability Logic

The route engine should convert selected focus duration into a reasonable distance band.

Inputs:

- Origin airport
- Selected duration
- Simulated cruise speed
- Tolerance band
- Optional home airport

Outputs:

- Sorted list of route options
- Labels explaining availability

Suggested simple v1 formula:

```text
estimatedDistance = durationHours * simulatedCruiseSpeedMph
minimumDistance = estimatedDistance * 0.55
maximumDistance = estimatedDistance * 1.25
```

Suggested simulated cruise speed:

```text
360 mph for early MVP simplicity
```

This can be tuned later. The most important behavior is that nearby airports appear for short focus sessions and farther airports appear for longer focus sessions.

## Route Labels

Suggested label rules:

```text
Return Home: destination is the user's home airport and current airport is not home
Best Match: distance is near the selected duration's target distance
Short Hop: distance is below target but still useful
Long Stretch: distance is near upper range
Too Far: not selectable for current duration, optionally shown as locked/teaser
```

## Timer Truth Model

Do not treat an in-memory countdown as truth.

Active flight truth fields:

```text
startedAt
plannedArrivalAt
durationMinutes
originCode
destinationCode
distanceMiles
status
```

Every screen should calculate:

```text
elapsedMs = now - startedAt
remainingMs = plannedArrivalAt - now
progress = elapsedMs / totalDurationMs
milesFlown = distanceMiles * progress
milesRemaining = distanceMiles - milesFlown
```

Clamp progress between 0 and 1.

If `now >= plannedArrivalAt`, the flight is arrival-ready.

## Rewards Rules

Simple v1 reward formula:

```text
basePoints = durationMinutes * 4
distanceBonus = floor(distanceMiles / 10)
completionBonus = 25
reward = basePoints + distanceBonus + completionBonus
```

Diversion options:

- No points for very early diversion.
- Partial points after a minimum threshold.
- Always record the diverted session.

Suggested diversion rule:

```text
if progress < 0.25: points = 0
else points = floor(fullReward * progress * 0.5)
```

## Data Integrity Rules

- Current airport updates only when a flight completes.
- Diverted flights do not automatically move the user to the destination.
- Points changes must always create a transaction record.
- Store purchases must check balance before spending.
- Reset data should clear Room and DataStore, then return to onboarding.
