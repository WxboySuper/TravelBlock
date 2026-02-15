/**
 * Time-related type definitions for flight duration selection and display.
 *
 * @module types/time
 */

/**
 * Time units supported by the application.
 */
export enum TimeUnit {
  SECONDS = "seconds",
  MINUTES = "minutes",
  HOURS = "hours",
}

/**
 * Represents a time value with its numeric value and unit.
 */
export interface TimeValue {
  /** Numeric value of the time */
  value: number;
  /** Unit of the time value */
  unit: TimeUnit;
}

/**
 * Represents a range of time values with minimum and maximum bounds.
 */
export interface TimeRange {
  /** Minimum time value in seconds */
  min: number;
  /** Maximum time value in seconds */
  max: number;
  /** Snap interval in seconds (e.g., 600 for 10-minute intervals) */
  interval: number;
}

/**
 * Formatted time display with hours and minutes.
 */
export interface FormattedTime {
  /** Hours component */
  hours: number;
  /** Minutes component */
  minutes: number;
  /** Formatted string (e.g., "1h 30m") */
  formatted: string;
}
