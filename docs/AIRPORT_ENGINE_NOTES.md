# Airport Engine Notes

## Why This Matters

Airport availability is the heart of TravelBlock.

A generic focus timer can count down. TravelBlock needs to answer: where can I fly from here with this amount of focus time?

## Core Inputs

- Current airport
- Home airport
- Selected duration
- Airport dataset
- Simulated cruise speed
- Tolerance band

## Core Outputs

- Reachable route options
- Route labels
- Distance
- Estimated reward
- Return-home status
- Locked/too-far hints if desired

## Airport Dataset Requirements

Each airport should include:

```text
code
icao
iata optional
name
city
stateOrRegion
country
latitude
longitude
keywords
```

For Alex's likely starting region, include at minimum:

```text
KOUN - University of Oklahoma Westheimer Airport
KOKC - Will Rogers World Airport
KTUL - Tulsa International Airport
KDFW - Dallas/Fort Worth International Airport
KDAL - Dallas Love Field
KICT - Wichita Dwight D. Eisenhower National Airport
KMCI - Kansas City International Airport
KDEN - Denver International Airport
KSTL - St. Louis Lambert International Airport
KAUS - Austin-Bergstrom International Airport
KHOU - William P. Hobby Airport
KIAH - George Bush Intercontinental Airport
KAMA - Rick Husband Amarillo International Airport
KLIT - Bill and Hillary Clinton National Airport
KMEM - Memphis International Airport
KMSY - Louis Armstrong New Orleans International Airport
```

Add enough major airports to make longer routes feel real.

## Search Requirements

Search should match:

- ICAO code, such as KOUN
- IATA code, such as OUN when available
- Airport name
- City
- State or region
- Country
- Keywords, such as Norman, OU, University of Oklahoma, Westheimer

## Distance Calculation

Use great-circle distance.

Implementation should live in domain logic, not UI.

Test examples should include known airport pairs with approximate distances. Tests do not need perfect nautical precision, but should catch major errors.

## Duration-to-Distance Mapping

v1 can use a simple simulated cruise speed.

Suggested:

```text
simulatedCruiseSpeedMph = 360
```

Formula:

```text
targetDistance = durationHours * simulatedCruiseSpeedMph
minimumDistance = targetDistance * 0.55
maximumDistance = targetDistance * 1.25
```

This keeps route discovery flexible enough that users do not see empty lists constantly.

## Labels

### Best Match

Distance is close to target distance.

### Short Hop

Distance is below target but still useful.

### Long Stretch

Distance is near the upper bound.

### Return Home

Destination is the home airport and current airport is different.

### Too Far

Destination is outside range for the selected duration. This can be used for hints, not the main selectable list.

## Sorting

Suggested route sort:

1. Return Home if contextually relevant
2. Best Match
3. Short Hop
4. Long Stretch
5. Distance from selected duration target
6. Destination popularity or airport size if available

## Empty State

Avoid empty results where possible.

If no destinations are found:

- Suggest increasing focus duration.
- Show nearest airports as unavailable/too short/too far context.
- Allow choosing a custom duration.

## Reward Tie-In

Rewards can use duration and distance:

```text
basePoints = durationMinutes * 4
distanceBonus = floor(distanceMiles / 10)
completionBonus = 25
```

Avoid making rewards overly complex before the core loop feels good.

## Testing Checklist

- KOUN at 15 minutes returns nearby options or useful short-hop suggestions.
- KOUN at 25 minutes returns regional options.
- KOUN at 45 to 60 minutes returns farther regional routes.
- Current airport changing changes available routes.
- Return Home appears when away from home.
- Too Far destinations do not become selectable accidentally.
