import { computeScore } from '../../services/airportScoring';
import type { Airport } from '../../types/airport';

describe('airportScoring extra branches', () => {
  it('gives high score for exact IATA match', () => {
    const airport: Airport = {
      icao: 'IIII',
      iata: 'TST',
      name: 'Test IATA Airport',
      city: 'TestCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'tst' });
    expect(score).toBeGreaterThanOrEqual(900);
  });

  it('gives mid score for IATA startsWith', () => {
    const airport: Airport = {
      icao: 'IIII',
      iata: 'tstx',
      name: 'Test IATA X Airport',
      city: 'TestCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'tst' });
    expect(score).toBeGreaterThanOrEqual(450);
    expect(score).toBeLessThan(900);
  });

  it('gives name prefix score when name startsWith search', () => {
    const airport: Airport = {
      icao: 'NNNN',
      iata: 'NNN',
      name: 'Great Airport Name',
      city: 'OtherCity',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'great' });
    expect(score).toBeGreaterThanOrEqual(300);
  });

  it('gives city substring score when city includes term but does not start with it', () => {
    const airport: Airport = {
      icao: 'SUB1',
      iata: 'SUB',
      name: 'Substring Airport',
      city: 'LovelyTown',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    };

    const score = computeScore(airport, { term: 'vely' });
    // city includes -> should get at least 80 from city score
    expect(score).toBeGreaterThanOrEqual(80);
    expect(score).toBeLessThan(250);
  });
});
