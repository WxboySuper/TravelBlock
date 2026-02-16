/**
 * Flight route interpolation utilities
 * 
 * Provides functions for calculating positions along great-circle routes,
 * bearings, and generating waypoints for map visualization.
 * 
 * Uses spherical geometry for accurate geodesic calculations.
 * 
 * @module utils/flightInterpolation
 */

import type { Airport } from '@/types/airport';

/**
 * Position coordinate
 */
/**
 * Position coordinate
 */
export interface Position {
  lat: number;
  lon: number;
}

/**
 * Extract lat/lon from Airport or Position
 */
function extractCoords(point: Position | Airport): { lat: number; lon: number } {
  if ('lat' in point && 'lon' in point) {
    return { lat: point.lat, lon: point.lon };
  }
  throw new Error('Invalid position object');
}

/**
 * Earth radius in kilometers (mean radius)
 */
const EARTH_RADIUS_KM = 6371;

/**
 * Convert degrees to radians
 */
function degreesToRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Convert radians to degrees
 */
function radiansToDegrees(radians: number): number {
  return radians * (180 / Math.PI);
}

/**
 * Normalize angle to 0-360 degrees range
 */
function normalizeAngle(degrees: number): number {
  degrees = degrees % 360;
  if (degrees < 0) {
    degrees += 360;
  }
  return degrees;
}

/**
 * Calculate bearing (initial heading) from one point to another
 * 
 * Uses the forward azimuth formula for bearings on a sphere.
 * Returns the initial bearing (forward azimuth) which may vary
 * along a great circle route.
 * 
 * @param from - Starting position
 * @param to - Ending position
 * @returns Bearing in degrees (0-360, where 0/360 is North)
 * 
 * @example
 * ```typescript
 * const bearing = calculateBearing(
 *   { lat: 40.7128, lon: -74.0060 }, // NYC
 *   { lat: 51.5074, lon: -0.1278 }   // London
 * );
 * console.log(bearing); // ~51° (Northeast)
 * ```
 */
export function calculateBearing(from: Position, to: Position): number {
  const lat1 = degreesToRadians(from.lat);
  const lat2 = degreesToRadians(to.lat);
  const dLon = degreesToRadians(to.lon - from.lon);

  const y = Math.sin(dLon) * Math.cos(lat2);
  const x = Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
  
  const bearing = Math.atan2(y, x);
  
  return normalizeAngle(radiansToDegrees(bearing));
}

/**
 * Interpolate position along great-circle route
 * 
 * Uses spherical linear interpolation (slerp) to calculate a position
 * between two points on a sphere. This provides accurate positions along
 * the shortest path (great circle) between origin and destination.
 * 
 * @param origin - Starting airport or position
 * @param destination - Ending airport or position
 * @param progress - Progress along route (0-1, where 0=origin, 1=destination)
 * @returns Interpolated position
 * 
 * @example
 * ```typescript
 * // Get position at 50% of flight
 * const midpoint = interpolatePosition(jfk, lhr, 0.5);
 * 
 * // Get position at 75% of flight
 * const nearDest = interpolatePosition(jfk, lhr, 0.75);
 * ```
 */
export function interpolatePosition(
  origin: Position | Airport,
  destination: Position | Airport,
  progress: number
): Position {
  // Clamp progress to 0-1 range
  progress = Math.max(0, Math.min(1, progress));

  // Extract coordinates
  const originCoords = extractCoords(origin);
  const destCoords = extractCoords(destination);
  const lat1 = originCoords.lat;
  const lon1 = originCoords.lon;
  const lat2 = destCoords.lat;
  const lon2 = destCoords.lon;

  // Handle edge cases
  if (progress === 0) {
    return { lat: lat1, lon: lon1 };
  }
  if (progress === 1) {
    return { lat: lat2, lon: lon2 };
  }

  // Convert to radians
  const phi1 = degreesToRadians(lat1);
  const phi2 = degreesToRadians(lat2);
  const lambda1 = degreesToRadians(lon1);
  const lambda2 = degreesToRadians(lon2);

  // Calculate angular distance between points
  const dPhi = phi2 - phi1;
  const dLambda = lambda2 - lambda1;

  const a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2) +
            Math.cos(phi1) * Math.cos(phi2) *
            Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
  const angularDistance = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  // Handle very short distances (no interpolation needed)
  if (angularDistance < 0.00001) {
    return { lat: lat1, lon: lon1 };
  }

  // Spherical linear interpolation (slerp)
  const A = Math.sin((1 - progress) * angularDistance) / Math.sin(angularDistance);
  const B = Math.sin(progress * angularDistance) / Math.sin(angularDistance);

  const x = A * Math.cos(phi1) * Math.cos(lambda1) + B * Math.cos(phi2) * Math.cos(lambda2);
  const y = A * Math.cos(phi1) * Math.sin(lambda1) + B * Math.cos(phi2) * Math.sin(lambda2);
  const z = A * Math.sin(phi1) + B * Math.sin(phi2);

  const phi = Math.atan2(z, Math.sqrt(x * x + y * y));
  const lambda = Math.atan2(y, x);

  return {
    lat: radiansToDegrees(phi),
    lon: radiansToDegrees(lambda),
  };
}

/**
 * Generate waypoints along great-circle route for map polyline
 * 
 * Creates an array of evenly-spaced positions along the route between
 * origin and destination. Useful for drawing curved paths on maps.
 * 
 * @param origin - Starting airport or position
 * @param destination - Ending airport or position
 * @param numPoints - Number of waypoints to generate (default: 100)
 * @returns Array of positions along the route
 * 
 * @example
 * ```typescript
 * // Generate 100 waypoints for smooth map polyline
 * const waypoints = generateRouteWaypoints(jfk, lhr, 100);
 * 
 * // Use with react-native-maps Polyline
 * <Polyline coordinates={waypoints} />
 * ```
 */
export function generateRouteWaypoints(
  origin: Position | Airport,
  destination: Position | Airport,
  numPoints: number = 100
): Position[] {
  if (numPoints < 2) {
    throw new Error('numPoints must be at least 2');
  }

  const waypoints: Position[] = [];
  
  // Always include origin
  const originCoords = extractCoords(origin);
  waypoints.push(originCoords);

  // Generate intermediate points
  for (let i = 1; i < numPoints - 1; i++) {
    const progress = i / (numPoints - 1);
    waypoints.push(interpolatePosition(origin, destination, progress));
  }

  // Always include destination
  const destCoords = extractCoords(destination);
  waypoints.push(destCoords);

  return waypoints;
}

/**
 * Calculate distance between two positions in kilometers
 * 
 * Uses the Haversine formula to calculate great-circle distance.
 * This is a convenience wrapper that returns kilometers directly.
 * 
 * @param from - Starting position
 * @param to - Ending position
 * @returns Distance in kilometers
 * 
 * @example
 * ```typescript
 * const distance = calculatePositionDistance(
 *   { lat: 40.7128, lon: -74.0060 },
 *   { lat: 51.5074, lon: -0.1278 }
 * );
 * console.log(distance); // ~5,570 km
 * ```
 */
export function calculatePositionDistance(from: Position, to: Position): number {
  const lat1 = degreesToRadians(from.lat);
  const lat2 = degreesToRadians(to.lat);
  const dLat = degreesToRadians(to.lat - from.lat);
  const dLon = degreesToRadians(to.lon - from.lon);

  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  
  return EARTH_RADIUS_KM * c;
}

/**
 * Calculate intermediate bearing at a given progress along the route
 * 
 * Unlike initial bearing, this calculates the bearing at a specific point
 * along the route, which changes along a great circle path.
 * 
 * @param origin - Starting position
 * @param destination - Ending position
 * @param progress - Progress along route (0-1)
 * @returns Bearing at that point in degrees (0-360)
 * 
 * @example
 * ```typescript
 * // Get heading at 50% of flight
 * const heading = calculateIntermediateBearing(jfk, lhr, 0.5);
 * ```
 */
export function calculateIntermediateBearing(
  origin: Position | Airport,
  destination: Position | Airport,
  progress: number
): number {
  // Get current position
  const currentPos = interpolatePosition(origin, destination, progress);
  
  // Calculate bearing from current position to destination
  // This gives us the heading we'd be flying at this point
  const destPos = extractCoords(destination);
  
  return calculateBearing(currentPos, destPos);
}
