/**
 * Radius service for flight time calculations and destination filtering.
 *
 * This service converts focus time into reachable destinations by:
 * - Estimating flight distance based on selected time
 * - Using realistic cruise speeds (~450 mph)
 * - Accounting for takeoff/landing overhead (~25 minutes)
 * - Filtering airports within the calculated radius
 *
 * @module services/radiusService
 */

import { Coordinates } from "@/types/location";
import { AirportWithFlightTime, FlightEstimate } from "@/types/radius";
import { calculateDistance } from "@/utils/distance";
import { getAirportsWithinDistance } from "./airportService";

/**
 * Flight estimation constants based on commercial aviation standards.
 */
export const FLIGHT_CONSTANTS = {
  /** Average cruise speed in mph (typical commercial aircraft) */
  CRUISE_SPEED_MPH: 450,
  /** Takeoff and landing overhead in seconds (taxi, climb, descent) */
  OVERHEAD_SECONDS: 25 * 60, // 25 minutes
  /** Default tolerance for flight time variation (±5%) */
  DEFAULT_TOLERANCE: 0.05,
} as const;

/**
 * Calculates the maximum distance that can be traveled in the given flight time.
 *
 * Formula: distance = (time - overhead) * cruiseSpeed
 *
 * @param timeInSeconds - Total flight duration in seconds
 * @returns Maximum flight distance in miles
 *
 * @example
 * ```typescript
 * // 1 hour flight
 * calculateMaxDistance(3600); // Returns ~262.5 miles
 *
 * // 5 hour flight
 * calculateMaxDistance(18000); // Returns ~2062.5 miles
 * ```
 */
export function calculateMaxDistance(timeInSeconds: number): number {
  // Subtract overhead time (takeoff, landing, taxi)
  const cruiseTimeSeconds = Math.max(
    0,
    timeInSeconds - FLIGHT_CONSTANTS.OVERHEAD_SECONDS
  );

  // Convert to hours
  const cruiseTimeHours = cruiseTimeSeconds / 3600;

  // Calculate distance at cruise speed
  const distanceInMiles = cruiseTimeHours * FLIGHT_CONSTANTS.CRUISE_SPEED_MPH;

  return distanceInMiles;
}

/**
 * Estimates flight time required to travel a given distance.
 *
 * Formula: time = (distance / cruiseSpeed) + overhead
 *
 * @param distanceInMiles - Flight distance in miles
 * @returns Estimated flight time in seconds
 *
 * @example
 * ```typescript
 * // ~250 mile flight (e.g., NYC to Washington DC)
 * estimateFlightTime(250); // Returns ~3600 seconds (1 hour)
 *
 * // ~2500 mile flight (e.g., NYC to LA)
 * estimateFlightTime(2500); // Returns ~21500 seconds (~6 hours)
 * ```
 */
export function estimateFlightTime(distanceInMiles: number): number {
  // Calculate cruise time
  const cruiseTimeHours = distanceInMiles / FLIGHT_CONSTANTS.CRUISE_SPEED_MPH;
  const cruiseTimeSeconds = cruiseTimeHours * 3600;

  // Add overhead time
  const totalTimeSeconds = cruiseTimeSeconds + FLIGHT_CONSTANTS.OVERHEAD_SECONDS;

  return Math.round(totalTimeSeconds);
}

/**
 * Gets detailed flight estimate information.
 *
 * @param timeInSeconds - Flight duration in seconds
 * @returns Flight estimation details
 */
export function getFlightEstimate(timeInSeconds: number): FlightEstimate {
  const distanceInMiles = calculateMaxDistance(timeInSeconds);

  return {
    timeInSeconds,
    distanceInMiles,
    cruiseSpeed: FLIGHT_CONSTANTS.CRUISE_SPEED_MPH,
    overhead: FLIGHT_CONSTANTS.OVERHEAD_SECONDS,
  };
}

/**
 * Finds airports reachable within the specified flight time from an origin.
 *
 * This is the main filtering function that maps time to destinations.
 * It applies a tolerance to account for wind, routing, and aircraft variations.
 *
 * @param origin - Origin airport coordinates
 * @param timeInSeconds - Selected flight duration in seconds
 * @param tolerance - Optional tolerance percentage (default: 5%)
 * @returns Array of reachable airports with flight time estimates, sorted by distance
 *
 * @example
 * ```typescript
 * // Find airports within 1 hour of JFK
 * const jfk = { lat: 40.6413, lon: -73.7781 };
 * const destinations = getDestinationsInTimeRange(jfk, 3600);
 * // Returns airports within ~260 miles (Boston, Philadelphia, Washington DC, etc.)
 *
 * // Find airports within 5 hours of JFK
 * const longFlights = getDestinationsInTimeRange(jfk, 18000);
 * // Returns airports within ~2000 miles (includes LA, Seattle, Miami, etc.)
 * ```
 */
export function getDestinationsInTimeRange(
  origin: Coordinates,
  timeInSeconds: number,
  tolerance: number = FLIGHT_CONSTANTS.DEFAULT_TOLERANCE
): AirportWithFlightTime[] {
  // Calculate maximum distance with tolerance
  const baseDistance = calculateMaxDistance(timeInSeconds);
  const maxDistance = baseDistance * (1 + tolerance);

  // Get airports within distance range
  const airports = getAirportsWithinDistance(origin, maxDistance);

  // Map to AirportWithFlightTime and add flight estimates
  const airportsWithTime: AirportWithFlightTime[] = airports.map((airport) => {
    const distance = calculateDistance(origin, {
      lat: airport.lat,
      lon: airport.lon,
    });
    const flightTime = estimateFlightTime(distance);

    return {
      ...airport,
      distance,
      flightTime,
    };
  });

  // Filter to only include airports within the time range (considering tolerance)
  const minTime = timeInSeconds * (1 - tolerance);
  const maxTime = timeInSeconds * (1 + tolerance);

  const filtered = airportsWithTime.filter(
    (airport) => airport.flightTime >= minTime && airport.flightTime <= maxTime
  );

  // Sort by distance (closest first)
  return filtered.sort((a, b) => a.distance - b.distance);
}

/**
 * Gets all airports reachable within or under the specified flight time.
 *
 * Unlike getDestinationsInTimeRange, this returns ALL airports up to the max time,
 * not just those within a tolerance range of the exact time.
 *
 * @param origin - Origin airport coordinates
 * @param maxFlightTime - Maximum flight duration in seconds
 * @returns Array of reachable airports with flight time, sorted by distance
 *
 * @example
 * ```typescript
 * // Find all airports within 2 hours of LAX
 * const lax = { lat: 33.9416, lon: -118.4085 };
 * const destinations = getDestinationsByFlightTime(lax, 7200);
 * // Returns all airports within ~525 miles
 * ```
 */
export function getDestinationsByFlightTime(
  origin: Coordinates,
  maxFlightTime: number
): AirportWithFlightTime[] {
  // Calculate maximum distance
  const maxDistance = calculateMaxDistance(maxFlightTime);

  // Get all airports within distance
  const airports = getAirportsWithinDistance(origin, maxDistance);

  // Map to AirportWithFlightTime with flight estimates
  const airportsWithTime: AirportWithFlightTime[] = airports.map((airport) => {
    const distance = calculateDistance(origin, {
      lat: airport.lat,
      lon: airport.lon,
    });
    const flightTime = estimateFlightTime(distance);

    return {
      ...airport,
      distance,
      flightTime,
    };
  });

  // Sort by distance (closest first)
  return airportsWithTime.sort((a, b) => a.distance - b.distance);
}
