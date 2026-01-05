/**
 * Airport service for loading, searching, and filtering airport data.
 * 
 * This service provides efficient access to the airports.json dataset with:
 * - Lazy loading and in-memory caching
 * - Multi-field search (name, city, ICAO, IATA)
 * - Distance-based filtering
 * - Country and code-based lookups
 * 
 * Performance characteristics:
 * - Initial load: ~100-200ms (one-time cost)
 * - Subsequent operations: <10ms (cached)
 * - Search operations: <100ms for typical queries
 * - Optimized for 50,000+ airports
 * 
 * @module services/airportService
 */

import { Airport, AirportData } from '../types/airport';
import { Coordinates, calculateDistance } from '../utils/distance';
import { computeScore } from './airportScoring';

// Normalize strings for search: unicode decomposition without diacritics
function normalizeForSearch(s: string): string {
  return typeof String.prototype.normalize === 'function'
    ? s.normalize('NFKD').replace(/\p{Diacritic}/gu, '')
    : s;
}

/**
 * In-memory cache for loaded airport data.
 * Initially null, populated on first access.
 */
// Internal airport type includes runtime precomputed normalized fields
type InternalAirport = Airport & {
  __lcIcao: string;
  __lcIata: string;
  __lcName: string;
  __lcCity: string;
  // normalized uppercase country
  country: string;
};

let airportCache: Record<string, InternalAirport> | null = null;

/**
 * Array cache for efficient iteration and filtering.
 * Populated when data is first loaded.
 */
let airportArray: InternalAirport[] | null = null;

/**
 * Loads the airport dataset from airports.json.
 * 
 * This function implements lazy loading - the data is only loaded on first access.
 * Subsequent calls return the cached data immediately.
 * 
 * The data is loaded synchronously using require() for simplicity and reliability
 * in the React Native environment. The dataset is cached in memory for the lifetime
 * of the application.
 * 
 * @returns A Promise that resolves to the complete airport dataset
 * 
 * @example
 * ```typescript
 * const airports = await loadAirports();
 * console.log(`Loaded ${Object.keys(airports).length} airports`);
 * ```
 */
export function loadAirports(): Promise<AirportData> {
  if (airportCache) {
    return Promise.resolve(airportCache);
  }

  // Use require for synchronous loading in React Native
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  const rawData: AirportData = require('../data/airports.json');

  // Normalize keys to uppercase and precompute lowercase/normalized search fields
  const normalized: Record<string, InternalAirport> = {};

  for (const [key, airport] of Object.entries(rawData)) {
    const icaoKey = String(key).trim().toUpperCase();

    // Defensive defaults
    const name = typeof airport.name === 'string' ? airport.name : '';
    const city = typeof airport.city === 'string' ? airport.city : '';
    const iata = typeof airport.iata === 'string' ? airport.iata : '';
    const icao = typeof airport.icao === 'string' ? airport.icao : icaoKey;

    const __lcName = normalizeForSearch(name).toLowerCase();
    const __lcCity = normalizeForSearch(city).toLowerCase();
    const __lcIata = normalizeForSearch(iata).toLowerCase();
    const __lcIcao = normalizeForSearch(icao).toLowerCase();

    // Normalize country code to uppercase for reliable country filtering
    const countryCode = String(airport.country ?? '').toUpperCase();

    // Create a fully-typed internal airport object with precomputed fields
    const enhanced: InternalAirport = {
      ...airport,
      country: countryCode,
      __lcName,
      __lcCity,
      __lcIata,
      __lcIcao,
    } as InternalAirport;

    normalized[icaoKey] = enhanced;
  }

  airportCache = normalized;
  airportArray = Object.values(normalized);

  return Promise.resolve(airportCache);
}

// Strip internal precomputed fields from an InternalAirport before returning
function stripInternal(internal: InternalAirport): Airport {
  // Destructure away internal fields
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { __lcIcao, __lcIata, __lcName, __lcCity, ...publicFields } = internal;
  return publicFields as Airport;
}



/**
 * Searches for airports matching the given query string.
 * 
 * The search is performed across multiple fields:
 * - ICAO code (exact and prefix match)
 * - IATA code (exact and prefix match)
 * - Airport name (substring match, case-insensitive)
 * - City name (substring match, case-insensitive)
 * 
 * Results are scored and sorted by relevance:
 * - Exact ICAO/IATA matches: highest priority
 * - Prefix matches: medium priority
 * - Substring matches: lower priority
 * 
 * @param query - Search string (minimum 2 characters recommended)
 * @returns Array of matching airports, sorted by relevance
 * 
 * @example
 * ```typescript
 * // Search by ICAO code
 * const jfk = searchAirports("KJFK");
 * 
 * // Search by IATA code
 * const lax = searchAirports("LAX");
 * 
 * // Search by city name
 * const nyAirports = searchAirports("New York");
 * 
 * // Search by airport name
 * const heathrow = searchAirports("Heathrow");
 * ```
 */
export function searchAirports(query: string): Airport[] {
  if (!airportArray) {
    // Return empty array if data not loaded
    return [];
  }

  if (!query || query.trim().length === 0) {
    return [];
  }

  // Normalize search term for consistent matching
  const searchTerm = normalizeForSearch(query.trim()).toLowerCase();
  const results: Array<{ airport: InternalAirport; score: number }> = [];

  for (const airport of airportArray) {
    const score = computeScore(airport, searchTerm);
    if (score > 0) {
      results.push({ airport, score });
    }
  }

  // Sort by score (descending) and return public Airport objects (strip internals)
  return results
    .sort((a, b) => b.score - a.score)
    .map((result) => stripInternal(result.airport));
}

/**
 * Finds all airports within a specified distance from an origin point.
 * 
 * This function uses the Haversine formula (via calculateDistance) to compute
 * the great-circle distance between the origin and each airport. Only airports
 * within the specified maximum distance are returned.
 * 
 * Results are sorted by distance (nearest first).
 * 
 * Performance note: This function iterates through all airports, so it may take
 * 50-100ms for the full dataset. Consider caching results for repeated queries
 * with the same origin and distance.
 * 
 * @param origin - Origin coordinates (latitude and longitude)
 * @param maxDistance - Maximum distance in miles
 * @returns Array of airports within range, sorted by distance (nearest first)
 * 
 * @example
 * ```typescript
 * // Find all airports within 50 miles of New York City
 * const nycCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
 * const nearbyAirports = getAirportsWithinDistance(nycCoords, 50);
 * 
 * console.log(`Found ${nearbyAirports.length} airports within 50 miles`);
 * ```
 */
export function getAirportsWithinDistance(
  origin: Coordinates,
  maxDistance: number
): Airport[] {
  if (!airportArray) {
    return [];
  }

  const results: Array<{ airport: InternalAirport; distance: number }> = [];

  for (const airport of airportArray) {
    const airportCoords: Coordinates = {
      lat: airport.lat,
      lon: airport.lon,
    };

    const distance = calculateDistance(origin, airportCoords);

    if (distance <= maxDistance) {
      results.push({ airport, distance });
    }
  }

  // Sort by distance (ascending) and strip internal fields before returning
  return results
    .sort((a, b) => a.distance - b.distance)
    .map((result) => stripInternal(result.airport));
}

/**
 * Retrieves a single airport by its ICAO code.
 * 
 * This is an O(1) lookup operation using the cached airport data structure.
 * ICAO codes are case-insensitive for lookup purposes.
 * 
 * @param icao - ICAO code (4-letter code, case-insensitive)
 * @returns The airport with the specified ICAO code, or null if not found
 * 
 * @example
 * ```typescript
 * const jfk = getAirportByICAO("KJFK");
 * if (jfk) {
 *   console.log(`${jfk.name} is in ${jfk.city}`);
 * }
 * 
 * // Case-insensitive
 * const lax = getAirportByICAO("klax");
 * ```
 */
export function getAirportByICAO(icao: string): Airport | null {
  if (!airportCache) {
    return null;
  }

  // Keys are normalized to uppercase during loading; return a public Airport object
  const found = airportCache[icao.toUpperCase()];
  return found ? stripInternal(found) : null;
}
/**
 * Retrieves all airports in a specific country.
 * 
 * Countries are identified by their two-letter country codes (e.g., "US", "GB", "FR").
 * The search is case-insensitive.
 * 
 * Results are returned in the order they appear in the dataset (no specific sorting).
 * Consider sorting results by name or other criteria if needed.
 * 
 * @param country - Two-letter country code (case-insensitive)
 * @returns Array of all airports in the specified country
 * 
 * @example
 * ```typescript
 * // Get all airports in the United States
 * const usAirports = getAirportsByCountry("US");
 * console.log(`Found ${usAirports.length} airports in the US`);
 * 
 * // Get all airports in the United Kingdom
 * const ukAirports = getAirportsByCountry("GB");
 * 
 * // Case-insensitive
 * const frAirports = getAirportsByCountry("fr");
 * ```
 */
export function getAirportsByCountry(country: string): Airport[] {
  if (!airportArray) {
    return [];
  }

  const countryCode = country.toUpperCase();
  
  return airportArray
    .filter((airport) => airport.country === countryCode)
    .map((airport) => stripInternal(airport));
}

/**
 * Clears the in-memory airport cache.
 * 
 * This function is primarily useful for testing purposes, allowing tests to
 * verify lazy loading behavior. In production, the cache should persist for
 * the lifetime of the application.
 * 
 * @internal
 */
export function clearCache(): void {
  airportCache = null;
  airportArray = null;
}
