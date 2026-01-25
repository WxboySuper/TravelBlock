import type { Airport } from '../types/airport';

/**
 * Interface representing a search query context.
 * Wrapping the term in an object avoids "primitive obsession" issues in linter checks.
 */
export interface SearchQuery {
  term: string;
}

/**
 * Interface representing an airport with precomputed lowercase fields for optimized search.
 * This matches the internal structure used in airportService.
 */
export interface PrecomputedAirport {
  __lcIcao: string;
  __lcIata: string;
  __lcName: string;
  __lcCity: string;
}

/**
 * Interface for normalized fields used in legacy scoring.
 */
interface LcFields {
  lcIcao: string;
  lcIata: string;
  lcName: string;
  lcCity: string;
}

// --- Optimized Scorers (Direct Property Access) ---

function scoreIcaoOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  const term = query.term;
  const val = airport.__lcIcao;
  return (val === term) ? 1000 : (val.startsWith(term) ? 500 : 0);
}

function scoreIataOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  const term = query.term;
  const val = airport.__lcIata;
  if (!val) return 0;
  return (val === term) ? 900 : (val.startsWith(term) ? 450 : 0);
}

function scoreNameOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  const term = query.term;
  const val = airport.__lcName;
  if (!val) return 0;
  return val.startsWith(term) ? 300 : (val.includes(term) ? 100 : 0);
}

function scoreCityOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  const term = query.term;
  const val = airport.__lcCity;
  if (!val) return 0;
  return val.startsWith(term) ? 250 : (val.includes(term) ? 80 : 0);
}

/**
 * Optimized scoring function that uses precomputed fields to avoid allocation.
 *
 * @param airport - The airport object with precomputed __lc fields
 * @param query - The search query object containing the normalized term
 * @returns Numeric relevance score
 */
export function computeScoreOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  return scoreIcaoOptimized(airport, query) +
         scoreIataOptimized(airport, query) +
         scoreNameOptimized(airport, query) +
         scoreCityOptimized(airport, query);
}

// --- Legacy Scorers (Computed Fields Access) ---

function scoreIcaoLegacy(fields: LcFields, query: SearchQuery): number {
  const term = query.term;
  const val = fields.lcIcao;
  return (val === term) ? 1000 : (val.startsWith(term) ? 500 : 0);
}

function scoreIataLegacy(fields: LcFields, query: SearchQuery): number {
  const term = query.term;
  const val = fields.lcIata;
  if (!val) return 0;
  return (val === term) ? 900 : (val.startsWith(term) ? 450 : 0);
}

function scoreNameLegacy(fields: LcFields, query: SearchQuery): number {
  const term = query.term;
  const val = fields.lcName;
  if (!val) return 0;
  return val.startsWith(term) ? 300 : (val.includes(term) ? 100 : 0);
}

function scoreCityLegacy(fields: LcFields, query: SearchQuery): number {
  const term = query.term;
  const val = fields.lcCity;
  if (!val) return 0;
  return val.startsWith(term) ? 250 : (val.includes(term) ? 80 : 0);
}

/**
 * Computes a composite relevance score for an airport against a search term.
 *
 * The score combines matches against the airport's ICAO, IATA, name, and city fields with predefined weights.
 *
 * @param airport - The airport object to score
 * @param query - The search query object containing the normalized term
 * @returns A numeric relevance score (higher means a better match)
 */
export function computeScore(airport: Airport, query: SearchQuery): number {
  // Check if we already have precomputed fields to avoid getLcFields overhead
  // This is a safety optimization, though typically computeScore is used for single/test cases
  const fields = getLcFields(airport);

  // Create a new SearchQuery if term is not already lowercase (not needed here as caller handles it, but good for safety)
  // Assuming query.term is normalized as per contract.

  return scoreIcaoLegacy(fields, query) +
         scoreIataLegacy(fields, query) +
         scoreNameLegacy(fields, query) +
         scoreCityLegacy(fields, query);
}

/**
 * Provide lowercase canonical ICAO, IATA, name, and city fields for an airport, preferring precomputed `__lc*` values.
 *
 * @param airport - The airport object to extract fields from
 * @returns An object with `lcIcao`, `lcIata`, `lcName`, and `lcCity` — each a lowercase string taken from the corresponding `__lc*` field when it is a string, or derived from the airport's `icao`, `iata`, `name`, or `city` property coerced to string and lowercased otherwise.
 */
function getLcFields(airport: Airport): LcFields {
  const lcIcao = typeof airport.__lcIcao === 'string' ? airport.__lcIcao : String(airport.icao ?? '').toLowerCase();
  const lcIata = typeof airport.__lcIata === 'string' ? airport.__lcIata : String(airport.iata ?? '').toLowerCase();
  const lcName = typeof airport.__lcName === 'string' ? airport.__lcName : String(airport.name ?? '').toLowerCase();
  const lcCity = typeof airport.__lcCity === 'string' ? airport.__lcCity : String(airport.city ?? '').toLowerCase();

  return { lcIcao, lcIata, lcName, lcCity };
}
