/**
 * Type definitions for location services.
 *
 * This module defines types used by the location service for GPS coordinates,
 * permissions, and location-related operations.
 *
 * @module types/location
 */

/**
 * Represents geographic coordinates (latitude and longitude).
 * Re-exported from utils/distance for convenience.
 */
export type { Coordinates } from '../utils/distance';

/**
 * Represents the possible states of location permission.
 */
export enum LocationPermissionStatus {
  /** Permission has been granted by the user */
  GRANTED = 'granted',
  /** Permission has been denied by the user */
  DENIED = 'denied',
  /** Permission status has not been determined yet */
  UNDETERMINED = 'undetermined',
}
