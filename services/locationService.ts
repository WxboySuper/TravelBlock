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
import { loadAirports } from './airportService';
import { calculateDistance } from '../utils/distance';

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

/**
 * Finds the nearest airports to the given coordinates.
 *
 * @param limit - Maximum number of airports to return (default: 3)
 * @param coordinates - Optional coordinates to search from. If not provided, uses current device location
 * @returns Promise that resolves to an array of nearest airports
 */
export async function getNearestAirports(
  limit: number = 3,
  coordinates?: Coordinates
): Promise<Airport[]> {
  try {
    // Use provided coordinates or get current location
    const searchCoords = coordinates || (await getCurrentLocation());

    if (!searchCoords) {
      console.warn('No coordinates available for airport search');
      return [];
    }

    // Validate provided/search coordinates are finite numbers.
    if (!Number.isFinite(searchCoords.lat) || !Number.isFinite(searchCoords.lon)) {
      console.warn('Invalid search coordinates for airport search', searchCoords);
      return [];
    }

    // Delegate airport loading and nearest selection to helper
    return await findNearestAirportsForCoordinates(searchCoords, limit);
  } catch (error) {
    console.error('Error finding nearest airports:', error);
    return [];
  }
}

// Helper: compute distance to an airport if both coordinates are valid, otherwise null
function computeDistanceToAirport(search: Coordinates, airport: Airport): number | null {
  const lat = airport.lat;
  const lon = airport.lon;
  if (!isFiniteNumber(lat) || !isFiniteNumber(lon)) {
    // eslint-disable-next-line no-console
    console.warn('Skipping airport with invalid coordinates', airport);
    return null;
  }
  const distance = calculateDistance(search, { lat, lon });
  return Number.isFinite(distance) ? distance : null;
}

// Helper: find nearest airport for already-validated search coordinates
async function findNearestAirportForCoordinates(searchCoords: Coordinates): Promise<Airport | null> {
  const airports = await loadAirports();
  const airportArray = Object.values(airports);
  if (airportArray.length === 0) {
    console.warn('No airports available in database');
    return null;
  }

  let nearest: Airport | null = null;
  let min = Infinity;
  for (const airport of airportArray) {
    const distance = computeDistanceToAirport(searchCoords, airport);
    if (distance === null) continue;
    if (distance < min) {
      min = distance;
      nearest = airport;
    }
  }
  return nearest;
}

// Helper: find nearest N airports for already-validated search coordinates
async function findNearestAirportsForCoordinates(searchCoords: Coordinates, limit: number): Promise<Airport[]> {
  const airports = await loadAirports();
  const airportArray = Object.values(airports);
  if (airportArray.length === 0) {
    console.warn('No airports available in database');
    return [];
  }

  const withDist = airportArray
    .map((airport) => {
      const distance = computeDistanceToAirport(searchCoords, airport);
      return { airport, distance };
    })
    .filter((item): item is { airport: Airport; distance: number } => item.distance !== null);

  withDist.sort((a, b) => a.distance - b.distance);

  return withDist.slice(0, limit).map((item) => item.airport);
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

    // Delegate airport loading and nearest selection to helper
    return await findNearestAirportForCoordinates(searchCoords);
  } catch (error) {
    console.error('Error finding nearest airport:', error);
    return null;
  }
}
