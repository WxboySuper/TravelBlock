import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Airport } from '../types/airport';

/**
 * Keys used for storage operations.
 */
export enum StorageKey {
  HOME_AIRPORT = 'travelblock_home_airport',
}

/**
 * Service for local data persistence using AsyncStorage.
 */
export const storageService = {
  /**
   * Saves the home airport to persistent storage.
   */
  async saveHomeAirport(airport: Airport): Promise<void> {
    try {
      await AsyncStorage.setItem(StorageKey.HOME_AIRPORT, JSON.stringify(airport));
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
      const data = await AsyncStorage.getItem(StorageKey.HOME_AIRPORT);
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
      await AsyncStorage.removeItem(StorageKey.HOME_AIRPORT);
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
