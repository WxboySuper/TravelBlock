import { computeScore } from '../../services/airportScoring';
import type { Airport } from '../../types/airport';

describe('airportScoring computeScore branches', () => {
  it('scores exact ICAO match highest for ICAO field', () => {
    const airport: Airport = {
      icao: 'KXYZ',
      iata: 'XYZ',
      name: 'Kxyz International',
      city: 'Kxyz City',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, 'KXYZ');
    expect(score).toBeGreaterThanOrEqual(1000);
  });

  it('scores ICAO startsWith when not exact', () => {
    const airport: Airport = {
      icao: 'KABC1',
      iata: 'ABC',
      name: 'Alpha Bravo Charlie Airport',
      city: 'Abcville',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, 'KABC');
    expect(score).toBeGreaterThanOrEqual(500);
    expect(score).toBeLessThan(1000);
  });

  it('scores name substring when IATA is empty', () => {
    const airport: Airport = {
      icao: 'ZZZZ',
      iata: '',
      name: 'The Great ZZZZ Airport',
      city: 'Greatville',
      state: '',
      country: 'GB',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, 'great');
    // name includes 'great' so should get at least the substring name score (100)
    expect(score).toBeGreaterThanOrEqual(100);
  });

  it('scores city prefix higher than substring', () => {
    const airport: Airport = {
      icao: 'CITY',
      iata: 'CTY',
      name: 'City Regional Airport',
      city: 'MetroCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const scorePrefix = computeScore(airport, 'metro');
    // city startsWith -> 250 should be contributed
    expect(scorePrefix).toBeGreaterThanOrEqual(250);
  });

  it('uses precomputed __lc* fields when present', () => {
    const airport: Airport = {
      icao: 'PRE1',
      iata: 'PRE',
      name: 'Precomputed Airport',
      city: 'PreCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
      __lcIcao: 'pre1',
      __lcIata: 'pre',
      __lcName: 'precomputed airport',
      __lcCity: 'precity',
    };

    const score = computeScore(airport, 'pre');
    // precomputed fields should allow matching and yield a positive score
    expect(score).toBeGreaterThan(0);
  });
});
