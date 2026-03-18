/**
 * Hook for managing destinations based on origin and flight time.
 *
 * @module hooks/useDestinations
 */

import { getDestinationsByFlightTime, getDestinationsInTimeRange } from "@/services/radiusService";
import { loadAirports } from "@/services/airportService";
import { Airport } from "@/types/airport";
import { Coordinates } from "@/types/location";
import { AirportWithFlightTime } from "@/types/radius";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

interface UseDestinationsOptions {
  /** Origin airport */
  origin: Airport | null;
  /** Flight time in seconds */
  flightTimeInSeconds: number;
  /** Whether to use exact time range (true) or max time (false) */
  useTimeRange?: boolean;
  /** Tolerance for time range matching (default: 5%) */
  tolerance?: number;
  /** Debounce delay in milliseconds (default: 300ms) */
  debounceMs?: number;
}

interface UseDestinationsResult {
  /** List of destination airports */
  destinations: AirportWithFlightTime[];
  /** Whether destinations are loading */
  isLoading: boolean;
  /** Error message if loading failed */
  error: string | null;
  /** Refresh destinations */
  refresh: () => void;
}

/**
 * Custom hook for fetching and managing destinations based on origin and flight time.
 *
 * Features:
 * - Automatic updates when origin or flight time changes
 * - Debouncing to prevent excessive calculations
 * - Loading and error states
 * - Memoized results for performance
 *
 * @param options - Configuration options
 * @returns Destinations state and control functions
 *
 * @example
 * ```tsx
 * const { destinations, isLoading, error } = useDestinations({
 *   origin: homeAirport,
 *   flightTimeInSeconds: 3600, // 1 hour
 *   useTimeRange: true,
 * });
 *
 * if (isLoading) return <LoadingSpinner />;
 * if (error) return <ErrorMessage message={error} />;
 *
 * return <DestinationsList destinations={destinations} />;
 * ```
 */
export function useDestinations({
  origin,
  flightTimeInSeconds,
  useTimeRange = true,
  tolerance,
  debounceMs = 300,
}: UseDestinationsOptions): UseDestinationsResult {
  const [destinations, setDestinations] = useState<AirportWithFlightTime[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const requestIdRef = useRef(0);

  // Convert origin to coordinates
  const originCoords: Coordinates | null = useMemo(() => {
    if (!origin) return null;
    return { lat: origin.lat, lon: origin.lon };
  }, [origin]);

  // Fetch destinations
  const fetchDestinations = useCallback(async () => {
    const requestId = ++requestIdRef.current;

    if (!originCoords) {
      setDestinations([]);
      setError("No origin airport selected");
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await loadAirports();
      if (requestId !== requestIdRef.current) {
        return;
      }

      let results: AirportWithFlightTime[];

      if (useTimeRange) {
        results = getDestinationsInTimeRange(
          originCoords,
          flightTimeInSeconds,
          tolerance
        );
      } else {
        results = getDestinationsByFlightTime(originCoords, flightTimeInSeconds);
      }

      if (requestId !== requestIdRef.current) {
        return;
      }

      setDestinations(results);
      setError(null);
    } catch (err) {
      if (requestId !== requestIdRef.current) {
        return;
      }

      setError(err instanceof Error ? err.message : "Failed to fetch destinations");
      setDestinations([]);
    } finally {
      if (requestId !== requestIdRef.current) {
        return;
      }

      setIsLoading(false);
    }
  }, [originCoords, flightTimeInSeconds, useTimeRange, tolerance]);

  // Debounced fetch
  useEffect(() => {
    // Clear existing timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Set new timer
    const timer = setTimeout(() => {
      void fetchDestinations();
    }, debounceMs);

    debounceTimerRef.current = timer;

    // Cleanup
    return () => {
      if (debounceTimerRef.current === timer) {
        debounceTimerRef.current = null;
      }
      clearTimeout(timer);
    };
  }, [debounceMs, fetchDestinations]);

  const refresh = useCallback(() => {
    void fetchDestinations();
  }, [fetchDestinations]);

  return {
    destinations,
    isLoading,
    error,
    refresh,
  };
}
