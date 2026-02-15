/**
 * Time slider utility functions for time value manipulation, formatting, and snap logic.
 *
 * @module utils/timeSlider
 */

import { FormattedTime, TimeRange } from "@/types/time";

/**
 * Constants for time slider configuration.
 */
export const TIME_SLIDER_CONFIG = {
  /** Minimum flight duration in seconds (30 minutes) */
  MIN_TIME: 30 * 60,
  /** Maximum flight duration in seconds (5 hours) */
  MAX_TIME: 5 * 60 * 60,
  /** Snap interval in seconds (10 minutes) */
  SNAP_INTERVAL: 10 * 60,
} as const;

/**
 * Snaps a value to the nearest interval.
 *
 * @param value - The value to snap
 * @param interval - The interval to snap to
 * @returns The snapped value
 *
 * @example
 * ```typescript
 * snapToInterval(1234, 600); // Returns 1200 (20 minutes)
 * snapToInterval(905, 600);  // Returns 900 (15 minutes)
 * ```
 */
export function snapToInterval(value: number, interval: number): number {
  if (interval <= 0) {
    return value;
  }
  return Math.round(value / interval) * interval;
}

/**
 * Constrains a value within a specified range.
 *
 * @param value - The value to constrain
 * @param min - The minimum allowed value
 * @param max - The maximum allowed value
 * @returns The constrained value
 *
 * @example
 * ```typescript
 * getTimeInRange(1000, 800, 1200); // Returns 1000
 * getTimeInRange(500, 800, 1200);  // Returns 800
 * getTimeInRange(1500, 800, 1200); // Returns 1200
 * ```
 */
export function getTimeInRange(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value));
}

/**
 * Converts seconds to minutes.
 *
 * @param seconds - The number of seconds
 * @returns The equivalent number of minutes
 */
export function secondsToMinutes(seconds: number): number {
  return seconds / 60;
}

/**
 * Converts minutes to seconds.
 *
 * @param minutes - The number of minutes
 * @returns The equivalent number of seconds
 */
export function minutesToSeconds(minutes: number): number {
  return minutes * 60;
}

/**
 * Converts seconds to hours.
 *
 * @param seconds - The number of seconds
 * @returns The equivalent number of hours
 */
export function secondsToHours(seconds: number): number {
  return seconds / 3600;
}

/**
 * Converts hours to seconds.
 *
 * @param hours - The number of hours
 * @returns The equivalent number of seconds
 */
export function hoursToSeconds(hours: number): number {
  return hours * 3600;
}

/**
 * Formats a time value in seconds to a human-readable string.
 *
 * @param seconds - The time value in seconds
 * @returns Formatted time object with hours, minutes, and formatted string
 *
 * @example
 * ```typescript
 * formatTimeValue(3600);  // Returns { hours: 1, minutes: 0, formatted: "1h 0m" }
 * formatTimeValue(5400);  // Returns { hours: 1, minutes: 30, formatted: "1h 30m" }
 * formatTimeValue(1800);  // Returns { hours: 0, minutes: 30, formatted: "30m" }
 * formatTimeValue(7200);  // Returns { hours: 2, minutes: 0, formatted: "2h 0m" }
 * ```
 */
export function formatTimeValue(seconds: number): FormattedTime {
  const totalMinutes = Math.floor(seconds / 60);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  let formatted: string;
  if (hours > 0) {
    formatted = `${hours}h ${minutes}m`;
  } else {
    formatted = `${minutes}m`;
  }

  return {
    hours,
    minutes,
    formatted,
  };
}

/**
 * Parses a formatted time string back to seconds.
 *
 * Supports formats:
 * - "1h 30m" -> 5400 seconds
 * - "30m" -> 1800 seconds
 * - "2h 0m" -> 7200 seconds
 * - "2h" -> 7200 seconds
 *
 * @param formatted - The formatted time string
 * @returns The time value in seconds, or 0 if parsing fails
 *
 * @example
 * ```typescript
 * parseTimeValue("1h 30m"); // Returns 5400
 * parseTimeValue("30m");    // Returns 1800
 * parseTimeValue("2h");     // Returns 7200
 * parseTimeValue("invalid"); // Returns 0
 * ```
 */
export function parseTimeValue(formatted: string): number {
  const trimmed = formatted.trim();
  
  // Match patterns like "1h 30m", "1h", "30m"
  const hoursMatch = trimmed.match(/(\d+)h/);
  const minutesMatch = trimmed.match(/(\d+)m/);

  const hours = hoursMatch ? parseInt(hoursMatch[1], 10) : 0;
  const minutes = minutesMatch ? parseInt(minutesMatch[1], 10) : 0;

  return hoursToSeconds(hours) + minutesToSeconds(minutes);
}

/**
 * Creates a TimeRange object with validation.
 *
 * @param min - Minimum time in seconds
 * @param max - Maximum time in seconds
 * @param interval - Snap interval in seconds
 * @returns A validated TimeRange object
 * @throws Error if min >= max or interval <= 0
 *
 * @example
 * ```typescript
 * createTimeRange(1800, 18000, 600);
 * // Returns { min: 1800, max: 18000, interval: 600 }
 * ```
 */
export function createTimeRange(min: number, max: number, interval: number): TimeRange {
  if (min >= max) {
    throw new Error("TimeRange min must be less than max");
  }
  if (interval <= 0) {
    throw new Error("TimeRange interval must be greater than 0");
  }

  return { min, max, interval };
}

/**
 * Gets the default time range for the slider (30m to 5h with 10m intervals).
 *
 * @returns The default TimeRange configuration
 */
export function getDefaultTimeRange(): TimeRange {
  return {
    min: TIME_SLIDER_CONFIG.MIN_TIME,
    max: TIME_SLIDER_CONFIG.MAX_TIME,
    interval: TIME_SLIDER_CONFIG.SNAP_INTERVAL,
  };
}
