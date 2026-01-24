import { computeScore } from '../../services/airportScoring';
import type { Airport } from '../../types/airport';

describe('airportScoring zero branches', () => {
  it('returns 0 when no fields match search', () => {
    const airport: Airport = {
      icao: 'NONE',
      iata: 'NN',
      name: 'No Match Airport',
      city: 'NoCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'zzzzzz' });
    expect(score).toBe(0);
  });

  it('returns 0 for entirely empty identifiable fields', () => {
    const airport: Airport = {
      icao: '',
      iata: '',
      name: '',
      city: '',
      state: '',
      country: '',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'anything' });
    expect(score).toBe(0);
  });

  it('handles missing fields (undefined) without throwing', () => {
    // Omit icao/iata/name/city properties entirely to hit the nullish fallback
    const airport = {} as unknown as Airport;
    const score = computeScore(airport, { term: 'anything' });
    expect(score).toBe(0);
  });
});
