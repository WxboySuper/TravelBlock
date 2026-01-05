/**
 * TypeScript types for airport data and selection functionality.
 */

import { Coordinates } from '@/utils/distance';

/**
 * Represents a single airport with ICAO/IATA codes, location, and metadata.
 */
export interface Airport {
  /** ICAO code (4-letter, e.g., "KJFK") */
  icao: string;
  /** IATA code (3-letter, e.g., "JFK"), optional for some locations */
  iata?: string;
  /** Airport name (e.g., "John F. Kennedy International Airport") */
  name: string;
  /** City name (e.g., "New York") */
  city: string;
  /** Country name (e.g., "United States") */
  country: string;
  /** Latitude in decimal degrees */
  lat: number;
  /** Longitude in decimal degrees */
  lon: number;
}

/**
 * Extended airport with calculated distance (for display in search results).
 */
export interface AirportWithDistance extends Airport {
  /** Distance from origin point (in miles or km, depending on context) */
  distance?: number;
  /** Whether distance is in kilometers (default false = miles) */
  distanceInKm?: boolean;
}

/**
 * Parameters for airport search functionality.
 */
export interface AirportSearchParams {
  /** Search query (can match name, city, ICAO, or IATA) */
  query: string;
  /** Optional origin point to calculate distances */
  origin?: Coordinates;
  /** Maximum results to return (default: 50) */
  limit?: number;
  /** Whether to return distances in kilometers (default: false = miles) */
  distanceInKm?: boolean;
}

/**
 * Results from an airport search operation.
 */
export interface AirportSearchResult {
  /** Array of matching airports, sorted by relevance and optionally by distance */
  airports: AirportWithDistance[];
  /** Total matches found (may exceed returned count if limited) */
  totalMatches: number;
  /** Query that was executed */
  query: string;
}
