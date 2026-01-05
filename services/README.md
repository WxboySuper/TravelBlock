# Services

This directory contains business logic and data access services for the TravelBlock application.

## Airport Service

The Airport Service provides efficient access to a comprehensive dataset of airports worldwide.

### Quick Start

```typescript
import { 
  loadAirports, 
  searchAirports, 
  getAirportByICAO,
  getAirportsWithinDistance,
  getAirportsByCountry 
} from './services/airportService';

// Load the airport dataset (call once during app initialization)
const airports = await loadAirports();

// Search for airports by name, city, ICAO, or IATA code
const results = searchAirports('Los Angeles');

// Get a specific airport by ICAO code
const jfk = getAirportByICAO('KJFK');

// Find airports within a radius
const nearby = getAirportsWithinDistance(
  { lat: 40.7128, lon: -74.0060 }, // NYC coordinates
  50 // miles
);

// Get all airports in a country
const usAirports = getAirportsByCountry('US');
```

### Features

- **Lazy Loading**: Data is loaded only when first needed
- **In-Memory Caching**: Fast access after initial load
- **Multi-Field Search**: Search by ICAO, IATA, name, or city
- **Distance Filtering**: Find airports within a radius using Haversine formula
- **Country Filtering**: Get all airports in a specific country
- **Type Safety**: Full TypeScript support with detailed interfaces

### Performance

- Search operations: <100ms
- ICAO lookups: <0.01ms (O(1))
- Optimized for 50,000+ airports
- See [PERFORMANCE.md](./PERFORMANCE.md) for detailed benchmarks

### Testing

The service has comprehensive test coverage (>95%) covering:

- Lazy loading and caching behavior
- Search functionality across all fields
- Distance calculations and filtering
- Edge cases and error handling
- Performance characteristics

Run tests:

```bash
npm test -- __tests__/services/airportService.test.ts
```

### API Reference

#### `loadAirports(): Promise<AirportData>`

Loads the airport dataset. Called automatically by other functions, but can be called explicitly during app initialization for better performance.

#### `searchAirports(query: string): Airport[]`

Searches for airports matching the query across multiple fields. Results are scored and sorted by relevance.

**Parameters:**

- `query`: Search term (name, city, ICAO, or IATA code)

**Returns:** Array of matching airports, sorted by relevance

#### `getAirportByICAO(icao: string): Airport | null`

Retrieves a single airport by its ICAO code. Case-insensitive O(1) lookup.

**Parameters:**

- `icao`: 4-letter ICAO code

**Returns:** Airport object or null if not found

#### `getAirportsWithinDistance(origin: Coordinates, maxDistance: number): Airport[]`

Finds all airports within a specified distance from an origin point.

**Parameters:**

- `origin`: Coordinates object with lat and lon
- `maxDistance`: Maximum distance in miles

**Returns:** Array of airports within range, sorted by distance (nearest first)

#### `getAirportsByCountry(country: string): Airport[]`

Retrieves all airports in a specific country.

**Parameters:**

- `country`: 2-letter country code (e.g., "US", "GB", "FR")

**Returns:** Array of airports in the specified country

### Types

See [../types/airport.ts](../types/airport.ts) for detailed type definitions:

- `Airport`: Individual airport with all metadata
- `AirportData`: Complete dataset as ICAO-keyed dictionary
- `Coordinates`: Latitude/longitude pair (from distance utils)

### Dependencies

The Airport Service depends on:

- `utils/distance.ts`: Haversine distance calculations
- `data/airports.json`: Airport dataset (~29,000 airports)

### Future Enhancements

Potential improvements for future phases:

- Advanced filtering (elevation, timezone, state)
- Custom sorting options
- Result pagination for large datasets
- Debounced search wrapper for UI integration
- Web Worker support for background processing
