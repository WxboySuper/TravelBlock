/**
 * Location service for GPS permissions and coordinate-based airport finding.
 *
 * This service provides methods to:
 * - Request and check location permissions
 * - Get the user's current GPS coordinates
 * - Find the nearest airport to a given location
 *
 * **Permission Requirements**: All methods that access device location require
 * the `expo-location` permission to be granted by the user.
 *
 * @module services/locationService
 */

import {
  requestForegroundPermissionsAsync,
  getForegroundPermissionsAsync,
  getCurrentPositionAsync,
  Accuracy,
} from 'expo-location';
import type { Airport } from '../types/airport';
import type { Coordinates } from '../types/location';
import { loadAirports, getAirportsWithinDistance } from './airportService';

// Helper: validate a numeric coordinate is finite
function isFiniteNumber(n: unknown): n is number {
  return typeof n === 'number' && Number.isFinite(n);
}

// Helper: fetch current position and return validated coordinates or null
async function fetchCurrentPositionSafe(): Promise<Coordinates | null> {
  try {
    const location = await getCurrentPositionAsync({ accuracy: Accuracy.Balanced });
    const lat = location?.coords?.latitude;
    const lon = location?.coords?.longitude;
    if (!isFiniteNumber(lat) || !isFiniteNumber(lon)) {
      console.warn('fetchCurrentPositionSafe: non-finite coordinates received', {
        hasLat: isFiniteNumber(lat),
        hasLon: isFiniteNumber(lon),
      });
      return null;
    }
    return { lat, lon };
  } catch (err) {
    console.error('fetchCurrentPositionSafe: error fetching position', err);
    return null;
  }
}

// Helper: Resolve search coordinates (from argument or current location) and validate
async function getValidatedSearchCoordinates(coordinates?: Coordinates): Promise<Coordinates | null> {
  // Use provided coordinates or get current location
  const searchCoords = coordinates || (await getCurrentLocation());

  if (!searchCoords) {
    console.warn('No coordinates available for airport search');
    return null;
  }

  // Validate provided/search coordinates are finite numbers.
  if (!Number.isFinite(searchCoords.lat) || !Number.isFinite(searchCoords.lon)) {
    console.warn('Invalid search coordinates for airport search', searchCoords);
    return null;
  }

  return searchCoords;
}

// Helper: find nearest N airports for already-validated search coordinates
async function findNearestAirportsForCoordinates(searchCoords: Coordinates, limit: number): Promise<Airport[]> {
  await loadAirports();

  // Optimization: Try finding airports within 500 miles first (fast path using bounding box check)
  // This avoids scanning the entire 50,000+ airport database in most cases
  const nearby = getAirportsWithinDistance(searchCoords, 500);
  if (nearby.length >= limit) {
    return nearby.slice(0, limit);
  }

  // Fallback: If not enough airports found nearby, expand search to near-global range
  // 15,000 miles covers practically everywhere from any point on Earth
  return getAirportsWithinDistance(searchCoords, 15000).slice(0, limit);
}

/**
 * Requests permission to access the device's location.
 *
 * This method prompts the user for foreground location permission if it has not
 * been granted. The permission dialog is shown according to platform guidelines
 * (iOS and Android handle permission requests differently).
 *
 * **Permission Note**: This method must be called before accessing device location.
 * On iOS, the permission dialog will only be shown once per app installation.
 *
 * @returns Promise that resolves to `true` if permission is granted, `false` otherwise
 *
 * @example
 * ```typescript
 * const granted = await requestLocationPermission();
 * if (granted) {
 *   // Safe to call getCurrentLocation()
 * } else {
 *   // Show user a message explaining why location is needed
 * }
 * ```
 */
export async function requestLocationPermission(): Promise<boolean> {
  try {
    const { status } = await requestForegroundPermissionsAsync();
    return status === 'granted';
  } catch (error) {
    console.error('Error requesting location permission:', error);
    return false;
  }
}

/**
 * Checks if location permission has been granted.
 *
 * This method checks the current permission status without prompting the user.
 * Use this to determine if you need to call `requestLocationPermission()`.
 *
 * @returns Promise that resolves to `true` if permission is granted, `false` otherwise
 *
 * @example
 * ```typescript
 * const hasPermission = await hasLocationPermission();
 * if (!hasPermission) {
 *   // Request permission before getting location
 *   await requestLocationPermission();
 * }
 * ```
 */
export async function hasLocationPermission(): Promise<boolean> {
  try {
    const { status } = await getForegroundPermissionsAsync();
    return status === 'granted';
  } catch (error) {
    console.error('Error checking location permission:', error);
    return false;
  }
}

/**
 * Gets the device's current GPS coordinates.
 *
 * This method retrieves the user's current location using GPS/network positioning.
 * The accuracy and speed depend on device capabilities and available sensors.
 *
 * **Permission Note**: This method requires location permission. Call
 * `requestLocationPermission()` first if permission has not been granted.
 *
 * @returns Promise that resolves to current coordinates, or `null` if location cannot be retrieved
 *
 * @example
 * ```typescript
 * const location = await getCurrentLocation();
 * if (location) {
 *   console.log(`Lat: ${location.lat}, Lon: ${location.lon}`);
 * } else {
 *   console.log('Unable to get location');
 * }
 * ```
 */
export async function getCurrentLocation(): Promise<Coordinates | null> {
  try {
    // Check permission first; bail out early if not granted
    const hasPermission = await hasLocationPermission();
    if (!hasPermission) {
      console.warn('Location permission not granted');
      return null;
    }

    // Delegate to the safe fetch helper which performs validation and
    // handles native errors.
    return await fetchCurrentPositionSafe();
  } catch (error) {
    console.error('Error getting current location:', error);
    return null;
  }
}

/**
 * Finds the nearest airports to the given coordinates.
 *
 * @param limit - Maximum number of airports to return (default: 3)
 * @param coordinates - Optional coordinates to search from. If not provided, uses current device location
 * @returns Promise that resolves to an array of nearest airports
 */
export async function getNearestAirports(
  limit = 3,
  coordinates?: Coordinates
): Promise<Airport[]> {
  try {
    const searchCoords = await getValidatedSearchCoordinates(coordinates);
    if (!searchCoords) return [];

    // Delegate airport loading and nearest selection to helper
    return await findNearestAirportsForCoordinates(searchCoords, limit);
  } catch (error) {
    console.error('Error finding nearest airports:', error);
    throw error; // Re-throw to allow wrapper to handle/log specifically if needed, or caller to handle
  }
}

/**
 * Finds the nearest airport to the given coordinates.
 *
 * This method searches the entire airport database and returns the closest airport
 * based on great-circle distance (Haversine formula). If no coordinates are provided,
 * it attempts to use the device's current location.
 *
 * **Permission Note**: When called without coordinates, this method requires location
 * permission to access the device's current position.
 *
 * @param coordinates - Optional coordinates to search from. If not provided, uses current device location
 * @returns Promise that resolves to the nearest airport, or `null` if no location or airports found
 *
 * @example
 * ```typescript
 * // Find nearest airport to current location
 * const nearest = await getNearestAirport();
 *
 * // Find nearest airport to specific coordinates
 * const nearest = await getNearestAirport({ lat: 40.7128, lon: -74.0060 });
 *
 * if (nearest) {
 *   console.log(`Nearest: ${nearest.name} (${nearest.icao})`);
 * }
 * ```
 */
export async function getNearestAirport(
  coordinates?: Coordinates
): Promise<Airport | null> {
  try {
    // Reuse the multiple airport logic but limit to 1
    // This reduces code duplication while maintaining the API contract
    const nearest = await getNearestAirports(1, coordinates);
    return nearest[0] || null;
  } catch (error) {
    // Re-log as singular error for backward compatibility if needed,
    // or just let the inner one log. But since we catch and re-throw or return null...
    // The implementation reuses getNearestAirports, which logs 'Error finding nearest airports:'.
    // To match legacy behavior/tests perfectly, we should catch, log specific msg, and return null.
    console.error('Error finding nearest airport:', error);
    return null;
  }
}
