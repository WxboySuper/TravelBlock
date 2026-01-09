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
