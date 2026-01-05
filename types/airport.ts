/**
 * Type definitions for airport data.
 * 
 * This module defines the structure of airport data as stored in airports.json
 * and used throughout the application.
 * 
 * @module types/airport
 */

/**
 * Represents an airport with all its associated metadata.
 * 
 * The data structure matches the format in airports.json, where each airport
 * is identified by its ICAO code and contains comprehensive information about
 * the airport's location, identifiers, and timezone.
 */
export interface Airport {
  /** ICAO code (International Civil Aviation Organization) - 4-letter code */
  icao: string;
  
  /** IATA code (International Air Transport Association) - 3-letter code, may be empty */
  iata: string;
  
  /** Full name of the airport */
  name: string;
  
  /** City where the airport is located */
  city: string;
  
  /** State/province (may be empty for non-US airports) */
  state: string;
  
  /** Two-letter country code (e.g., "US", "GB", "FR") */
  country: string;
  
  /** Elevation above sea level in feet */
  elevation: number;
  
  /** Latitude in decimal degrees */
  lat: number;
  
  /** Longitude in decimal degrees */
  lon: number;
  
  /** Timezone identifier (e.g., "America/New_York", "Europe/London") */
  tz: string;
  /** Optional precomputed lowercase/normalized search fields */
  __lcIcao?: string;
  __lcIata?: string;
  __lcName?: string;
  __lcCity?: string;
}

/**
 * Represents the complete airport dataset.
 * 
 * This is a dictionary/map structure where:
 * - Keys are ICAO codes (e.g., "KJFK", "EGLL")
 * - Values are Airport objects
 * 
 * This structure allows for O(1) lookup by ICAO code.
 * 
 * @example
 * ```typescript
 * const airports: AirportData = {
 *   "KJFK": {
 *     icao: "KJFK",
 *     iata: "JFK",
 *     name: "John F Kennedy International Airport",
 *     city: "New York",
 *     // ... other fields
 *   },
 *   // ... more airports
 * };
 * ```
 */
export type AirportData = Record<string, Airport>;
