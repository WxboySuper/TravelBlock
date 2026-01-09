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
    // Check permission first
    const hasPermission = await hasLocationPermission();
    if (!hasPermission) {
      console.warn('Location permission not granted');
      return null;
    }

    // Get current position with reasonable accuracy
    const location = await getCurrentPositionAsync({
      accuracy: Accuracy.Balanced,
    });

    return {
      lat: location.coords.latitude,
      lon: location.coords.longitude,
    };
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

    // Load airport data
    const airports = await loadAirports();
    const airportArray = Object.values(airports);

    if (airportArray.length === 0) {
      console.warn('No airports available in database');
      return null;
    }

    // Find nearest airport by calculating distance to each
    let nearestAirport: Airport | null = null;
    let minDistance = Infinity;

    for (const airport of airportArray) {
      const airportCoords: Coordinates = {
        lat: airport.lat,
        lon: airport.lon,
      };

      const distance = calculateDistance(searchCoords, airportCoords);

      if (distance < minDistance) {
        minDistance = distance;
        nearestAirport = airport;
      }
    }

    return nearestAirport;
  } catch (error) {
    console.error('Error finding nearest airport:', error);
    return null;
  }
}
