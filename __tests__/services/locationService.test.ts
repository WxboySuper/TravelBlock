/**
 * Unit tests for location service.
 *
 * These tests verify the functionality of the location service including:
 * - Permission handling (request and check)
 * - Current location retrieval
 * - Nearest airport finding
 * - Error handling and edge cases
 */

/* eslint-disable @typescript-eslint/no-explicit-any */

// Mock expo-location module
jest.mock('expo-location', () => ({
  requestForegroundPermissionsAsync: jest.fn(),
  getForegroundPermissionsAsync: jest.fn(),
  getCurrentPositionAsync: jest.fn(),
  Accuracy: {
    Balanced: 3,
  },
}));

// Mock airport service
jest.mock('../../services/airportService', () => ({
  loadAirports: jest.fn(),
}));

import {
  requestForegroundPermissionsAsync,
  getForegroundPermissionsAsync,
  getCurrentPositionAsync,
  Accuracy,
} from 'expo-location';
import {
  requestLocationPermission,
  hasLocationPermission,
  getCurrentLocation,
  getNearestAirport,
} from '../../services/locationService';
import { loadAirports } from '../../services/airportService';
import type { Airport } from '../../types/airport';
import type { Coordinates } from '../../types/location';

// Provide a `Location` object for backwards-compatible test references
const Location = {
  requestForegroundPermissionsAsync,
  getForegroundPermissionsAsync,
  getCurrentPositionAsync,
  Accuracy,
};

describe('Location Service', () => {
  // Mock airports for testing
  const mockAirportJFK: Airport = {
    icao: 'KJFK',
    iata: 'JFK',
    name: 'John F Kennedy International Airport',
    city: 'New York',
    state: 'NY',
    country: 'US',
    elevation: 13,
    lat: 40.6413,
    lon: -73.7781,
    tz: 'America/New_York',
  };

  const mockAirportLGA: Airport = {
    icao: 'KLGA',
    iata: 'LGA',
    name: 'LaGuardia Airport',
    city: 'New York',
    state: 'NY',
    country: 'US',
    elevation: 21,
    lat: 40.7769,
    lon: -73.8740,
    tz: 'America/New_York',
  };

  const mockAirportLAX: Airport = {
    icao: 'KLAX',
    iata: 'LAX',
    name: 'Los Angeles International Airport',
    city: 'Los Angeles',
    state: 'CA',
    country: 'US',
    elevation: 125,
    lat: 33.9425,
    lon: -118.4081,
    tz: 'America/Los_Angeles',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    // Suppress console output in tests
    jest.spyOn(console, 'error').mockImplementation(() => undefined);
    jest.spyOn(console, 'warn').mockImplementation(() => undefined);
  });

  afterEach(() => {
    (console.error as jest.Mock).mockRestore();
    (console.warn as jest.Mock).mockRestore();
  });

  describe('requestLocationPermission', () => {
    it('should return true when permission is granted', async () => {
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });

      const result = await requestLocationPermission();

      expect(result).toBe(true);
      expect(Location.requestForegroundPermissionsAsync).toHaveBeenCalledTimes(1);
    });

    it('should return false when permission is denied', async () => {
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });

      const result = await requestLocationPermission();

      expect(result).toBe(false);
      expect(Location.requestForegroundPermissionsAsync).toHaveBeenCalledTimes(1);
    });

    it('should return false when permission is undetermined', async () => {
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'undetermined',
      });

      const result = await requestLocationPermission();

      expect(result).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      const error = new Error('Permission request failed');
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockRejectedValueOnce(error);

      const result = await requestLocationPermission();

      expect(result).toBe(false);
      expect(console.error).toHaveBeenCalledWith(
        'Error requesting location permission:',
        error
      );
    });
  });

  describe('hasLocationPermission', () => {
    it('should return true when permission is granted', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });

      const result = await hasLocationPermission();

      expect(result).toBe(true);
      expect(Location.getForegroundPermissionsAsync).toHaveBeenCalledTimes(1);
    });

    it('should return false when permission is denied', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });

      const result = await hasLocationPermission();

      expect(result).toBe(false);
    });

    it('should return false when permission is undetermined', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'undetermined',
      });

      const result = await hasLocationPermission();

      expect(result).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      const error = new Error('Permission check failed');
      (Location.getForegroundPermissionsAsync as jest.Mock).mockRejectedValueOnce(error);

      const result = await hasLocationPermission();

      expect(result).toBe(false);
      expect(console.error).toHaveBeenCalledWith(
        'Error checking location permission:',
        error
      );
    });
  });

  describe('getCurrentLocation', () => {
    it('should return coordinates when permission is granted and location is available', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      (Location.getCurrentPositionAsync as jest.Mock).mockResolvedValueOnce({
        coords: {
          latitude: 40.7128,
          longitude: -74.0060,
        },
      });

      const result = await getCurrentLocation();

      expect(result).toEqual({
        lat: 40.7128,
        lon: -74.0060,
      });
      expect(Location.getCurrentPositionAsync).toHaveBeenCalledWith({
        accuracy: Location.Accuracy.Balanced,
      });
    });

    it('should return null when permission is not granted', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });

      const result = await getCurrentLocation();

      expect(result).toBeNull();
      expect(console.warn).toHaveBeenCalledWith('Location permission not granted');
      expect(Location.getCurrentPositionAsync).not.toHaveBeenCalled();
    });

    it('should handle location retrieval errors gracefully', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      const error = new Error('Location unavailable');
      (Location.getCurrentPositionAsync as jest.Mock).mockRejectedValueOnce(error);

      const result = await getCurrentLocation();

      expect(result).toBeNull();
      expect(console.error).toHaveBeenCalledWith('Error getting current location:', error);
    });

    it('should handle permission check errors', async () => {
      const error = new Error('Permission check failed');
      (Location.getForegroundPermissionsAsync as jest.Mock).mockRejectedValueOnce(error);

      const result = await getCurrentLocation();

      expect(result).toBeNull();
      expect(console.error).toHaveBeenCalledWith('Error checking location permission:', error);
    });
  });

  describe('getNearestAirport', () => {
    beforeEach(() => {
      // Default mock for loadAirports
      (loadAirports as jest.Mock).mockResolvedValue({
        KJFK: mockAirportJFK,
        KLGA: mockAirportLGA,
        KLAX: mockAirportLAX,
      });
    });

    it('should find nearest airport from provided coordinates', async () => {
      // Coordinates near JFK (40.6413, -73.7781)
      const searchCoords: Coordinates = { lat: 40.65, lon: -73.78 };

      const result = await getNearestAirport(searchCoords);

      expect(result).toBeDefined();
      expect(result?.icao).toBe('KJFK');
    });

    it('should find nearest airport using current location when no coordinates provided', async () => {
      // Mock permission granted and current location near LaGuardia
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      (Location.getCurrentPositionAsync as jest.Mock).mockResolvedValueOnce({
        coords: {
          latitude: 40.7769,
          longitude: -73.8740,
        },
      });

      const result = await getNearestAirport();

      expect(result).toBeDefined();
      expect(result?.icao).toBe('KLGA');
    });

    it('should return null when no coordinates provided and location unavailable', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });

      const result = await getNearestAirport();

      expect(result).toBeNull();
      expect(console.warn).toHaveBeenCalledWith('No coordinates available for airport search');
    });

    it('should handle empty airport database', async () => {
      (loadAirports as jest.Mock).mockResolvedValueOnce({});

      const searchCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const result = await getNearestAirport(searchCoords);

      expect(result).toBeNull();
      expect(console.warn).toHaveBeenCalledWith('No airports available in database');
    });

    it('should find nearest airport from west coast coordinates', async () => {
      // Coordinates near LAX
      const searchCoords: Coordinates = { lat: 33.95, lon: -118.40 };

      const result = await getNearestAirport(searchCoords);

      expect(result).toBeDefined();
      expect(result?.icao).toBe('KLAX');
    });

    it('should handle loadAirports errors gracefully', async () => {
      const error = new Error('Failed to load airports');
      (loadAirports as jest.Mock).mockRejectedValueOnce(error);

      const searchCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const result = await getNearestAirport(searchCoords);

      expect(result).toBeNull();
      expect(console.error).toHaveBeenCalledWith('Error finding nearest airport:', error);
    });

    it('should correctly calculate distances for multiple airports', async () => {
      // Position slightly closer to LaGuardia than JFK
      const searchCoords: Coordinates = { lat: 40.75, lon: -73.87 };

      const result = await getNearestAirport(searchCoords);

      expect(result).toBeDefined();
      // Should find LGA as it's closer
      expect(result?.icao).toBe('KLGA');
    });

    it('should handle single airport in database', async () => {
      (loadAirports as jest.Mock).mockResolvedValueOnce({
        KJFK: mockAirportJFK,
      });

      const searchCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const result = await getNearestAirport(searchCoords);

      expect(result).toBeDefined();
      expect(result?.icao).toBe('KJFK');
    });

    it('should handle extreme coordinates (near poles)', async () => {
      // Near North Pole
      const searchCoords: Coordinates = { lat: 89.0, lon: 0 };

      const result = await getNearestAirport(searchCoords);

      // Should still find some airport (farthest one)
      expect(result).toBeDefined();
      expect(result?.icao).toBeDefined();
    });

    it('should handle coordinates at equator', async () => {
      const searchCoords: Coordinates = { lat: 0, lon: 0 };

      const result = await getNearestAirport(searchCoords);

      expect(result).toBeDefined();
    });
  });

  describe('Integration Scenarios', () => {
    beforeEach(() => {
      (loadAirports as jest.Mock).mockResolvedValue({
        KJFK: mockAirportJFK,
        KLGA: mockAirportLGA,
        KLAX: mockAirportLAX,
      });
    });

    it('should handle complete workflow: request permission, get location, find airport', async () => {
      // Step 1: Request permission
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      const permissionGranted = await requestLocationPermission();
      expect(permissionGranted).toBe(true);

      // Step 2: Get current location
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      (Location.getCurrentPositionAsync as jest.Mock).mockResolvedValueOnce({
        coords: {
          latitude: 40.6413,
          longitude: -73.7781,
        },
      });
      const location = await getCurrentLocation();
      expect(location).toBeDefined();
      if (!location) throw new Error('Expected location to be defined');

      // Step 3: Find nearest airport
      const nearest = await getNearestAirport(location);
      expect(nearest).toBeDefined();
      expect(nearest?.icao).toBe('KJFK');
    });

    it('should handle permission denied workflow', async () => {
      // Request permission - denied
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });
      const permissionGranted = await requestLocationPermission();
      expect(permissionGranted).toBe(false);

      // Try to get location - should fail
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });
      const location = await getCurrentLocation();
      expect(location).toBeNull();

      // Try to find nearest without coords - should fail
      // Ensure the permission check inside getNearestAirport/getCurrentLocation
      // also returns denied by providing a second mock for getForegroundPermissionsAsync
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'denied',
      });
      const nearest = await getNearestAirport();
      expect(nearest).toBeNull();
    });

    it('should allow finding nearest airport with explicit coordinates even without permission', async () => {
      // No permission
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValue({
        status: 'denied',
      });

      // But can still find nearest with explicit coordinates
      const searchCoords: Coordinates = { lat: 40.6413, lon: -73.7781 };
      const nearest = await getNearestAirport(searchCoords);

      expect(nearest).toBeDefined();
      expect(nearest?.icao).toBe('KJFK');
    });
  });

  describe('Edge Cases', () => {
    it('should handle malformed location data', async () => {
      (Location.getForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'granted',
      });
      (Location.getCurrentPositionAsync as jest.Mock).mockResolvedValueOnce({
        coords: {
          latitude: NaN,
          longitude: -74.0060,
        },
      });

      const result = await getCurrentLocation();

      // Non-finite coordinates should be treated as unavailable
      expect(result).toBeNull();
    });

    it('should handle permission status with unexpected value', async () => {
      (Location.requestForegroundPermissionsAsync as jest.Mock).mockResolvedValueOnce({
        status: 'unknown' as any,
      });

      const result = await requestLocationPermission();

      expect(result).toBe(false);
    });
  });
});
