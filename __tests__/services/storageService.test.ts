import * as AsyncStorage from 'expo-sqlite/kv-store';
import { StorageKey, storageService } from '../../services/storageService';
import { Airport } from '../../types/airport';

// Mock the Expo SQLite KV store entrypoint used by the app
jest.mock('expo-sqlite/kv-store', () => ({
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
}), { virtual: true });

describe('storageService', () => {
  const mockAirport: Airport = {
    icao: 'KJFK',
    iata: 'JFK',
    name: 'John F Kennedy International Airport',
    city: 'New York',
    state: 'NY',
    country: 'US',
    lat: 40.6413,
    lon: -73.7781,
    elevation: 13,
    tz: 'America/New_York'
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('saveHomeAirport', () => {
    it('should save the airport to AsyncStorage', async () => {
      await storageService.saveHomeAirport(mockAirport);
      expect(AsyncStorage.setItem).toHaveBeenCalledWith(
        StorageKey.HOME_AIRPORT,
        JSON.stringify(mockAirport)
      );
    });

    it('should throw an error and log if saving fails', async () => {
      const error = new Error('Save failed');
      (AsyncStorage.setItem as jest.Mock).mockRejectedValueOnce(error);
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      await expect(storageService.saveHomeAirport(mockAirport)).rejects.toThrow(error);
      expect(consoleSpy).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('getHomeAirport', () => {
    it('should return the airport from AsyncStorage', async () => {
      (AsyncStorage.getItem as jest.Mock).mockResolvedValueOnce(JSON.stringify(mockAirport));
      
      const result = await storageService.getHomeAirport();
      
      expect(AsyncStorage.getItem).toHaveBeenCalledWith(StorageKey.HOME_AIRPORT);
      expect(result).toEqual(mockAirport);
    });

    it('should return null if no airport is stored', async () => {
      (AsyncStorage.getItem as jest.Mock).mockResolvedValueOnce(null);
      
      const result = await storageService.getHomeAirport();
      
      expect(result).toBeNull();
    });

    it('should return null and log if retrieval fails', async () => {
      (AsyncStorage.getItem as jest.Mock).mockRejectedValueOnce(new Error('Read failed'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      const result = await storageService.getHomeAirport();
      
      expect(result).toBeNull();
      expect(consoleSpy).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('clearHomeAirport', () => {
    it('should remove the airport from AsyncStorage', async () => {
      await storageService.clearHomeAirport();
      expect(AsyncStorage.removeItem).toHaveBeenCalledWith(StorageKey.HOME_AIRPORT);
    });

    it('should log if removal fails', async () => {
      (AsyncStorage.removeItem as jest.Mock).mockRejectedValueOnce(new Error('Remove failed'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      await storageService.clearHomeAirport();
      
      expect(consoleSpy).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('hasHomeAirport', () => {
    it('should return true if airport exists', async () => {
      jest.spyOn(storageService, 'getHomeAirport').mockResolvedValueOnce(mockAirport);
      
      const result = await storageService.hasHomeAirport();
      
      expect(result).toBe(true);
    });

    it('should return false if airport does not exist', async () => {
      jest.spyOn(storageService, 'getHomeAirport').mockResolvedValueOnce(null);
      
      const result = await storageService.hasHomeAirport();
      
      expect(result).toBe(false);
    });
  });
});
