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

  useEffect(() => {
    return () => {
      requestIdRef.current += 1;

      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
        debounceTimerRef.current = null;
      }
    };
  }, []);

  // Convert origin to coordinates
  const originCoords: Coordinates | null = useMemo(() => {
    if (!origin) return null;
    return { lat: origin.lat, lon: origin.lon };
  }, [origin]);

  const isCurrentRequest = useCallback((requestId: number) => {
    return requestId === requestIdRef.current;
  }, []);

  const applySuccess = useCallback(
    (requestId: number, results: AirportWithFlightTime[]) => {
      if (!isCurrentRequest(requestId)) {
        return false;
      }

      setDestinations(results);
      setError(null);
      return true;
    },
    [isCurrentRequest]
  );

  const applyFailure = useCallback(
    (requestId: number, err: unknown) => {
      if (!isCurrentRequest(requestId)) {
        return false;
      }

      setError(err instanceof Error ? err.message : "Failed to fetch destinations");
      setDestinations([]);
      return true;
    },
    [isCurrentRequest]
  );

  const finishRequest = useCallback(
    (requestId: number) => {
      if (!isCurrentRequest(requestId)) {
        return;
      }

      setIsLoading(false);
    },
    [isCurrentRequest]
  );

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
      if (!isCurrentRequest(requestId)) {
        return;
      }

      const results = useTimeRange
        ? getDestinationsInTimeRange(
          originCoords,
          flightTimeInSeconds,
          tolerance
        )
        : getDestinationsByFlightTime(originCoords, flightTimeInSeconds);

      applySuccess(requestId, results);
    } catch (err) {
      applyFailure(requestId, err);
    } finally {
      finishRequest(requestId);
    }
  }, [applyFailure, applySuccess, finishRequest, isCurrentRequest, originCoords, flightTimeInSeconds, useTimeRange, tolerance]);

  // Debounced fetch
  useEffect(() => {
    // Clear existing timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Set new timer
    const timer = setTimeout(() => {
      fetchDestinations();
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
    fetchDestinations();
  }, [fetchDestinations]);

  return {
    destinations,
    isLoading,
    error,
    refresh,
  };
}
