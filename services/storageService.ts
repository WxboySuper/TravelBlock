import type { Airport } from '../types/airport';
import { StorageKey } from '../types/storage';
// Use Expo's SQLite-based KV store. The package exposes the same simple
// get/set/remove primitives we need via the `expo-sqlite/kv-store` entrypoint.
import { getItem, removeItem, setItem } from '../expo-sqlite/kv-store';
import { isValidAirport } from '../utils/validation';

/**
 * Service for local data persistence using `expo-sqlite/kv-store`.
 *
 * Primary backend: SQLite (via expo-sqlite/kv-store). AsyncStorage and an
 * in-memory Map are used as fallbacks when SQLite is unavailable.
 */
export const storageService = {
  /**
   * Saves the home airport to persistent storage.
   */
  async saveHomeAirport(airport: Airport): Promise<void> {
    try {
      await setItem({ key: StorageKey.HOME_AIRPORT, value: JSON.stringify(airport) });
    } catch (error) {
      console.error('Error saving home airport:', error);
      throw error;
    }
  },

  /**
   * Retrieves the home airport from persistent storage.
   */
  async getHomeAirport(): Promise<Airport | null> {
    try {
      const data = await getItem({ key: StorageKey.HOME_AIRPORT });
      if (!data) return null;

      const parsed = JSON.parse(data);
      if (isValidAirport(parsed)) {
        return parsed;
      }

      console.error('Home airport data is malformed or corrupted', parsed);
      return null;
    } catch (error) {
      console.error('Error getting home airport:', error);
      return null;
    }
  },

  /**
   * Clears the home airport from persistent storage.
   */
  async clearHomeAirport(): Promise<void> {
    try {
      await removeItem({ key: StorageKey.HOME_AIRPORT });
    } catch (error) {
      console.error('Error clearing home airport:', error);
    }
  },

  /**
   * Checks if a home airport is currently set.
   */
  async hasHomeAirport(): Promise<boolean> {
    const airport = await this.getHomeAirport();
    return airport !== null;
  },

  /**
   * Sets the onboarding completion flag.
   */
  async setOnboardingCompleted(completed: boolean): Promise<void> {
    try {
      if (completed) {
        await setItem({ key: StorageKey.HAS_COMPLETED_ONBOARDING, value: 'true' });
      } else {
        await removeItem({ key: StorageKey.HAS_COMPLETED_ONBOARDING });
      }
    } catch (error) {
      console.error('Error setting onboarding completed:', error);
      throw error;
    }
  },

  /**
   * Checks if the onboarding flow has been completed.
   */
  async getOnboardingCompleted(): Promise<boolean> {
    try {
      const value = await getItem({ key: StorageKey.HAS_COMPLETED_ONBOARDING });
      return value === 'true';
    } catch (error) {
      console.error('Error getting onboarding completed:', error);
      return false;
    }
  },

  /**
   * Generic method to get an item from storage.
   * Returns the parsed JSON value if it exists, null otherwise.
   */
  async getGenericItem<T>(key: StorageKey): Promise<T | null> {
    try {
      const data = await getItem({ key });
      if (!data) return null;
      return JSON.parse(data) as T;
    } catch (error) {
      console.error(`Error getting item ${key}:`, error);
      return null;
    }
  },

  /**
   * Generic method to set an item in storage.
   * Serializes the value to JSON before storing.
   */
  async setGenericItem<T>(key: StorageKey, value: T): Promise<void> {
    try {
      await setItem({ key, value: JSON.stringify(value) });
    } catch (error) {
      console.error(`Error setting item ${key}:`, error);
      throw error;
    }
  },

  /**
   * Generic method to remove an item from storage.
   */
  async removeGenericItem(key: StorageKey): Promise<void> {
    try {
      await removeItem({ key });
    } catch (error) {
      console.error(`Error removing item ${key}:`, error);
    }
  },
};
