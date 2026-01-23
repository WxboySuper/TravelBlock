import type { Airport } from '../types/airport';

/**
 * Compute a numeric match score for a lowercase ICAO code against a lowercase search term.
 *
 * @param lcIcao - The airport's ICAO code in lowercase.
 * @param searchTerm - The search term in lowercase to match against `lcIcao`.
 * @returns `1000` if `lcIcao` exactly equals `searchTerm`, `500` if `lcIcao` starts with `searchTerm`, `0` otherwise.
 */
function scoreIcao(lcIcao: string, searchTerm: string): number {
  if (lcIcao === searchTerm) return 1000;
  if (lcIcao.startsWith(searchTerm)) return 500;
  return 0;
}

/**
 * Assigns a relevance score for a lowercase IATA code against the normalized search term.
 *
 * @param lcIata - The airport's IATA code in lowercase (empty string treated as missing)
 * @param searchTerm - The normalized search term in lowercase
 * @returns `900` if `lcIata` exactly equals `searchTerm`, `450` if `lcIata` starts with `searchTerm`, `0` otherwise
 */
function scoreIata(lcIata: string, searchTerm: string): number {
  if (!lcIata) return 0;
  if (lcIata === searchTerm) return 900;
  if (lcIata.startsWith(searchTerm)) return 450;
  return 0;
}

/**
 * Assigns a relevance score for an airport name based on prefix and substring matches.
 *
 * @param lcName - The airport name in lowercase (preferably pre-normalized)
 * @param searchTerm - The search term in lowercase used for matching
 * @returns `300` if `lcName` starts with `searchTerm`, `100` if `lcName` contains `searchTerm`, `0` if `lcName` is empty or there is no match
 */
function scoreName(lcName: string, searchTerm: string): number {
  if (!lcName) return 0;
  if (lcName.startsWith(searchTerm)) return 300;
  if (lcName.includes(searchTerm)) return 100;
  return 0;
}

/**
 * Scores how well a lowercase city field matches a lowercase search term.
 *
 * @param lcCity - City name already normalized to lowercase
 * @param searchTerm - Search term already normalized to lowercase
 * @returns `250` if `lcCity` starts with `searchTerm`, `80` if `lcCity` contains `searchTerm`, `0` if `lcCity` is empty or there is no match
 */
function scoreCity(lcCity: string, searchTerm: string): number {
  if (!lcCity) return 0;
  if (lcCity.startsWith(searchTerm)) return 250;
  if (lcCity.includes(searchTerm)) return 80;
  return 0;
}

/**
 * Computes a composite relevance score for an airport against a search term.
 *
 * The score combines matches against the airport's ICAO, IATA, name, and city fields with predefined weights.
 *
 * @param airport - The airport object to score
 * @param searchTerm - The search term used to evaluate relevance
 * @returns A numeric relevance score (higher means a better match)
 */
export function computeScore(airport: Airport, searchTerm: string): number {
  const normalizedSearch = searchTerm.toLowerCase();

  const lcIcao = typeof airport.__lcIcao === 'string' ? airport.__lcIcao : String(airport.icao ?? '').toLowerCase();
  const lcIata = typeof airport.__lcIata === 'string' ? airport.__lcIata : String(airport.iata ?? '').toLowerCase();
  const lcName = typeof airport.__lcName === 'string' ? airport.__lcName : String(airport.name ?? '').toLowerCase();
  const lcCity = typeof airport.__lcCity === 'string' ? airport.__lcCity : String(airport.city ?? '').toLowerCase();

  return scoreIcao(lcIcao, normalizedSearch) + scoreIata(lcIata, normalizedSearch) + scoreName(lcName, normalizedSearch) + scoreCity(lcCity, normalizedSearch);
}
