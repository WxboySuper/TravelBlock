/**
 * Flight metrics simulation utilities
 * 
 * Provides functions for simulating realistic flight metrics (altitude, speed,
 * vertical speed) based on flight progress and phase.
 * 
 * Simulates typical commercial flight profiles:
 * - Climbing: 0-10% of flight, climbing to cruise altitude
 * - Cruising: 10-80% of flight, steady altitude and speed
 * - Descending: 80-100% of flight, descending to landing
 * 
 * @module utils/flightMetrics
 */

import { FlightPhase } from '@/types/flight';

/**
 * Flight simulation constants
 */
export const FLIGHT_CONSTANTS = {
  /** Typical cruise speed in mph */
  CRUISE_SPEED_MPH: 450,
  
  /** Typical climb rate in feet per minute */
  CLIMB_RATE_FPM: 2000,
  
  /** Typical descent rate in feet per minute */
  DESCENT_RATE_FPM: 1500,
  
  /** Approach speed in mph (slower for landing) */
  APPROACH_SPEED_MPH: 250,
  
  /** Takeoff speed in mph */
  TAKEOFF_SPEED_MPH: 180,
  
  /** Phase transition points (as percentage of flight) */
  CLIMB_END_PERCENT: 10,
  DESCENT_START_PERCENT: 80,
  
  /** Maximum altitude for very long flights (feet) */
  MAX_ALTITUDE_FT: 40000,
  
  /** Minimum altitude for short flights (feet) */
  MIN_CRUISE_ALTITUDE_FT: 20000,
  
  /** Distance thresholds for altitude assignment (km) */
  SHORT_FLIGHT_THRESHOLD_KM: 200,
  LONG_FLIGHT_THRESHOLD_KM: 1000,
} as const;

/**
 * Determine flight phase based on progress percentage
 * 
 * @param progressPercent - Progress through flight (0-100)
 * @returns Current flight phase
 * 
 * @example
 * ```typescript
 * getFlightPhase(5);  // FlightPhase.Climbing
 * getFlightPhase(50); // FlightPhase.Cruising
 * getFlightPhase(90); // FlightPhase.Descending
 * ```
 */
export function getFlightPhase(progressPercent: number): FlightPhase {
  if (progressPercent < FLIGHT_CONSTANTS.CLIMB_END_PERCENT) {
    return FlightPhase.Climbing;
  }
  if (progressPercent < FLIGHT_CONSTANTS.DESCENT_START_PERCENT) {
    return FlightPhase.Cruising;
  }
  return FlightPhase.Descending;
}

/**
 * Calculate maximum cruise altitude based on flight distance
 * 
 * Shorter flights don't climb as high, longer flights cruise higher
 * for fuel efficiency.
 * 
 * @param distanceKm - Total flight distance in kilometers
 * @returns Maximum cruise altitude in feet MSL
 * 
 * @example
 * ```typescript
 * getMaxAltitude(150);  // ~22,000 ft (short hop)
 * getMaxAltitude(500);  // ~31,000 ft (medium)
 * getMaxAltitude(2000); // ~40,000 ft (long haul)
 * ```
 */
export function getMaxAltitude(distanceKm: number): number {
  const { SHORT_FLIGHT_THRESHOLD_KM, LONG_FLIGHT_THRESHOLD_KM,
          MIN_CRUISE_ALTITUDE_FT, MAX_ALTITUDE_FT } = FLIGHT_CONSTANTS;
  
  if (distanceKm <= SHORT_FLIGHT_THRESHOLD_KM) {
    // Short flights: 20,000-25,000 ft
    const ratio = distanceKm / SHORT_FLIGHT_THRESHOLD_KM;
    return MIN_CRUISE_ALTITUDE_FT + (ratio * 5000);
  }
  
  if (distanceKm >= LONG_FLIGHT_THRESHOLD_KM) {
    // Long flights: max altitude
    return MAX_ALTITUDE_FT;
  }
  
  // Medium flights: interpolate between 25,000 and 40,000 ft
  const ratio = (distanceKm - SHORT_FLIGHT_THRESHOLD_KM) / 
                (LONG_FLIGHT_THRESHOLD_KM - SHORT_FLIGHT_THRESHOLD_KM);
  return 25000 + (ratio * 15000);
}

/**
 * Calculate current altitude based on progress and phase
 * 
 * Simulates realistic altitude profile with smooth transitions:
 * - Climbing: Exponential curve from 0 to cruise altitude
 * - Cruising: Steady at cruise altitude
 * - Descending: Exponential curve from cruise altitude to 0
 * 
 * @param progressPercent - Progress through flight (0-100)
 * @param phase - Current flight phase
 * @param maxAltitude - Maximum cruise altitude for this flight
 * @returns Current altitude in feet MSL
 * 
 * @example
 * ```typescript
 * const maxAlt = getMaxAltitude(500); // ~31,000 ft
 * calculateAltitude(5, FlightPhase.Climbing, maxAlt);   // ~15,500 ft
 * calculateAltitude(50, FlightPhase.Cruising, maxAlt);  // ~31,000 ft
 * calculateAltitude(90, FlightPhase.Descending, maxAlt); // ~15,500 ft
 * ```
 */
export function calculateAltitude(
  progressPercent: number,
  phase: FlightPhase,
  maxAltitude: number
): number {
  const { CLIMB_END_PERCENT, DESCENT_START_PERCENT } = FLIGHT_CONSTANTS;
  
  switch (phase) {
    case FlightPhase.Climbing: {
      // Progress within climbing phase (0-1)
      const climbProgress = progressPercent / CLIMB_END_PERCENT;
      
      // Use smooth exponential curve (easeOutQuad)
      const easedProgress = 1 - Math.pow(1 - climbProgress, 2);
      
      return maxAltitude * easedProgress;
    }
    
    case FlightPhase.Cruising: {
      // Steady cruise altitude
      return maxAltitude;
    }
    
    case FlightPhase.Descending: {
      // Progress within descending phase (0-1)
      const descendProgress = (progressPercent - DESCENT_START_PERCENT) / 
                              (100 - DESCENT_START_PERCENT);
      
      // Use smooth exponential curve (easeInQuad)
      const easedProgress = Math.pow(1 - descendProgress, 2);
      
      return maxAltitude * easedProgress;
    }
    
    default:
      return 0;
  }
}

/**
 * Calculate current ground speed based on progress and phase
 * 
 * Simulates realistic speed profile:
 * - Climbing: Accelerating from takeoff to cruise speed
 * - Cruising: Steady at cruise speed
 * - Descending: Decelerating to approach speed
 * 
 * @param progressPercent - Progress through flight (0-100)
 * @param phase - Current flight phase
 * @returns Current ground speed in mph
 * 
 * @example
 * ```typescript
 * calculateSpeed(5, FlightPhase.Climbing);   // ~315 mph (accelerating)
 * calculateSpeed(50, FlightPhase.Cruising);  // ~450 mph
 * calculateSpeed(90, FlightPhase.Descending); // ~350 mph (decelerating)
 * ```
 */
export function calculateSpeed(
  progressPercent: number,
  phase: FlightPhase
): number {
  const { TAKEOFF_SPEED_MPH, CRUISE_SPEED_MPH, APPROACH_SPEED_MPH,
          CLIMB_END_PERCENT, DESCENT_START_PERCENT } = FLIGHT_CONSTANTS;
  
  switch (phase) {
    case FlightPhase.Climbing: {
      // Accelerate from takeoff speed to cruise speed
      const climbProgress = progressPercent / CLIMB_END_PERCENT;
      
      // Use smooth acceleration curve
      const easedProgress = 1 - Math.pow(1 - climbProgress, 2);
      
      return TAKEOFF_SPEED_MPH + 
             (easedProgress * (CRUISE_SPEED_MPH - TAKEOFF_SPEED_MPH));
    }
    
    case FlightPhase.Cruising: {
      // Steady cruise speed
      return CRUISE_SPEED_MPH;
    }
    
    case FlightPhase.Descending: {
      // Decelerate from cruise speed to approach speed
      const descendProgress = (progressPercent - DESCENT_START_PERCENT) / 
                              (100 - DESCENT_START_PERCENT);
      
      // Use smooth deceleration curve
      const easedProgress = Math.pow(1 - descendProgress, 2);
      
      return APPROACH_SPEED_MPH + 
             (easedProgress * (CRUISE_SPEED_MPH - APPROACH_SPEED_MPH));
    }
  }
}

/**
 * Calculate vertical speed (rate of climb/descent)
 * 
 * Returns positive values for climbing, negative for descending,
 * zero for cruising.
 * 
 * @param phase - Current flight phase
 * @param currentAltitude - Current altitude in feet
 * @param targetAltitude - Target altitude (cruise for climbing, 0 for descending)
 * @returns Vertical speed in feet per minute
 * 
 * @example
 * ```typescript
 * calculateVerticalSpeed(FlightPhase.Climbing, 15000, 31000);   // +2000 fpm
 * calculateVerticalSpeed(FlightPhase.Cruising, 31000, 31000);   // 0 fpm
 * calculateVerticalSpeed(FlightPhase.Descending, 15000, 0);     // -1500 fpm
 * ```
 */
export function calculateVerticalSpeed(
  phase: FlightPhase,
  currentAltitude: number,
  targetAltitude: number
): number {
  const { CLIMB_RATE_FPM, DESCENT_RATE_FPM } = FLIGHT_CONSTANTS;
  
  switch (phase) {
    case FlightPhase.Climbing: {
      // Check if we're close to cruise altitude (within 500 ft)
      if (Math.abs(currentAltitude - targetAltitude) < 500) {
        // Leveling off
        return Math.round((targetAltitude - currentAltitude) * 4);
      }
      return CLIMB_RATE_FPM;
    }
    
    case FlightPhase.Cruising: {
      // Level flight
      return 0;
    }
    
    case FlightPhase.Descending: {
      // Check if we're close to ground (within 500 ft)
      if (currentAltitude < 500) {
        // Final approach - gentle descent
        return Math.round(-currentAltitude * 2);
      }
      return -DESCENT_RATE_FPM;
    }
  }
}

/**
 * Format altitude for display
 * 
 * @param altitudeFeet - Altitude in feet
 * @returns Formatted string (e.g., "35,000 ft")
 */
export function formatAltitude(altitudeFeet: number): string {
  return `${Math.round(altitudeFeet).toLocaleString()} ft`;
}

/**
 * Format speed for display
 * 
 * @param speedMph - Speed in mph
 * @returns Formatted string (e.g., "450 mph")
 */
export function formatSpeed(speedMph: number): string {
  return `${Math.round(speedMph)} mph`;
}

/**
 * Format vertical speed for display
 * 
 * @param verticalSpeedFpm - Vertical speed in feet per minute
 * @returns Formatted string with arrow (e.g., "↑ 2,000 ft/min")
 */
export function formatVerticalSpeed(verticalSpeedFpm: number): string {
  if (verticalSpeedFpm === 0) {
    return '— 0 ft/min';
  }
  
  const arrow = verticalSpeedFpm > 0 ? '↑' : '↓';
  const absValue = Math.abs(Math.round(verticalSpeedFpm));
  return `${arrow} ${absValue.toLocaleString()} ft/min`;
}

/**
 * Format heading for display
 * 
 * @param headingDegrees - Heading in degrees (0-360)
 * @returns Formatted string with cardinal direction (e.g., "095° E")
 */
export function formatHeading(headingDegrees: number): string {
  // Normalize to 0-360
  const normalized = ((headingDegrees % 360) + 360) % 360;
  
  // Determine cardinal direction
  let direction: string;
  if (normalized < 22.5 || normalized >= 337.5) {
    direction = 'N';
  } else if (normalized < 67.5) {
    direction = 'NE';
  } else if (normalized < 112.5) {
    direction = 'E';
  } else if (normalized < 157.5) {
    direction = 'SE';
  } else if (normalized < 202.5) {
    direction = 'S';
  } else if (normalized < 247.5) {
    direction = 'SW';
  } else if (normalized < 292.5) {
    direction = 'W';
  } else {
    direction = 'NW';
  }
  
  const degrees = Math.round(normalized).toString().padStart(3, '0');
  return `${degrees}° ${direction}`;
}
