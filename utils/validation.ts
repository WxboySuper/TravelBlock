import type { Airport } from '../types/airport';

/**
 * Checks if a value is a non-null object.
 */
function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

/**
 * Validates if the given data matches the Airport interface structure.
 * This is a type guard that ensures the data is safe to treat as an Airport object.
 *
 * @param data - The unknown data to validate
 * @returns true if data matches Airport schema, false otherwise
 */
export function isValidAirport(data: unknown): data is Airport {
  if (!isObject(data)) {
    return false;
  }

  // Check required string fields
  const stringFields = ['icao', 'name', 'city', 'country', 'state', 'tz', 'iata'];
  const hasValidStrings = stringFields.every(field => typeof data[field] === 'string');
  if (!hasValidStrings) return false;

  // Check required number fields and finiteness
  const numberFields = ['lat', 'lon', 'elevation'];
  const hasValidNumbers = numberFields.every(field => Number.isFinite(data[field]));

  return hasValidNumbers;
}
