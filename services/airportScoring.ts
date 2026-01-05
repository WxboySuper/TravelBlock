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
  // Prefer precomputed normalized fields (e.g. __lcName). Use a small loop
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const airportAny: any = airport as any;
  type OutKey = 'lcIcao' | 'lcIata' | 'lcName' | 'lcCity';
  const fields: Array<[OutKey, string, keyof Airport]> = [
    ['lcIcao', '__lcIcao', 'icao'],
    ['lcIata', '__lcIata', 'iata'],
    ['lcName', '__lcName', 'name'],
    ['lcCity', '__lcCity', 'city'],
  ];

  const result: Record<OutKey, string> = {} as Record<OutKey, string>;
  for (const [outKey, preKey, fallbackKey] of fields) {
    const preVal = airportAny[preKey];
    if (typeof preVal === 'string') {
      result[outKey] = preVal;
    } else {
      result[outKey] = String(airportAny[fallbackKey] ?? '').toLowerCase();
    }
  }

  return {
    lcIcao: result.lcIcao,
    lcIata: result.lcIata,
    lcName: result.lcName,
    lcCity: result.lcCity,
  };
}
