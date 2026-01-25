/* eslint-disable */
// @ts-nocheck test
// Mock the project-local Expo SQLite KV store entrypoint used by the app
jest.mock('../../expo-sqlite/kv-store', () => ({
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
}), { virtual: true });

import { getItem, removeItem, setItem } from '../../expo-sqlite/kv-store';
import { storageService } from '../../services/storageService';
import { StorageKey } from '../../types/storage';

describe('storageService', () => {
  const mockAirport = {
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
    it('should save the airport to KV store', async () => {
      await storageService.saveHomeAirport(mockAirport);
      expect(setItem).toHaveBeenCalledWith({
        key: StorageKey.HOME_AIRPORT,
        value: JSON.stringify(mockAirport),
      });
    });

    it('should throw an error and log if saving fails', async () => {
      const error = new Error('Save failed');
      (setItem as jest.Mock).mockRejectedValueOnce(error);
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      await expect(storageService.saveHomeAirport(mockAirport)).rejects.toThrow(error);
      expect(consoleSpy).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('getHomeAirport', () => {
    it('should return the airport from KV store', async () => {
      (getItem as jest.Mock).mockResolvedValueOnce(JSON.stringify(mockAirport));
      
      const result = await storageService.getHomeAirport();
      
      expect(getItem).toHaveBeenCalledWith({ key: StorageKey.HOME_AIRPORT });
      expect(result).toEqual(mockAirport);
    });

    it('should return null if no airport is stored', async () => {
      (getItem as jest.Mock).mockResolvedValueOnce(null);
      
      const result = await storageService.getHomeAirport();
      
      expect(result).toBeNull();
    });

    it('should return null and log if retrieval fails', async () => {
      (getItem as jest.Mock).mockRejectedValueOnce(new Error('Read failed'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      const result = await storageService.getHomeAirport();
      
      expect(result).toBeNull();
      expect(consoleSpy).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('clearHomeAirport', () => {
    it('should remove the airport from storage', async () => {
      await storageService.clearHomeAirport();
      expect(removeItem).toHaveBeenCalledWith({ key: StorageKey.HOME_AIRPORT });
    });

    it('should log if removal fails', async () => {
      (removeItem as jest.Mock).mockRejectedValueOnce(new Error('Remove failed'));
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

  describe('setOnboardingCompleted', () => {
    it('should set the onboarding flag to true', async () => {
      await storageService.setOnboardingCompleted(true);
      expect(setItem).toHaveBeenCalledWith({
        key: StorageKey.HAS_COMPLETED_ONBOARDING,
        value: 'true',
      });
    });

    it('should remove the onboarding flag if false', async () => {
      await storageService.setOnboardingCompleted(false);
      expect(removeItem).toHaveBeenCalledWith({
        key: StorageKey.HAS_COMPLETED_ONBOARDING,
      });
    });

    it('should throw an error and log if saving fails', async () => {
      const error = new Error('Save failed');
      (setItem as jest.Mock).mockRejectedValueOnce(error);
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      await expect(storageService.setOnboardingCompleted(true)).rejects.toThrow(error);
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });

  describe('getOnboardingCompleted', () => {
    it('should return true if flag is set to "true"', async () => {
      (getItem as jest.Mock).mockResolvedValueOnce('true');

      const result = await storageService.getOnboardingCompleted();

      expect(getItem).toHaveBeenCalledWith({ key: StorageKey.HAS_COMPLETED_ONBOARDING });
      expect(result).toBe(true);
    });

    it('should return false if flag is anything else or null', async () => {
      (getItem as jest.Mock).mockResolvedValueOnce(null);
      const result = await storageService.getOnboardingCompleted();
      expect(result).toBe(false);

      (getItem as jest.Mock).mockResolvedValueOnce('false');
      const result2 = await storageService.getOnboardingCompleted();
      expect(result2).toBe(false);
    });

    it('should return false and log if retrieval fails', async () => {
      (getItem as jest.Mock).mockRejectedValueOnce(new Error('Read failed'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

      const result = await storageService.getOnboardingCompleted();

      expect(result).toBe(false);
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });
});
