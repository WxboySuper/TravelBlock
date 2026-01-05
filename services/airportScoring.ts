import type { Airport } from '../types/airport';

function scoreIcao(lcIcao: string, searchTerm: string): number {
  if (lcIcao === searchTerm) return 1000;
  if (lcIcao.startsWith(searchTerm)) return 500;
  return 0;
}

function scoreIata(lcIata: string, searchTerm: string): number {
  if (!lcIata) return 0;
  if (lcIata === searchTerm) return 900;
  if (lcIata.startsWith(searchTerm)) return 450;
  return 0;
}

function scoreName(lcName: string, searchTerm: string): number {
  if (!lcName) return 0;
  if (lcName.startsWith(searchTerm)) return 300;
  if (lcName.includes(searchTerm)) return 100;
  return 0;
}

function scoreCity(lcCity: string, searchTerm: string): number {
  if (!lcCity) return 0;
  if (lcCity.startsWith(searchTerm)) return 250;
  if (lcCity.includes(searchTerm)) return 80;
  return 0;
}

export function computeScore(airport: Airport, searchTerm: string): number {
  const normalizedSearch = searchTerm.toLowerCase();
  const { lcIcao, lcIata, lcName, lcCity } = getLcFields(airport);
  return scoreIcao(lcIcao, normalizedSearch) + scoreIata(lcIata, normalizedSearch) + scoreName(lcName, normalizedSearch) + scoreCity(lcCity, normalizedSearch);
}

function getLcFields(airport: Airport) {
  const lcIcao = typeof airport.__lcIcao === 'string' ? airport.__lcIcao : String(airport.icao ?? '').toLowerCase();
  const lcIata = typeof airport.__lcIata === 'string' ? airport.__lcIata : String(airport.iata ?? '').toLowerCase();
  const lcName = typeof airport.__lcName === 'string' ? airport.__lcName : String(airport.name ?? '').toLowerCase();
  const lcCity = typeof airport.__lcCity === 'string' ? airport.__lcCity : String(airport.city ?? '').toLowerCase();

  return { lcIcao, lcIata, lcName, lcCity };
}
