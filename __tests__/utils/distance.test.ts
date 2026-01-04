/**
 * Unit tests for distance calculation utilities.
 * 
 * These tests verify the accuracy of the Haversine formula implementation
 * against known real-world distances and edge cases.
 */

import {
  calculateDistance,
  calculateDistanceKm,
  calculateDistanceBetweenPoints,
  calculateDistanceBetweenPointsKm,
  degreesToRadians,
  Coordinates,
} from '../../utils/distance';

describe('Distance Calculation Utilities', () => {
  describe('degreesToRadians', () => {
    it('should convert 0 degrees to 0 radians', () => {
      expect(degreesToRadians(0)).toBe(0);
    });

    it('should convert 180 degrees to π radians', () => {
      expect(degreesToRadians(180)).toBeCloseTo(Math.PI, 10);
    });

    it('should convert 90 degrees to π/2 radians', () => {
      expect(degreesToRadians(90)).toBeCloseTo(Math.PI / 2, 10);
    });

    it('should convert 360 degrees to 2π radians', () => {
      expect(degreesToRadians(360)).toBeCloseTo(2 * Math.PI, 10);
    });

    it('should handle negative degrees', () => {
      expect(degreesToRadians(-90)).toBeCloseTo(-Math.PI / 2, 10);
    });
  });

  describe('calculateDistance (miles)', () => {
    it('should return 0 for same location', () => {
      const distance = calculateDistance(40.7128, -74.0060, 40.7128, -74.0060);
      expect(distance).toBe(0);
    });

    it('should calculate distance from New York City to Los Angeles (approximately 2,451 miles)', () => {
      // NYC: 40.7128° N, 74.0060° W
      // LA: 34.0522° N, 118.2437° W
      const distance = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
      
      // Real distance is approximately 2,451 miles
      // Allow 1% margin of error (24.51 miles)
      expect(distance).toBeGreaterThan(2451 - 24.51);
      expect(distance).toBeLessThan(2451 + 24.51);
    });

    it('should calculate distance from London to Paris (approximately 214 miles)', () => {
      // London: 51.5074° N, 0.1278° W
      // Paris: 48.8566° N, 2.3522° E
      const distance = calculateDistance(51.5074, -0.1278, 48.8566, 2.3522);
      
      // Real distance is approximately 214 miles
      // Allow 1% margin of error (2.14 miles)
      expect(distance).toBeGreaterThan(214 - 2.14);
      expect(distance).toBeLessThan(214 + 2.14);
    });

    it('should calculate distance from Tokyo to Sydney (approximately 4,863 miles)', () => {
      // Tokyo: 35.6762° N, 139.6503° E
      // Sydney: 33.8688° S, 151.2093° E
      const distance = calculateDistance(35.6762, 139.6503, -33.8688, 151.2093);
      
      // Real distance is approximately 4,863 miles
      // Allow 1% margin of error (48.63 miles)
      expect(distance).toBeGreaterThan(4863 - 48.63);
      expect(distance).toBeLessThan(4863 + 48.63);
    });

    it('should calculate distance from New York to Moscow (approximately 4,665 miles)', () => {
      // NYC: 40.7128° N, 74.0060° W
      // Moscow: 55.7558° N, 37.6173° E
      const distance = calculateDistance(40.7128, -74.0060, 55.7558, 37.6173);
      
      // Real distance is approximately 4,665 miles
      // Allow 1% margin of error (46.65 miles)
      expect(distance).toBeGreaterThan(4665 - 46.65);
      expect(distance).toBeLessThan(4665 + 46.65);
    });

    it('should handle equator crossing (Singapore to Nairobi, approximately 4,633 miles)', () => {
      // Singapore: 1.3521° N, 103.8198° E
      // Nairobi: 1.2921° S, 36.8219° E
      const distance = calculateDistance(1.3521, 103.8198, -1.2921, 36.8219);
      
      // Real distance is approximately 4,633 miles (great circle distance)
      // Allow 1% margin of error (46.33 miles)
      expect(distance).toBeGreaterThan(4633 - 46.33);
      expect(distance).toBeLessThan(4633 + 46.33);
    });

    it('should handle prime meridian crossing (London to Accra, approximately 3,170 miles)', () => {
      // London: 51.5074° N, 0.1278° W
      // Accra: 5.6037° N, 0.1870° W
      const distance = calculateDistance(51.5074, -0.1278, 5.6037, -0.1870);
      
      // Real distance is approximately 3,170 miles
      // Allow 1% margin of error (31.7 miles)
      expect(distance).toBeGreaterThan(3170 - 31.7);
      expect(distance).toBeLessThan(3170 + 31.7);
    });

    it('should handle antipodal points (nearly opposite sides of Earth)', () => {
      // Test points that are approximately on opposite sides of Earth
      // Madrid: 40.4168° N, 3.7038° W
      // Wellington: 41.2865° S, 174.7762° E
      const distance = calculateDistance(40.4168, -3.7038, -41.2865, 174.7762);
      
      // Distance should be approximately half Earth's circumference
      // Earth's circumference at equator ≈ 24,901 miles, so half ≈ 12,450 miles
      // Allow 2% margin for these extreme distances
      expect(distance).toBeGreaterThan(12200);
      expect(distance).toBeLessThan(12700);
    });

    it('should handle international date line crossing (Tokyo to Honolulu, approximately 3,859 miles)', () => {
      // Tokyo: 35.6762° N, 139.6503° E
      // Honolulu: 21.3099° N, 157.8581° W
      const distance = calculateDistance(35.6762, 139.6503, 21.3099, -157.8581);
      
      // Real distance is approximately 3,859 miles (great circle distance)
      // Allow 1% margin of error (38.59 miles)
      expect(distance).toBeGreaterThan(3859 - 38.59);
      expect(distance).toBeLessThan(3859 + 38.59);
    });

    it('should handle very short distances (within same city)', () => {
      // Two points in New York City approximately 1 mile apart
      // Empire State Building: 40.7484° N, 73.9857° W
      // Times Square: 40.7580° N, 73.9855° W
      const distance = calculateDistance(40.7484, -73.9857, 40.7580, -73.9855);
      
      // Distance should be less than 1 mile
      expect(distance).toBeGreaterThan(0.5);
      expect(distance).toBeLessThan(1.5);
    });

    it('should be symmetric (distance A to B equals B to A)', () => {
      const distanceAB = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
      const distanceBA = calculateDistance(34.0522, -118.2437, 40.7128, -74.0060);
      
      expect(distanceAB).toBeCloseTo(distanceBA, 10);
    });
  });

  describe('calculateDistanceKm (kilometers)', () => {
    it('should return 0 for same location', () => {
      const distance = calculateDistanceKm(40.7128, -74.0060, 40.7128, -74.0060);
      expect(distance).toBe(0);
    });

    it('should calculate distance from London to Paris (approximately 344 km)', () => {
      // London: 51.5074° N, 0.1278° W
      // Paris: 48.8566° N, 2.3522° E
      const distance = calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
      
      // Real distance is approximately 344 km
      // Allow 1% margin of error (3.44 km)
      expect(distance).toBeGreaterThan(344 - 3.44);
      expect(distance).toBeLessThan(344 + 3.44);
    });

    it('should calculate distance from Tokyo to Sydney (approximately 7,823 km)', () => {
      // Tokyo: 35.6762° N, 139.6503° E
      // Sydney: 33.8688° S, 151.2093° E
      const distance = calculateDistanceKm(35.6762, 139.6503, -33.8688, 151.2093);
      
      // Real distance is approximately 7,823 km
      // Allow 1% margin of error (78.23 km)
      expect(distance).toBeGreaterThan(7823 - 78.23);
      expect(distance).toBeLessThan(7823 + 78.23);
    });

    it('should be symmetric (distance A to B equals B to A)', () => {
      const distanceAB = calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
      const distanceBA = calculateDistanceKm(48.8566, 2.3522, 51.5074, -0.1278);
      
      expect(distanceAB).toBeCloseTo(distanceBA, 10);
    });

    it('should have consistent ratio with miles (1 mile ≈ 1.60934 km)', () => {
      const distanceMiles = calculateDistance(51.5074, -0.1278, 48.8566, 2.3522);
      const distanceKm = calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
      
      const ratio = distanceKm / distanceMiles;
      
      // Allow small margin for rounding differences
      expect(ratio).toBeGreaterThan(1.608);
      expect(ratio).toBeLessThan(1.611);
    });
  });

  describe('calculateDistanceBetweenPoints (Coordinates objects, miles)', () => {
    it('should calculate distance using Coordinates objects', () => {
      const nyc: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const la: Coordinates = { lat: 34.0522, lon: -118.2437 };
      
      const distance = calculateDistanceBetweenPoints(nyc, la);
      
      // Should match the direct function call
      const directDistance = calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
      expect(distance).toBe(directDistance);
    });

    it('should return 0 for same coordinates', () => {
      const point: Coordinates = { lat: 40.7128, lon: -74.0060 };
      const distance = calculateDistanceBetweenPoints(point, point);
      
      expect(distance).toBe(0);
    });
  });

  describe('calculateDistanceBetweenPointsKm (Coordinates objects, kilometers)', () => {
    it('should calculate distance using Coordinates objects', () => {
      const london: Coordinates = { lat: 51.5074, lon: -0.1278 };
      const paris: Coordinates = { lat: 48.8566, lon: 2.3522 };
      
      const distance = calculateDistanceBetweenPointsKm(london, paris);
      
      // Should match the direct function call
      const directDistance = calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
      expect(distance).toBe(directDistance);
    });

    it('should return 0 for same coordinates', () => {
      const point: Coordinates = { lat: 51.5074, lon: -0.1278 };
      const distance = calculateDistanceBetweenPointsKm(point, point);
      
      expect(distance).toBe(0);
    });
  });

  describe('Edge Cases', () => {
    it('should handle North Pole to South Pole distance', () => {
      // North Pole: 90° N, 0° E
      // South Pole: 90° S, 0° E
      const distance = calculateDistance(90, 0, -90, 0);
      
      // Should be approximately half of Earth's circumference through poles
      // Polar circumference ≈ 24,860 miles, half ≈ 12,430 miles
      expect(distance).toBeGreaterThan(12300);
      expect(distance).toBeLessThan(12500);
    });

    it('should handle locations at extreme latitudes', () => {
      // Two points near the North Pole
      const distance = calculateDistance(89, 0, 89, 180);
      
      // Distance should be small (close to pole)
      expect(distance).toBeGreaterThan(0);
      expect(distance).toBeLessThan(150);
    });

    it('should handle locations at extreme longitudes', () => {
      // Two points with maximum longitude difference
      const distance = calculateDistance(0, -180, 0, 180);
      
      // At the equator, this should be 0 (same point, opposite side of date line)
      expect(distance).toBeCloseTo(0, 5);
    });

    it('should handle locations on the equator', () => {
      // Two points on the equator, 90 degrees apart
      const distance = calculateDistance(0, 0, 0, 90);
      
      // Quarter of Earth's equatorial circumference
      // Earth's circumference ≈ 24,901 miles, quarter ≈ 6,225 miles
      expect(distance).toBeGreaterThan(6150);
      expect(distance).toBeLessThan(6300);
    });
  });
});
