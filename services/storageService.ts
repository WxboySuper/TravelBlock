import type { Airport } from '../types/airport';
import { StorageKey } from '../types/storage';
// Use Expo's SQLite-based KV store. The package exposes the same simple
// get/set/remove primitives we need via the `expo-sqlite/kv-store` entrypoint.
import { setItem, getItem, removeItem } from '../expo-sqlite/kv-store';

/**
 * Service for local data persistence using AsyncStorage.
 */
export const storageService = {
  /**
   * Saves the home airport to persistent storage.
   */
  async saveHomeAirport(airport: Airport): Promise<void> {
    try {
      await setItem(StorageKey.HOME_AIRPORT, JSON.stringify(airport));
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
      const data = await getItem(StorageKey.HOME_AIRPORT);
      return data ? JSON.parse(data) : null;
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
      await removeItem(StorageKey.HOME_AIRPORT);
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
};
