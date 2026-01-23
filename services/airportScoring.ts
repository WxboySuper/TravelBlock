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
 * Computes a composite relevance score for an airport against a search term.
 *
 * The score combines matches against the airport's ICAO, IATA, name, and city fields with predefined weights.
 *
 * @param airport - The airport object to score
 * @param query - The search query object containing the normalized term
 * @returns A numeric relevance score (higher means a better match)
 */
export function computeScore(airport: Airport, query: SearchQuery): number {
  const normalizedSearch = query.term.toLowerCase();
  const { lcIcao, lcIata, lcName, lcCity } = getLcFields(airport);

  let score = 0;

  // ICAO
  score += (lcIcao === normalizedSearch) ? 1000 : (lcIcao.startsWith(normalizedSearch) ? 500 : 0);

  // IATA
  if (lcIata) {
    score += (lcIata === normalizedSearch) ? 900 : (lcIata.startsWith(normalizedSearch) ? 450 : 0);
  }

  // Name
  if (lcName) {
    score += lcName.startsWith(normalizedSearch) ? 300 : (lcName.includes(normalizedSearch) ? 100 : 0);
  }

  // City
  if (lcCity) {
    score += lcCity.startsWith(normalizedSearch) ? 250 : (lcCity.includes(normalizedSearch) ? 80 : 0);
  }

  return score;
}

/**
 * Optimized scoring function that uses precomputed fields to avoid allocation.
 *
 * @param airport - The airport object with precomputed __lc fields
 * @param query - The search query object containing the normalized term
 * @returns Numeric relevance score
 */
export function computeScoreOptimized(airport: PrecomputedAirport, query: SearchQuery): number {
  const term = query.term;
  let score = 0;

  // ICAO
  const lcIcao = airport.__lcIcao;
  score += (lcIcao === term) ? 1000 : (lcIcao.startsWith(term) ? 500 : 0);

  // IATA
  const lcIata = airport.__lcIata;
  if (lcIata) {
    score += (lcIata === term) ? 900 : (lcIata.startsWith(term) ? 450 : 0);
  }

  // Name
  const lcName = airport.__lcName;
  if (lcName) {
    score += lcName.startsWith(term) ? 300 : (lcName.includes(term) ? 100 : 0);
  }

  // City
  const lcCity = airport.__lcCity;
  if (lcCity) {
    score += lcCity.startsWith(term) ? 250 : (lcCity.includes(term) ? 80 : 0);
  }

  return score;
}

/**
 * Provide lowercase canonical ICAO, IATA, name, and city fields for an airport, preferring precomputed `__lc*` values.
 *
 * @param airport - The airport object to extract fields from
 * @returns An object with `lcIcao`, `lcIata`, `lcName`, and `lcCity` — each a lowercase string taken from the corresponding `__lc*` field when it is a string, or derived from the airport's `icao`, `iata`, `name`, or `city` property coerced to string and lowercased otherwise.
 */
function getLcFields(airport: Airport) {
  const lcIcao = typeof airport.__lcIcao === 'string' ? airport.__lcIcao : String(airport.icao ?? '').toLowerCase();
  const lcIata = typeof airport.__lcIata === 'string' ? airport.__lcIata : String(airport.iata ?? '').toLowerCase();
  const lcName = typeof airport.__lcName === 'string' ? airport.__lcName : String(airport.name ?? '').toLowerCase();
  const lcCity = typeof airport.__lcCity === 'string' ? airport.__lcCity : String(airport.city ?? '').toLowerCase();

  return { lcIcao, lcIata, lcName, lcCity };
}
