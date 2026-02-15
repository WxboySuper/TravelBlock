/**
 * Flight context for managing flight setup state across the application.
 *
 * Provides state management for:
 * - Origin airport (home base)
 * - Destination airport (selected destination)
 * - Flight duration (selected time)
 *
 * State is persisted to AsyncStorage for recovery on app restart.
 *
 * @module context/FlightContext
 */

import { storageService } from "@/services/storageService";
import { Airport } from "@/types/airport";
import { StorageKey } from "@/types/storage";
import { createContext, ReactNode, useCallback, useContext, useEffect, useState } from "react";

/**
 * Flight state interface.
 */
export interface FlightState {
  /** Origin airport */
  origin: Airport | null;
  /** Destination airport */
  destination: Airport | null;
  /** Selected flight duration in seconds */
  flightDuration: number;
  /** Whether flight state is loaded from storage */
  isLoaded: boolean;
}

/**
 * Flight context value interface.
 */
interface FlightContextValue extends FlightState {
  /** Set the origin airport */
  setOrigin: (airport: Airport | null) => Promise<void>;
  /** Set the destination airport */
  setDestination: (airport: Airport | null) => Promise<void>;
  /** Set the flight duration in seconds */
  setFlightDuration: (seconds: number) => Promise<void>;
  /** Clear all flight state */
  clearFlightState: () => Promise<void>;
  /** Refresh flight state from storage */
  refreshFlightState: () => Promise<void>;
}

const FlightContext = createContext<FlightContextValue | null>(null);

/**
 * Default flight state.
 */
const DEFAULT_FLIGHT_STATE: FlightState = {
  origin: null,
  destination: null,
  flightDuration: 3600, // Default to 1 hour
  isLoaded: false,
};

/**
 * Flight context provider component.
 *
 * @example
 * ```tsx
 * // In app/_layout.tsx
 * <FlightProvider>
 *   <Stack>
 *     <Stack.Screen name="(tabs)" />
 *     <Stack.Screen name="flight/setup" />
 *   </Stack>
 * </FlightProvider>
 * ```
 */
export function FlightProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<FlightState>(DEFAULT_FLIGHT_STATE);

  // Load flight state from storage on mount
  useEffect(() => {
    loadFlightState();
  }, []);

  /**
   * Load flight state from AsyncStorage.
   */
  const loadFlightState = async () => {
    try {
      const [origin, destination, duration] = await Promise.all([
        storageService.getGenericItem<Airport>(StorageKey.FLIGHT_ORIGIN),
        storageService.getGenericItem<Airport>(StorageKey.FLIGHT_DESTINATION),
        storageService.getGenericItem<number>(StorageKey.FLIGHT_DURATION),
      ]);

      setState({
        origin: origin ?? null,
        destination: destination ?? null,
        flightDuration: duration ?? 3600,
        isLoaded: true,
      });
    } catch (error) {
      console.error("Failed to load flight state:", error);
      setState({ ...DEFAULT_FLIGHT_STATE, isLoaded: true });
    }
  };

  /**
   * Set origin airport and persist to storage.
   */
  const setOrigin = useCallback(async (airport: Airport | null) => {
    setState((prev) => ({ ...prev, origin: airport }));

    try {
      if (airport) {
        await storageService.setGenericItem(StorageKey.FLIGHT_ORIGIN, airport);
      } else {
        await storageService.removeGenericItem(StorageKey.FLIGHT_ORIGIN);
      }
    } catch (error) {
      console.error("Failed to save origin:", error);
    }
  }, []);

  /**
   * Set destination airport and persist to storage.
   */
  const setDestination = useCallback(async (airport: Airport | null) => {
    setState((prev) => ({ ...prev, destination: airport }));

    try {
      if (airport) {
        await storageService.setGenericItem(StorageKey.FLIGHT_DESTINATION, airport);
      } else {
        await storageService.removeGenericItem(StorageKey.FLIGHT_DESTINATION);
      }
    } catch (error) {
      console.error("Failed to save destination:", error);
    }
  }, []);

  /**
   * Set flight duration and persist to storage.
   */
  const setFlightDuration = useCallback(async (seconds: number) => {
    setState((prev) => ({ ...prev, flightDuration: seconds }));

    try {
      await storageService.setGenericItem(StorageKey.FLIGHT_DURATION, seconds);
    } catch (error) {
      console.error("Failed to save flight duration:", error);
    }
  }, []);

  /**
   * Clear all flight state.
   */
  const clearFlightState = useCallback(async () => {
    setState({
      origin: null,
      destination: null,
      flightDuration: 3600,
      isLoaded: true,
    });

    try {
      await Promise.all([
        storageService.removeGenericItem(StorageKey.FLIGHT_ORIGIN),
        storageService.removeGenericItem(StorageKey.FLIGHT_DESTINATION),
        storageService.removeGenericItem(StorageKey.FLIGHT_DURATION),
      ]);
    } catch (error) {
      console.error("Failed to clear flight state:", error);
    }
  }, []);

  /**
   * Refresh flight state from storage.
   */
  const refreshFlightState = useCallback(async () => {
    await loadFlightState();
  }, []);

  const value: FlightContextValue = {
    ...state,
    setOrigin,
    setDestination,
    setFlightDuration,
    clearFlightState,
    refreshFlightState,
  };

  return <FlightContext.Provider value={value}>{children}</FlightContext.Provider>;
}

/**
 * Hook to access flight context.
 *
 * @returns Flight context value
 * @throws Error if used outside FlightProvider
 *
 * @example
 * ```tsx
 * function FlightSetup() {
 *   const { origin, destination, setDestination } = useFlight();
 *
 *   return (
 *     <View>
 *       <Text>Origin: {origin?.name}</Text>
 *       <Text>Destination: {destination?.name ?? "Not selected"}</Text>
 *     </View>
 *   );
 * }
 * ```
 */
export function useFlight(): FlightContextValue {
  const context = useContext(FlightContext);

  if (!context) {
    throw new Error("useFlight must be used within a FlightProvider");
  }

  return context;
}
