/**
 * Unit tests for time slider utilities.
 */

import {
    createTimeRange,
    formatTimeValue,
    getDefaultTimeRange,
    getTimeInRange,
    hoursToSeconds,
    minutesToSeconds,
    parseTimeValue,
    secondsToHours,
    secondsToMinutes,
    snapToInterval,
    TIME_SLIDER_CONFIG,
} from "@/utils/timeSlider";

describe("timeSlider utilities", () => {
  describe("snapToInterval", () => {
    it("should snap to nearest interval", () => {
      expect(snapToInterval(1234, 600)).toBe(1200); // 20 minutes (1234 is closer to 1200 than 1800)
      expect(snapToInterval(905, 600)).toBe(1200); // 20 minutes (905 is closer to 1200 than 600)
      expect(snapToInterval(1750, 600)).toBe(1800); // 30 minutes
    });

    it("should handle exact intervals", () => {
      expect(snapToInterval(1800, 600)).toBe(1800);
      expect(snapToInterval(3600, 600)).toBe(3600);
    });

    it("should handle edge cases", () => {
      expect(snapToInterval(0, 600)).toBe(0);
      expect(snapToInterval(299, 600)).toBe(0);
      expect(snapToInterval(300, 600)).toBe(600);
    });

    it("should return original value if interval is 0 or negative", () => {
      expect(snapToInterval(1234, 0)).toBe(1234);
      expect(snapToInterval(1234, -1)).toBe(1234);
    });
  });

  describe("getTimeInRange", () => {
    it("should constrain value within range", () => {
      expect(getTimeInRange(1000, 800, 1200)).toBe(1000);
      expect(getTimeInRange(500, 800, 1200)).toBe(800);
      expect(getTimeInRange(1500, 800, 1200)).toBe(1200);
    });

    it("should handle boundary values", () => {
      expect(getTimeInRange(800, 800, 1200)).toBe(800);
      expect(getTimeInRange(1200, 800, 1200)).toBe(1200);
    });

    it("should handle negative values", () => {
      expect(getTimeInRange(-100, 0, 1000)).toBe(0);
    });
  });

  describe("time unit conversions", () => {
    it("should convert seconds to minutes", () => {
      expect(secondsToMinutes(60)).toBe(1);
      expect(secondsToMinutes(120)).toBe(2);
      expect(secondsToMinutes(1800)).toBe(30);
      expect(secondsToMinutes(3600)).toBe(60);
    });

    it("should convert minutes to seconds", () => {
      expect(minutesToSeconds(1)).toBe(60);
      expect(minutesToSeconds(2)).toBe(120);
      expect(minutesToSeconds(30)).toBe(1800);
      expect(minutesToSeconds(60)).toBe(3600);
    });

    it("should convert seconds to hours", () => {
      expect(secondsToHours(3600)).toBe(1);
      expect(secondsToHours(7200)).toBe(2);
      expect(secondsToHours(1800)).toBe(0.5);
    });

    it("should convert hours to seconds", () => {
      expect(hoursToSeconds(1)).toBe(3600);
      expect(hoursToSeconds(2)).toBe(7200);
      expect(hoursToSeconds(0.5)).toBe(1800);
    });

    it("should handle zero and decimal values", () => {
      expect(secondsToMinutes(0)).toBe(0);
      expect(minutesToSeconds(0)).toBe(0);
      expect(secondsToMinutes(90)).toBe(1.5);
    });
  });

  describe("formatTimeValue", () => {
    it("should format hours and minutes correctly", () => {
      expect(formatTimeValue(3600)).toEqual({
        hours: 1,
        minutes: 0,
        formatted: "1h 0m",
      });

      expect(formatTimeValue(5400)).toEqual({
        hours: 1,
        minutes: 30,
        formatted: "1h 30m",
      });

      expect(formatTimeValue(7200)).toEqual({
        hours: 2,
        minutes: 0,
        formatted: "2h 0m",
      });

      expect(formatTimeValue(18000)).toEqual({
        hours: 5,
        minutes: 0,
        formatted: "5h 0m",
      });
    });

    it("should format minutes-only durations", () => {
      expect(formatTimeValue(1800)).toEqual({
        hours: 0,
        minutes: 30,
        formatted: "30m",
      });

      expect(formatTimeValue(600)).toEqual({
        hours: 0,
        minutes: 10,
        formatted: "10m",
      });

      expect(formatTimeValue(2400)).toEqual({
        hours: 0,
        minutes: 40,
        formatted: "40m",
      });
    });

    it("should handle edge cases", () => {
      expect(formatTimeValue(0)).toEqual({
        hours: 0,
        minutes: 0,
        formatted: "0m",
      });

      expect(formatTimeValue(59)).toEqual({
        hours: 0,
        minutes: 0,
        formatted: "0m",
      });

      expect(formatTimeValue(60)).toEqual({
        hours: 0,
        minutes: 1,
        formatted: "1m",
      });
    });
  });

  describe("parseTimeValue", () => {
    it("should parse hours and minutes format", () => {
      expect(parseTimeValue("1h 30m")).toBe(5400);
      expect(parseTimeValue("2h 0m")).toBe(7200);
      expect(parseTimeValue("5h 0m")).toBe(18000);
    });

    it("should parse minutes-only format", () => {
      expect(parseTimeValue("30m")).toBe(1800);
      expect(parseTimeValue("10m")).toBe(600);
      expect(parseTimeValue("45m")).toBe(2700);
    });

    it("should parse hours-only format", () => {
      expect(parseTimeValue("1h")).toBe(3600);
      expect(parseTimeValue("2h")).toBe(7200);
      expect(parseTimeValue("5h")).toBe(18000);
    });

    it("should handle various whitespace", () => {
      expect(parseTimeValue("1h30m")).toBe(5400);
      expect(parseTimeValue("  1h 30m  ")).toBe(5400);
      expect(parseTimeValue("1h  30m")).toBe(5400);
    });

    it("should return 0 for invalid formats", () => {
      expect(parseTimeValue("invalid")).toBe(0);
      expect(parseTimeValue("")).toBe(0);
      expect(parseTimeValue("abc")).toBe(0);
    });

    it("should round-trip with formatTimeValue", () => {
      const testValues = [1800, 3600, 5400, 7200, 18000];
      testValues.forEach((seconds) => {
        const formatted = formatTimeValue(seconds).formatted;
        const parsed = parseTimeValue(formatted);
        expect(parsed).toBe(seconds);
      });
    });
  });

  describe("createTimeRange", () => {
    it("should create valid time range", () => {
      const range = createTimeRange(1800, 18000, 600);
      expect(range).toEqual({
        min: 1800,
        max: 18000,
        interval: 600,
      });
    });

    it("should throw error if min >= max", () => {
      expect(() => createTimeRange(1800, 1800, 600)).toThrow();
      expect(() => createTimeRange(2000, 1800, 600)).toThrow();
    });

    it("should throw error if interval <= 0", () => {
      expect(() => createTimeRange(1800, 18000, 0)).toThrow();
      expect(() => createTimeRange(1800, 18000, -1)).toThrow();
    });
  });

  describe("getDefaultTimeRange", () => {
    it("should return default configuration", () => {
      const range = getDefaultTimeRange();
      expect(range.min).toBe(TIME_SLIDER_CONFIG.MIN_TIME);
      expect(range.max).toBe(TIME_SLIDER_CONFIG.MAX_TIME);
      expect(range.interval).toBe(TIME_SLIDER_CONFIG.SNAP_INTERVAL);
    });

    it("should match expected values", () => {
      const range = getDefaultTimeRange();
      expect(range.min).toBe(1800); // 30 minutes
      expect(range.max).toBe(18000); // 5 hours
      expect(range.interval).toBe(600); // 10 minutes
    });
  });

  describe("TIME_SLIDER_CONFIG constants", () => {
    it("should have correct constant values", () => {
      expect(TIME_SLIDER_CONFIG.MIN_TIME).toBe(1800);
      expect(TIME_SLIDER_CONFIG.MAX_TIME).toBe(18000);
      expect(TIME_SLIDER_CONFIG.SNAP_INTERVAL).toBe(600);
    });
  });
});
