/**
 * Distance calculation utilities using the Haversine formula.
 * 
 * This module provides functions to calculate the great circle distance between
 * two points on Earth given their latitude and longitude coordinates.
 * 
 * @module utils/distance
 */

/**
 * Represents a geographic coordinate with latitude and longitude.
 */
export interface Coordinates {
  /** Latitude in decimal degrees (-90 to 90) */
  lat: number;
  /** Longitude in decimal degrees (-180 to 180) */
  lon: number;
}

/**
 * Earth's radius in miles.
 * Mean radius as defined by the International Union of Geodesy and Geophysics (IUGG).
 */
const EARTH_RADIUS_MILES = 3959;

/**
 * Earth's radius in kilometers.
 * Mean radius as defined by the International Union of Geodesy and Geophysics (IUGG).
 */
const EARTH_RADIUS_KM = 6371;

/**
 * Converts degrees to radians.
 * 
 * @param degrees - Angle in degrees
 * @returns Angle in radians
 * 
 * @example
 * ```typescript
 * const radians = degreesToRadians(180); // Returns Math.PI
 * ```
 */
export function degreesToRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Ensure a latitude value is between -90 and 90 degrees inclusive.
 *
 * @param lat - Latitude in degrees.
 * @throws Error if `lat` is less than -90 or greater than 90.
 */
function validateLatitude(lat: number): void {
  if (lat < -90 || lat > 90) {
    throw new Error('Latitude must be between -90 and 90 degrees');
  }
}

/**
 * Ensures a longitude value is between -180 and 180 degrees.
 *
 * @param lon - The longitude in decimal degrees
 * @throws Error if `lon` is less than -180 or greater than 180
 */
function validateLongitude(lon: number): void {
  if (lon < -180 || lon > 180) {
    throw new Error('Longitude must be between -180 and 180 degrees');
  }
}

/**
 * Ensure both coordinates have latitudes between -90 and 90 and longitudes between -180 and 180.
 */
function validateCoordinates(point1: Coordinates, point2: Coordinates): void {
  validateLatitude(point1.lat);
  validateLatitude(point2.lat);
  validateLongitude(point1.lon);
  validateLongitude(point2.lon);
}

/**
 * Compute the great-circle angular distance between two geographic points.
 *
 * @param point1 - First geographic coordinate with `lat` and `lon` in degrees
 * @param point2 - Second geographic coordinate with `lat` and `lon` in degrees
 * @returns The angular distance between `point1` and `point2` in radians
 */
function haversineAngularDistance(point1: Coordinates, point2: Coordinates): number {
  const φ1 = degreesToRadians(point1.lat);
  const φ2 = degreesToRadians(point2.lat);
  const Δφ = degreesToRadians(point2.lat - point1.lat);
  const Δλ = degreesToRadians(point2.lon - point1.lon);

  const haversine =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

  return 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
}

/**
 * Calculates the great circle distance between two points on Earth using the Haversine formula.
 * Returns the distance in miles.
 * 
 * The Haversine formula determines the great-circle distance between two points on a sphere
 * given their longitudes and latitudes. This is the shortest distance over the earth's surface,
 * giving an "as-the-crow-flies" distance (ignoring hills, valleys, and other terrain features).
 * 
 * Formula:
 * haversine = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
 * angularDistance = 2 * atan2(√haversine, √(1−haversine))
 * distance = R * angularDistance
 * 
 * Where:
 * - φ = latitude in radians
 * - λ = longitude in radians
 * - R = Earth's radius (3,959 miles)
 * - Δφ = difference in latitude
 * - Δλ = difference in longitude
 * 
 * @param lat1 - Latitude of the first point in decimal degrees
 * @param lon1 - Longitude of the first point in decimal degrees
 * @param lat2 - Latitude of the second point in decimal degrees
 * @param lon2 - Longitude of the second point in decimal degrees
 * @returns Distance in miles between the two points
 * 
 * @example
 * ```typescript
 * // Distance from New York City to Los Angeles
 * const distance = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
 * console.log(distance); // Approximately 2,451 miles
 * ```
 * 
 * @see {@link https://en.wikipedia.org/wiki/Haversine_formula|Haversine formula on Wikipedia}
 */
export function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number;
export function calculateDistance(point1: Coordinates, point2: Coordinates): number;
/**
 * Compute the great-circle distance in miles between two geographic points.
 *
 * @param a - Either the first `Coordinates` object or the latitude of the first point
 * @param b - Either the second `Coordinates` object or the longitude of the first point
 * @param c - Latitude of the second point when using numeric coordinates
 * @param d - Longitude of the second point when using numeric coordinates
 * @returns The distance between the two points in miles along the Earth's surface
 */
export function calculateDistance(
  a: number | Coordinates,
  b: number | Coordinates,
  c?: number,
  d?: number
): number {
  const [point1, point2] = normalizeToPoints(a, b, c, d);
  return calculateDistanceWithRadius(point1, point2, EARTH_RADIUS_MILES);
}

/**
 * Calculates the great circle distance between two points on Earth using the Haversine formula.
 * Returns the distance in kilometers.
 * 
 * This is a convenience wrapper around the core Haversine calculation that returns
 * the result in kilometers instead of miles.
 * 
 * @param lat1 - Latitude of the first point in decimal degrees
 * @param lon1 - Longitude of the first point in decimal degrees
 * @param lat2 - Latitude of the second point in decimal degrees
 * @param lon2 - Longitude of the second point in decimal degrees
 * @returns Distance in kilometers between the two points
 * 
 * @example
 * ```typescript
 * // Distance from London to Paris
 * const distance = calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
 * console.log(distance); // Approximately 344 km
 * ```
 * 
 * @see {@link calculateDistance} for the miles version
 */
export function calculateDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number;
export function calculateDistanceKm(point1: Coordinates, point2: Coordinates): number;
/**
 * Compute the great-circle distance between two geographic points in kilometers.
 *
 * Accepts either two `Coordinates` objects (as `a` and `b`) or four numeric arguments (`lat1, lon1, lat2, lon2`).
 *
 * @param a - First point or latitude of the first point when using numeric arguments
 * @param b - Second point or longitude of the first point when using numeric arguments
 * @param c - Latitude of the second point when using numeric arguments
 * @param d - Longitude of the second point when using numeric arguments
 * @returns The distance between the two points in kilometers
 */
export function calculateDistanceKm(
  a: number | Coordinates,
  b: number | Coordinates,
  c?: number,
  d?: number
): number {
  const [point1, point2] = normalizeToPoints(a, b, c, d);
  return calculateDistanceWithRadius(point1, point2, EARTH_RADIUS_KM);
}

/**
 * Convert the provided arguments into a two-element array of Coordinates.
 *
 * @param a - Either a Coordinates object representing the first point or the numeric `lat1` value
 * @param b - Either a Coordinates object representing the second point or the numeric `lon1` value
 * @param c - The numeric `lat2` value when `a` is numeric
 * @param d - The numeric `lon2` value when `a` is numeric
 * @returns A tuple `[point1, point2]` of `Coordinates`
 * @throws Error if arguments do not match either `(point1, point2)` or `(lat1, lon1, lat2, lon2)` forms
 */
function normalizeToPoints(
  a: number | Coordinates,
  b: number | Coordinates,
  c?: number,
  d?: number
): [Coordinates, Coordinates] {
  if (isCoordinates(a) && isCoordinates(b)) {
    return [a as Coordinates, b as Coordinates];
  }

  if (isNumbersTuple(a, b, c, d)) {
    return normalizeFromNumbersTuple([a as number, b as number, c as number, d as number]);
  }

  throw new Error('Invalid arguments: expected (lat1, lon1, lat2, lon2) or (point1, point2)');
}
/** Type-guard: true when the value looks like `Coordinates` */
function isCoordinates(value: unknown): value is Coordinates {
  const candidate = value as Partial<Coordinates>;
  return (
    typeof value === 'object' &&
    value !== null &&
    !Array.isArray(value) &&
    typeof candidate.lat === 'number' &&
    typeof candidate.lon === 'number' &&
    Number.isFinite(candidate.lat) &&
    Number.isFinite(candidate.lon)
  );
}

/** Predicate to verify the numeric 4-tuple path. Kept small to reduce complexity in the main flow. */
function isNumbersTuple(a: unknown, b: unknown, c: unknown, d: unknown): boolean {
  return (
    typeof a === 'number' &&
    typeof b === 'number' &&
    typeof c === 'number' &&
    typeof d === 'number'
  );
}

/**
 * Convert a numeric tuple [lat1, lon1, lat2, lon2] into two Coordinate objects.
 *
 * @param nums - Four numbers in the order: `lat1, lon1, lat2, lon2` (latitude and longitude in degrees)
 * @returns A two-element array `[point1, point2]` where `point1` is `{ lat: lat1, lon: lon1 }` and `point2` is `{ lat: lat2, lon: lon2 }`
 * @throws Error if any tuple element is not a number or is `NaN`
 */
>>>>>>> coderabbitai/docstrings/d448057
function normalizeFromNumbersTuple(nums: [number, number, number, number]): [Coordinates, Coordinates] {
  const [lat1, lon1, lat2, lon2] = nums;
  if ([lat1, lon1, lat2, lon2].some((v) => typeof v !== 'number' || Number.isNaN(v))) {
    throw new Error('Invalid numeric arguments for coordinates');
  }

  return [
    { lat: lat1, lon: lon1 },
    { lat: lat2, lon: lon2 },
  ];
}

/**
 * Compute the surface distance between two coordinates using the supplied sphere radius.
 *
 * @param point1 - The first geographic coordinate (latitude/longitude)
 * @param point2 - The second geographic coordinate (latitude/longitude)
 * @param radius - Radius of the sphere to use for the calculation; the returned distance is in the same units as this value
 * @returns The distance between `point1` and `point2` in the same units as `radius`
 */
function calculateDistanceWithRadius(
  point1: Coordinates,
  point2: Coordinates,
  radius: number
): number {
  validateCoordinates(point1, point2);
  const angularDistance = haversineAngularDistance(point1, point2);
  return radius * angularDistance;
}

/**
 * Calculates the distance between two Coordinates objects in miles.
 * 
 * This is a convenience function that accepts Coordinates objects instead of
 * individual latitude and longitude parameters.
 * 
 * @param point1 - First coordinate point
 * @param point2 - Second coordinate point
 * @returns Distance in miles between the two points
 * 
 * @example
 * ```typescript
 * const nyc: Coordinates = { lat: 40.7128, lon: -74.0060 };
 * const la: Coordinates = { lat: 34.0522, lon: -118.2437 };
 * const distance = calculateDistanceBetweenPoints(nyc, la);
 * ```
 */
export function calculateDistanceBetweenPoints(
  point1: Coordinates,
  point2: Coordinates
): number {
  return calculateDistance(point1, point2);
}

/**
 * Calculates the distance between two Coordinates objects in kilometers.
 * 
 * This is a convenience function that accepts Coordinates objects instead of
 * individual latitude and longitude parameters.
 * 
 * @param point1 - First coordinate point
 * @param point2 - Second coordinate point
 * @returns Distance in kilometers between the two points
 * 
 * @example
 * ```typescript
 * const london: Coordinates = { lat: 51.5074, lon: -0.1278 };
 * const paris: Coordinates = { lat: 48.8566, lon: 2.3522 };
 * const distance = calculateDistanceBetweenPointsKm(london, paris);
 * ```
 */
export function calculateDistanceBetweenPointsKm(
  point1: Coordinates,
  point2: Coordinates
): number {
  return calculateDistanceKm(point1, point2);
}