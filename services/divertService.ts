/**
 * Divert service
 * 
 * Handles flight diversion functionality - finding nearest airports,
 * calculating new routes, and generating divert reasons.
 * 
 * @module services/divertService
 */

import type { Airport } from '@/types/airport';
import type { DivertOption, FlightBooking, Position } from '@/types/flight';
import { generateFlightBooking } from '@/utils/flightGenerator';
import { calculatePositionDistance } from '@/utils/flightInterpolation';
import { getAirportsWithinDistance } from './airportService';

/**
 * Possible divert reasons for UI display
 */
const DIVERT_REASONS = [
  'Weather ahead',
  'Medical emergency',
  'Low fuel',
  'Technical issue',
  'Passenger emergency',
  'Turbulence',
  'Air traffic control request',
  'Airport congestion at destination',
] as const;

/**
 * Find nearest airports suitable for diversion
 * 
 * Returns airports sorted by distance from current position,
 * excluding the current destination.
 * 
 * @param currentPosition - Current position along flight route
 * @param destinationICAO - Current destination ICAO (to exclude)
 * @param maxCount - Maximum number of options to return (default: 5)
 * @param maxDistanceKm - Maximum search radius in km (default: 500)
 * @returns Array of divert options sorted by distance
 * 
 * @example
 * ```typescript
 * const position = { lat: 40.7128, lon: -74.0060 };
 * const options = await findNearestAirports(position, 'KLAX', 5);
 * 
 * options.forEach(opt => {
 *   console.log(`${opt.airport.icao} - ${opt.distanceFromCurrent}km`);
 * });
 * ```
 */
export async function findNearestAirports(
  currentPosition: Position,
  destinationICAO: string,
  maxCount: number = 5,
  maxDistanceKm: number = 500
): Promise<DivertOption[]> {
  try {
    // Get all airports within range
    const nearbyAirports = await getAirportsWithinDistance(
      currentPosition,
      maxDistanceKm
    );

    // Filter out destination and calculate exact distances
    const divertOptions: DivertOption[] = nearbyAirports
      .filter(airport => airport.icao !== destinationICAO)
      .map(airport => {
        const distanceFromCurrent = calculatePositionDistance(
          currentPosition,
          { lat: airport.lat, lon: airport.lon }
        );

        // Estimate time based on typical cruise speed (450 mph = 724 km/h)
        const estimatedTimeHours = distanceFromCurrent / 724;
        const estimatedTimeSeconds = Math.round(estimatedTimeHours * 3600);

        return {
          airport,
          distanceFromCurrent: Math.round(distanceFromCurrent * 10) / 10, // 1 decimal
          estimatedTime: estimatedTimeSeconds,
        };
      })
      .sort((a, b) => a.distanceFromCurrent - b.distanceFromCurrent)
      .slice(0, maxCount);

    return divertOptions;
  } catch (error) {
    console.error('[DivertService] Error finding nearest airports:', error);
    return [];
  }
}

/**
 * Calculate new flight booking for diverted route
 * 
 * Creates a new booking from current position to divert airport,
 * accounting for already elapsed time and distance.
 * 
 * @param originalBooking - Original flight booking
 * @param currentPosition - Current position along route
 * @param divertAirport - Target divert airport
 * @param elapsedSeconds - Time already elapsed in original flight
 * @returns New flight booking for diverted route
 * 
 * @example
 * ```typescript
 * const newBooking = calculateDivertRoute(
 *   originalBooking,
 *   currentPosition,
 *   nearestAirport,
 *   3600 // 1 hour elapsed
 * );
 * ```
 */
export function calculateDivertRoute(
  originalBooking: FlightBooking,
  currentPosition: Position,
  divertAirport: Airport,
  elapsedSeconds: number
): FlightBooking {
  // Create a temporary "airport" object for current position
  // This is just for the booking generation
  const currentPosAsAirport: Airport = {
    ...originalBooking.origin, // Copy origin fields as base
    icao: `POS${Date.now().toString().slice(-4)}`, // Temporary code
    iata: 'POS',
    name: 'Current Position',
    city: 'In Flight',
    lat: currentPosition.lat,
    lon: currentPosition.lon,
  };

  // Calculate remaining time budget (add 10% margin for safety)
  const remainingSeconds = Math.max(
    originalBooking.durationSeconds - elapsedSeconds,
    0
  );
  const divertTimeSeconds = Math.round(remainingSeconds * 0.9);

  // Generate new booking for divert route
  const divertBooking = generateFlightBooking(
    currentPosAsAirport,
    divertAirport,
    divertTimeSeconds
  );

  // Keep original flight number but add 'D' suffix to indicate divert
  const divertFlightNumber = `${originalBooking.flightNumber.split(' ')[0]} ${
    originalBooking.flightNumber.split(' ')[1]
  }D`;

  return {
    ...divertBooking,
    flightNumber: divertFlightNumber,
    gate: originalBooking.gate, // Keep same gate
    terminal: originalBooking.terminal, // Keep same terminal
  };
}

/**
 * Get a random divert reason for UI display
 * 
 * @returns Random divert reason string
 * 
 * @example
 * ```typescript
 * const reason = getDivertReason();
 * console.log(reason); // "Weather ahead"
 * ```
 */
export function getDivertReason(): string {
  const randomIndex = Math.floor(Math.random() * DIVERT_REASONS.length);
  return DIVERT_REASONS[randomIndex];
}

/**
 * Get all possible divert reasons
 * 
 * @returns Array of all divert reason strings
 */
export function getAllDivertReasons(): readonly string[] {
  return DIVERT_REASONS;
}

/**
 * Estimate time to reach divert airport
 * 
 * @param currentPosition - Current position
 * @param divertAirport - Target airport
 * @returns Estimated time in seconds
 */
export function estimateDivertTime(
  currentPosition: Position,
  divertAirport: Airport
): number {
  const distanceKm = calculatePositionDistance(
    currentPosition,
    { lat: divertAirport.lat, lon: divertAirport.lon }
  );

  // Use typical cruise speed: 450 mph = 724 km/h
  const timeHours = distanceKm / 724;
  return Math.round(timeHours * 3600);
}

/**
 * Check if divert airport is reachable with remaining time
 * 
 * @param divertOption - Divert option to check
 * @param remainingSeconds - Remaining time budget
 * @returns True if reachable, false otherwise
 */
export function isDivertReachable(
  divertOption: DivertOption,
  remainingSeconds: number
): boolean {
  // Add 10% safety margin
  const requiredTime = divertOption.estimatedTime * 1.1;
  return requiredTime <= remainingSeconds;
}
