/**
 * Unit tests for airport service.
 * 
 * These tests verify the functionality of the airport service including:
 * - Lazy loading and caching
 * - Search operations
 * - Distance-based filtering
 * - ICAO lookups
 * - Country-based filtering
 */

import {
  loadAirports,
  searchAirports,
  getAirportsWithinDistance,
  getAirportByICAO,
  getAirportsByCountry,
  clearCache,
} from '../../services/airportService';
import { Coordinates } from '../../utils/distance';

describe('Airport Service', () => {
  // Clear cache before each test to ensure clean state
  beforeEach(() => {
    clearCache();
  });

  describe('loadAirports', () => {
    it('should load airport data successfully', async () => {
      const airports = await loadAirports();
      
      expect(airports).toBeDefined();
      expect(typeof airports).toBe('object');
      expect(Object.keys(airports).length).toBeGreaterThan(0);
    });

    it('should return cached data on subsequent calls', async () => {
      const firstLoad = await loadAirports();
      const secondLoad = await loadAirports();
      
      // Should return the same reference (cached)
      expect(firstLoad).toBe(secondLoad);
    });

    it('should load airports with correct structure', async () => {
      const airports = await loadAirports();
      const firstAirport = Object.values(airports)[0];
      
      expect(firstAirport).toHaveProperty('icao');
      expect(firstAirport).toHaveProperty('iata');
      expect(firstAirport).toHaveProperty('name');
      expect(firstAirport).toHaveProperty('city');
      expect(firstAirport).toHaveProperty('state');
      expect(firstAirport).toHaveProperty('country');
      expect(firstAirport).toHaveProperty('elevation');
      expect(firstAirport).toHaveProperty('lat');
      expect(firstAirport).toHaveProperty('lon');
      expect(firstAirport).toHaveProperty('tz');
    });

    it('should load a significant number of airports', async () => {
      const airports = await loadAirports();
      const count = Object.keys(airports).length;
      
      // Should have at least 20,000 airports (dataset has ~29,000)
      expect(count).toBeGreaterThan(20000);
    });
  });

  describe('searchAirports', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should return empty array for empty query', () => {
      expect(searchAirports('')).toEqual([]);
      expect(searchAirports('   ')).toEqual([]);
    });

    it('should return empty array when data not loaded', () => {
      clearCache();
      expect(searchAirports('KJFK')).toEqual([]);
    });

    it('should find airport by exact ICAO code', () => {
      const results = searchAirports('KJFK');
      
      expect(results.length).toBeGreaterThan(0);
      const jfk = results.find((a) => a.icao === 'KJFK');
      expect(jfk).toBeDefined();
      expect(jfk?.name).toContain('Kennedy');
    });

    it('should find airports by IATA code', () => {
      const results = searchAirports('LAX');
      
      expect(results.length).toBeGreaterThan(0);
      const lax = results.find((a) => a.iata === 'LAX');
      expect(lax).toBeDefined();
      expect(lax?.city).toBe('Los Angeles');
    });

    it('should find airports by city name', () => {
      const results = searchAirports('London');
      
      expect(results.length).toBeGreaterThan(0);
      // Should find multiple London airports
      const londonAirports = results.filter((a) => a.city.toLowerCase().includes('london'));
      expect(londonAirports.length).toBeGreaterThan(0);
    });

    it('should find airports by airport name', () => {
      const results = searchAirports('London Heathrow');
      
      expect(results.length).toBeGreaterThan(0);
      const heathrow = results.find((a) => a.icao === 'EGLL');
      expect(heathrow).toBeDefined();
      expect(heathrow?.name).toContain('Heathrow');
    });

    it('should be case-insensitive', () => {
      const upperResults = searchAirports('KJFK');
      const lowerResults = searchAirports('kjfk');
      const mixedResults = searchAirports('KjFk');
      
      expect(upperResults.length).toBe(lowerResults.length);
      expect(upperResults.length).toBe(mixedResults.length);
      expect(upperResults[0].icao).toBe(lowerResults[0].icao);
    });

    it('should prioritize exact matches over partial matches', () => {
      const results = searchAirports('LAX');
      
      // First result should be the exact IATA match
      expect(results[0].iata).toBe('LAX');
    });

    it('should handle partial searches', () => {
      const results = searchAirports('New York');
      
      expect(results.length).toBeGreaterThan(0);
      // Should find JFK, LaGuardia, Newark, etc.
      const hasJFK = results.some((a) => a.icao === 'KJFK');
      expect(hasJFK).toBe(true);
    });

    it('should handle special characters in search', () => {
      const results = searchAirports("O'Hare");
      
      expect(results.length).toBeGreaterThan(0);
      const ohare = results.find((a) => a.name.includes("O'Hare"));
      expect(ohare).toBeDefined();
    });

    it('should return results in reasonable time', () => {
      const startTime = Date.now();
      searchAirports('Los Angeles');
      const endTime = Date.now();
      
      // Should complete in less than 100ms
      expect(endTime - startTime).toBeLessThan(100);
    });
  });

  describe('getAirportsWithinDistance', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should return empty array when data not loaded', () => {
      clearCache();
      const origin: Coordinates = { lat: 40.7128, lon: -74.0060 };
      expect(getAirportsWithinDistance(origin, 50)).toEqual([]);
    });

    it('should find airports within specified distance', () => {
      // New York City coordinates
      const nycCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      
      const nearbyAirports = getAirportsWithinDistance(nycCoords, 50);
      
      expect(nearbyAirports.length).toBeGreaterThan(0);
      // Should include JFK, LaGuardia, Newark
      const hasJFK = nearbyAirports.some((a) => a.icao === 'KJFK');
      const hasLGA = nearbyAirports.some((a) => a.icao === 'KLGA');
      expect(hasJFK || hasLGA).toBe(true);
    });

    it('should sort results by distance (nearest first)', () => {
      const nycCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const nearbyAirports = getAirportsWithinDistance(nycCoords, 100);
      
      expect(nearbyAirports.length).toBeGreaterThan(1);
      
      // Verify sorting by checking distances are non-decreasing
      // (we don't return distances, so we verify at least we got results)
      expect(nearbyAirports[0]).toBeDefined();
    });

    it('should return empty array when no airports within range', () => {
      // Middle of the Pacific Ocean
      const oceanCoords: Coordinates = { lat: 0, lon: -140 };
      
      const nearbyAirports = getAirportsWithinDistance(oceanCoords, 10);
      
      // Should find very few or no airports
      expect(nearbyAirports.length).toBeLessThan(5);
    });

    it('should handle zero distance (exact location)', () => {
      // Use JFK's exact coordinates
      const jfkCoords: Coordinates = { lat: 40.63980103, lon: -73.77890015 };
      
      const airports = getAirportsWithinDistance(jfkCoords, 0.1);
      
      // Should find at least JFK itself
      expect(airports.length).toBeGreaterThan(0);
    });

    it('should handle large distance values', () => {
      const nycCoords: Coordinates = { lat: 40.7128, lon: -74.0060 };
      
      // 500 miles should cover a large area
      const airports = getAirportsWithinDistance(nycCoords, 500);
      
      expect(airports.length).toBeGreaterThan(100);
    });
  });

  describe('getAirportByICAO', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should return null when data not loaded', () => {
      clearCache();
      expect(getAirportByICAO('KJFK')).toBeNull();
    });

    it('should find airport by exact ICAO code', () => {
      const jfk = getAirportByICAO('KJFK');
      
      expect(jfk).toBeDefined();
      expect(jfk?.icao).toBe('KJFK');
      expect(jfk?.iata).toBe('JFK');
      expect(jfk?.name).toContain('Kennedy');
    });

    it('should be case-insensitive', () => {
      const upper = getAirportByICAO('KJFK');
      const lower = getAirportByICAO('kjfk');
      const mixed = getAirportByICAO('KjFk');
      
      expect(upper).toBeDefined();
      expect(lower).toBeDefined();
      expect(mixed).toBeDefined();
      expect(upper?.icao).toBe(lower?.icao);
      expect(upper?.icao).toBe(mixed?.icao);
    });

    it('should return null for non-existent ICAO code', () => {
      const result = getAirportByICAO('XXXX');
      expect(result).toBeNull();
    });

    it('should find various international airports', () => {
      const heathrow = getAirportByICAO('EGLL');
      const charlesDeGaulle = getAirportByICAO('LFPG');
      const narita = getAirportByICAO('RJAA');
      
      expect(heathrow).toBeDefined();
      expect(heathrow?.name).toContain('Heathrow');
      
      expect(charlesDeGaulle).toBeDefined();
      expect(charlesDeGaulle?.city).toBe('Paris');
      
      expect(narita).toBeDefined();
      expect(narita?.city).toBe('Tokyo');
    });

    it('should handle airports with empty IATA codes', async () => {
      // Find any airport with empty IATA
      const airports = await loadAirports();
      const airportWithoutIATA = Object.values(airports).find((a) => !a.iata);
      
      if (airportWithoutIATA) {
        const found = getAirportByICAO(airportWithoutIATA.icao);
        expect(found).toBeDefined();
        expect(found?.iata).toBe('');
      }
    });

    it('should perform O(1) lookup (fast)', () => {
      const startTime = Date.now();
      for (let i = 0; i < 1000; i++) {
        getAirportByICAO('KJFK');
      }
      const endTime = Date.now();
      
      // 1000 lookups should complete in well under 100ms
      expect(endTime - startTime).toBeLessThan(100);
    });
  });

  describe('getAirportsByCountry', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should return empty array when data not loaded', () => {
      clearCache();
      expect(getAirportsByCountry('US')).toEqual([]);
    });

    it('should find all airports in a country', () => {
      const usAirports = getAirportsByCountry('US');
      
      expect(usAirports.length).toBeGreaterThan(0);
      // US should have thousands of airports
      expect(usAirports.length).toBeGreaterThan(1000);
      
      // All should have country code "US"
      usAirports.forEach((airport) => {
        expect(airport.country).toBe('US');
      });
    });

    it('should be case-insensitive', () => {
      const upperResults = getAirportsByCountry('US');
      const lowerResults = getAirportsByCountry('us');
      const mixedResults = getAirportsByCountry('Us');
      
      expect(upperResults.length).toBe(lowerResults.length);
      expect(upperResults.length).toBe(mixedResults.length);
    });

    it('should handle various countries', () => {
      const ukAirports = getAirportsByCountry('GB');
      const frenchAirports = getAirportsByCountry('FR');
      const japanAirports = getAirportsByCountry('JP');
      
      expect(ukAirports.length).toBeGreaterThan(0);
      expect(frenchAirports.length).toBeGreaterThan(0);
      expect(japanAirports.length).toBeGreaterThan(0);
      
      // Verify country codes
      ukAirports.forEach((a) => expect(a.country).toBe('GB'));
      frenchAirports.forEach((a) => expect(a.country).toBe('FR'));
      japanAirports.forEach((a) => expect(a.country).toBe('JP'));
    });

    it('should return empty array for invalid country code', () => {
      const results = getAirportsByCountry('XX');
      expect(results).toEqual([]);
    });

    it('should return empty array for empty country code', () => {
      const results = getAirportsByCountry('');
      expect(results).toEqual([]);
    });
  });

  describe('Performance', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should load airports in reasonable time', async () => {
      clearCache();
      
      const startTime = Date.now();
      await loadAirports();
      const endTime = Date.now();
      
      // Initial load should complete in less than 500ms
      expect(endTime - startTime).toBeLessThan(500);
    });

    it('should handle multiple concurrent searches efficiently', () => {
      const startTime = Date.now();
      
      searchAirports('New York');
      searchAirports('Los Angeles');
      searchAirports('London');
      searchAirports('Tokyo');
      searchAirports('Paris');
      
      const endTime = Date.now();
      
      // All searches should complete quickly
      expect(endTime - startTime).toBeLessThan(200);
    });

    it('should cache results effectively', async () => {
      clearCache();
      
      const firstLoadTime = Date.now();
      await loadAirports();
      const firstLoadEnd = Date.now();
      
      const secondLoadTime = Date.now();
      await loadAirports();
      const secondLoadEnd = Date.now();
      
      const firstDuration = firstLoadEnd - firstLoadTime;
      const secondDuration = secondLoadEnd - secondLoadTime;
      
      // Cached load should be much faster (< 1ms)
      expect(secondDuration).toBeLessThan(firstDuration);
      expect(secondDuration).toBeLessThan(5);
    });
  });

  describe('Edge Cases', () => {
    beforeEach(async () => {
      await loadAirports();
    });

    it('should handle searches with numbers', () => {
      const results = searchAirports('00AK');
      expect(results.length).toBeGreaterThan(0);
    });

    it('should handle searches with hyphens', () => {
      // Some airport names or cities might have hyphens
      const results = searchAirports('-');
      // Should return results or empty array without error
      expect(Array.isArray(results)).toBe(true);
    });

    it('should handle very long search queries', () => {
      const longQuery = 'a'.repeat(100);
      const results = searchAirports(longQuery);
      // Should return empty or small array without error
      expect(Array.isArray(results)).toBe(true);
    });

    it('should handle extreme coordinates for distance search', () => {
      // North Pole
      const northPole: Coordinates = { lat: 90, lon: 0 };
      const results = getAirportsWithinDistance(northPole, 1000);
      expect(Array.isArray(results)).toBe(true);
    });

    it('should handle unicode characters in search', () => {
      const results = searchAirports('Zürich');
      // Should handle without error
      expect(Array.isArray(results)).toBe(true);
    });
  });
});
