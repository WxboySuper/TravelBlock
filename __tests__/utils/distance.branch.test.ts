import {
  calculateDistance,
  calculateDistanceKm,
  Coordinates,
} from '../../utils/distance';

describe('Distance utilities - branches and invalid inputs', () => {
  it('throws when given invalid coordinate objects', () => {
    // should reject malformed coordinate objects
    expect(() => calculateDistance({} as unknown as Coordinates, { lat: 0, lon: 0 })).toThrow(/Invalid arguments/);
  });

  it('throws when numeric arguments contain NaN', () => {
    expect(() => calculateDistance(NaN, 0, 0, 0)).toThrow(/Invalid numeric arguments/);
  });

  it('throws when latitude is out of range', () => {
    expect(() => calculateDistance(100, 0, 0, 0)).toThrow(/Latitude must be between/);
  });

  it('throws when longitude is out of range', () => {
    expect(() => calculateDistance(0, 200, 0, 0)).toThrow(/Longitude must be between/);
  });

  it('numeric signature returns same result as object signature (miles)', () => {
    const nyc = { lat: 40.7128, lon: -74.006 } as Coordinates;
    const la = { lat: 34.0522, lon: -118.2437 } as Coordinates;

    const numeric = calculateDistance(nyc.lat, nyc.lon, la.lat, la.lon);
    const objects = calculateDistance(nyc, la);

    expect(numeric).toBeCloseTo(objects, 6);
  });

  it('numeric signature returns same result as object signature (km)', () => {
    const london = { lat: 51.5074, lon: -0.1278 } as Coordinates;
    const paris = { lat: 48.8566, lon: 2.3522 } as Coordinates;

    const numericKm = calculateDistanceKm(london.lat, london.lon, paris.lat, paris.lon);
    const objectsKm = calculateDistanceKm(london, paris);

    expect(numericKm).toBeCloseTo(objectsKm, 6);
  });
});
