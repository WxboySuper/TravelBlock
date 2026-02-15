/**
 * Type definitions for storage operations.
 *
 * This module defines TypeScript types and enums for all storage-related
 * operations in the application, providing type safety and documentation
 * for persistent data management.
 *
 * @module types/storage
 */

/**
 * Keys used for AsyncStorage operations.
 *
 * These constants ensure type-safe and consistent key naming across
 * the application. All storage keys should be added to this enum.
 *
 * Naming convention: `travelblock_<feature>_<key>`
 *
 * @example
 * ```typescript
 * import { StorageKey } from '../types/storage';
 * await AsyncStorage.setItem(StorageKey.HOME_AIRPORT, data);
 * ```
 */
export enum StorageKey {
  /**
   * Key for storing the user's home airport.
   * Value: JSON-serialized Airport object
   */
  HOME_AIRPORT = 'travelblock_home_airport',

  /**
   * Key for tracking if the user has completed the onboarding flow.
   * Value: "true" | "false"
   */
  HAS_COMPLETED_ONBOARDING = 'travelblock_has_completed_onboarding',

  /**
   * Key for storing the flight origin airport.
   * Value: JSON-serialized Airport object
   */
  FLIGHT_ORIGIN = 'travelblock_flight_origin',

  /**
   * Key for storing the flight destination airport.
   * Value: JSON-serialized Airport object
   */
  FLIGHT_DESTINATION = 'travelblock_flight_destination',

  /**
   * Key for storing the selected flight duration in seconds.
   * Value: number (seconds)
   */
  FLIGHT_DURATION = 'travelblock_flight_duration',

  /**
   * Key for storing the current flight booking.
   * Value: JSON-serialized FlightBooking object
   */
  FLIGHT_BOOKING = 'travelblock_flight_booking',

  /**
   * Key for storing the selected seat.
   * Value: JSON-serialized Seat object
   */
  FLIGHT_SEAT = 'travelblock_flight_seat',

  /**
   * Key for storing the boarding pass.
   * Value: JSON-serialized BoardingPass object
   */
  BOARDING_PASS = 'travelblock_boarding_pass',
}

/**
 * Generic storage operation result.
 *
 * Represents the outcome of a storage operation with success/error state.
 */
export interface StorageResult<T> {
  /** Whether the operation succeeded */
  success: boolean;
  
  /** The data returned (if successful) */
  data?: T;
  
  /** Error message (if failed) */
  error?: string;
}
