import type { Airport } from '../types/airport';

/**
 * Validates if the given data matches the Airport interface structure.
 * This is a type guard that ensures the data is safe to treat as an Airport object.
 *
 * @param data - The unknown data to validate
 * @returns true if data matches Airport schema, false otherwise
 */
export function isValidAirport(data: unknown): data is Airport {
  if (typeof data !== 'object' || data === null) {
    return false;
  }

  // Cast to Record<string, unknown> to access properties safely
  const airport = data as Record<string, unknown>;

  // Check required string fields
  const stringFields = ['icao', 'name', 'city', 'country'];
  for (const field of stringFields) {
    if (typeof airport[field] !== 'string') {
      return false;
    }
  }

  // Check required number fields and finiteness
  const numberFields = ['lat', 'lon'];
  for (const field of numberFields) {
    const value = airport[field];
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      return false;
    }
  }

  // Optional fields or fields we want to be lenient about can be skipped or checked loosely.
  // 'iata' might be empty string but should be string if present?
  // The interface says 'iata: string', so it must be a string.
  if (typeof airport.iata !== 'string') {
    return false;
  }

  return true;
}
