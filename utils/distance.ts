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
export function calculateDistance(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  // Convert latitude and longitude from degrees to radians
  const φ1 = degreesToRadians(lat1);
  const φ2 = degreesToRadians(lat2);
  const Δφ = degreesToRadians(lat2 - lat1);
  const Δλ = degreesToRadians(lon2 - lon1);

  // Haversine formula
  // haversine = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
  const haversine =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

  // angularDistance = 2 * atan2(√haversine, √(1−haversine))
  const angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

  // Distance = Earth's radius * angularDistance
  const distance = EARTH_RADIUS_MILES * angularDistance;

  return distance;
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
export function calculateDistanceKm(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  // Convert latitude and longitude from degrees to radians
  const φ1 = degreesToRadians(lat1);
  const φ2 = degreesToRadians(lat2);
  const Δφ = degreesToRadians(lat2 - lat1);
  const Δλ = degreesToRadians(lon2 - lon1);

  // Haversine formula
  const haversine =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

  const angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

  // Distance = Earth's radius in km * angularDistance
  const distance = EARTH_RADIUS_KM * angularDistance;

  return distance;
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
  return calculateDistance(point1.lat, point1.lon, point2.lat, point2.lon);
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
  return calculateDistanceKm(point1.lat, point1.lon, point2.lat, point2.lon);
}
