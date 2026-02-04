import { isValidAirport } from '../../utils/validation';

describe('isValidAirport', () => {
  const validAirport = {
    icao: 'KJFK',
    iata: 'JFK',
    name: 'John F Kennedy International Airport',
    city: 'New York',
    state: 'NY',
    country: 'US',
    elevation: 13,
    lat: 40.6413,
    lon: -73.7781,
    tz: 'America/New_York',
  };

  test('returns true for a valid airport object', () => {
    expect(isValidAirport(validAirport)).toBe(true);
  });

  test('returns true when iata field is empty string', () => {
    const emptyIata = { ...validAirport, iata: '' };
    expect(isValidAirport(emptyIata)).toBe(true);
  });

  test('returns false for null', () => {
    expect(isValidAirport(null)).toBe(false);
  });

  test('returns false for undefined', () => {
    expect(isValidAirport(undefined)).toBe(false);
  });

  test('returns false for non-object', () => {
    expect(isValidAirport('not an object')).toBe(false);
    expect(isValidAirport(123)).toBe(false);
  });

  test('returns false when required string fields are missing', () => {
     
    const { name, ...missingName } = validAirport;
    expect(isValidAirport(missingName)).toBe(false);

     
    const { icao, ...missingIcao } = validAirport;
    expect(isValidAirport(missingIcao)).toBe(false);

     
    const { state, ...missingState } = validAirport;
    expect(isValidAirport(missingState)).toBe(false);

     
    const { tz, ...missingTz } = validAirport;
    expect(isValidAirport(missingTz)).toBe(false);
  });

  test('returns false when required string fields are wrong type', () => {
    const wrongName = { ...validAirport, name: 123 };
    expect(isValidAirport(wrongName)).toBe(false);

    const wrongTz = { ...validAirport, tz: null };
    expect(isValidAirport(wrongTz)).toBe(false);
  });

  test('returns false when required numeric fields are missing', () => {
     
    const { lat, ...missingLat } = validAirport;
    expect(isValidAirport(missingLat)).toBe(false);

     
    const { elevation, ...missingElevation } = validAirport;
    expect(isValidAirport(missingElevation)).toBe(false);
  });

  test('returns false when numeric fields are not finite numbers', () => {
    const infiniteLat = { ...validAirport, lat: Infinity };
    expect(isValidAirport(infiniteLat)).toBe(false);

    const nanLon = { ...validAirport, lon: NaN };
    expect(isValidAirport(nanLon)).toBe(false);

    const stringLat = { ...validAirport, lat: '40.6413' };
    expect(isValidAirport(stringLat)).toBe(false);
  });

  test('returns false when iata is present but not a string', () => {
    const wrongIata = { ...validAirport, iata: 123 };
    expect(isValidAirport(wrongIata)).toBe(false);
  });
});
