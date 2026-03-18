/**
 * Hook for managing destinations based on origin and flight time.
 *
 * @module hooks/useDestinations
 */

import {
  getDestinationsByFlightTime,
  getDestinationsInTimeBucket,
  getDestinationsInTimeRange,
} from "@/services/radiusService";
import { loadAirports } from "@/services/airportService";
import { Airport } from "@/types/airport";
import { Coordinates } from "@/types/location";
import { AirportWithFlightTime } from "@/types/radius";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { Dispatch, MutableRefObject, SetStateAction } from "react";

interface UseDestinationsOptions {
  /** Origin airport */
  origin: Airport | null;
  /** Flight time in seconds */
  flightTimeInSeconds: number;
  /** Whether to use exact time range (true) or max time (false) when no bucket is configured */
  useTimeRange?: boolean;
  /** If provided, bucketed lookup is used regardless of useTimeRange */
  bucketIntervalSeconds?: number;
  /** Maximum time for the first bucket */
  initialBucketMaxTime?: number;
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

interface DestinationStateSetters {
  setDestinations: Dispatch<SetStateAction<AirportWithFlightTime[]>>;
  setError: Dispatch<SetStateAction<string | null>>;
  setIsLoading: Dispatch<SetStateAction<boolean>>;
}

function getOriginCoordinates(origin: Airport | null): Coordinates | null {
  if (!origin) return null;
  return { lat: origin.lat, lon: origin.lon };
}

function clearDebounceTimer(
  debounceTimerRef: MutableRefObject<ReturnType<typeof setTimeout> | null>
) {
  if (!debounceTimerRef.current) {
    return;
  }

  clearTimeout(debounceTimerRef.current);
  debounceTimerRef.current = null;
}

function invalidateDestinationRequests(
  requestIdRef: MutableRefObject<number>,
  debounceTimerRef: MutableRefObject<ReturnType<typeof setTimeout> | null>
) {
  requestIdRef.current += 1;
  clearDebounceTimer(debounceTimerRef);
}

function isCurrentRequest(
  requestIdRef: MutableRefObject<number>,
  requestId: number
) {
  return requestId === requestIdRef.current;
}

interface DestinationResolutionConfig {
  originCoords: Coordinates;
  flightTimeInSeconds: number;
  useTimeRange: boolean;
  bucketIntervalSeconds?: number;
  initialBucketMaxTime?: number;
  tolerance?: number;
}

function resolveDestinations({
  originCoords,
  flightTimeInSeconds,
  useTimeRange,
  bucketIntervalSeconds,
  initialBucketMaxTime,
  tolerance,
}: DestinationResolutionConfig) {
  if (bucketIntervalSeconds) {
    return getDestinationsInTimeBucket({
      origin: originCoords,
      timeInSeconds: flightTimeInSeconds,
      bucketSizeInSeconds: bucketIntervalSeconds,
      initialBucketMaxTime,
    });
  }

  return useTimeRange
    ? getDestinationsInTimeRange({
        origin: originCoords,
        timeInSeconds: flightTimeInSeconds,
        tolerance,
      })
    : getDestinationsByFlightTime({
        origin: originCoords,
        maxFlightTime: flightTimeInSeconds,
      });
}

async function fetchDestinations({
  requestIdRef,
  originCoords,
  flightTimeInSeconds,
  useTimeRange,
  bucketIntervalSeconds,
  initialBucketMaxTime,
  tolerance,
  setDestinations,
  setError,
  setIsLoading,
}: {
  requestIdRef: MutableRefObject<number>;
  originCoords: Coordinates | null;
  flightTimeInSeconds: number;
  useTimeRange: boolean;
  bucketIntervalSeconds?: number;
  initialBucketMaxTime?: number;
  tolerance?: number;
} & DestinationStateSetters) {
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
    if (!isCurrentRequest(requestIdRef, requestId)) {
      return;
    }

    const results = resolveDestinations({
      originCoords,
      flightTimeInSeconds,
      useTimeRange,
      bucketIntervalSeconds,
      initialBucketMaxTime,
      tolerance
    });
    if (!isCurrentRequest(requestIdRef, requestId)) {
      return;
    }

    setDestinations(results);
    setError(null);
  } catch (err) {
    if (!isCurrentRequest(requestIdRef, requestId)) {
      return;
    }

    setError(err instanceof Error ? err.message : "Failed to fetch destinations");
    setDestinations([]);
  } finally {
    if (isCurrentRequest(requestIdRef, requestId)) {
      setIsLoading(false);
    }
  }
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
  bucketIntervalSeconds,
  initialBucketMaxTime,
  tolerance,
  debounceMs = 300,
}: UseDestinationsOptions): UseDestinationsResult {
  const [destinations, setDestinations] = useState<AirportWithFlightTime[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const requestIdRef = useRef(0);

  useEffect(() => {
    return () => invalidateDestinationRequests(requestIdRef, debounceTimerRef);
  }, []);

  const originCoords = useMemo(() => getOriginCoordinates(origin), [origin]);

  const fetchAndStoreDestinations = useCallback(() => {
    return fetchDestinations({
      requestIdRef,
      originCoords,
      flightTimeInSeconds,
      useTimeRange,
      bucketIntervalSeconds,
      initialBucketMaxTime,
      tolerance,
      setDestinations,
      setError,
      setIsLoading,
    });
  }, [
    bucketIntervalSeconds,
    flightTimeInSeconds,
    initialBucketMaxTime,
    originCoords,
    tolerance,
    useTimeRange,
  ]);

  // Debounced fetch
  useEffect(() => {
    clearDebounceTimer(debounceTimerRef);

    // Set new timer
    const timer = setTimeout(() => {
      fetchAndStoreDestinations().catch(() => undefined);
    }, debounceMs);

    debounceTimerRef.current = timer;

    // Cleanup
    return () => {
      if (debounceTimerRef.current === timer) {
        debounceTimerRef.current = null;
      }
      clearTimeout(timer);
    };
  }, [debounceMs, fetchAndStoreDestinations]);

  const refresh = useCallback(() => {
    fetchAndStoreDestinations().catch(() => undefined);
  }, [fetchAndStoreDestinations]);

  return {
    destinations,
    isLoading,
    error,
    refresh,
  };
}
