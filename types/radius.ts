/**
 * Radius and flight estimation type definitions.
 *
 * @module types/radius
 */

import { Airport } from "./airport";

/**
 * Filter configuration for finding destinations within a time range.
 */
export interface RadiusFilter {
  /** Origin airport */
  origin: Airport;
  /** Flight time in seconds */
  timeInSeconds: number;
  /** Tolerance percentage (e.g., 0.05 for ±5%) */
  tolerance?: number;
}

/**
 * Flight time and distance estimation results.
 */
export interface FlightEstimate {
  /** Estimated flight time in seconds */
  timeInSeconds: number;
  /** Estimated flight distance in miles */
  distanceInMiles: number;
  /** Cruise speed used in mph */
  cruiseSpeed: number;
  /** Takeoff and landing overhead in seconds */
  overhead: number;
}

/**
 * Airport with additional flight estimation data.
 */
export interface AirportWithFlightTime extends Airport {
  /** Estimated flight time from origin in seconds */
  flightTime: number;
  /** Distance from origin in miles */
  distance: number;
}
