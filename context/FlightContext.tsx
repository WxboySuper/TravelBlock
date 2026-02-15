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
import { BoardingPass, FlightBooking, Seat } from "@/types/flight";
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
  /** Flight booking details */
  booking: FlightBooking | null;
  /** Selected seat */
  seat: Seat | null;
  /** Boarding pass */
  boardingPass: BoardingPass | null;
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
  /** Set the flight booking */
  setBooking: (booking: FlightBooking | null) => Promise<void>;
  /** Set the selected seat */
  setSeat: (seat: Seat | null) => Promise<void>;
  /** Set the boarding pass */
  setBoardingPass: (boardingPass: BoardingPass | null) => Promise<void>;
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
  booking: null,
  seat: null,
  boardingPass: null,
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
      const [origin, destination, duration, booking, seat, boardingPass] = await Promise.all([
        storageService.getGenericItem<Airport>(StorageKey.FLIGHT_ORIGIN),
        storageService.getGenericItem<Airport>(StorageKey.FLIGHT_DESTINATION),
        storageService.getGenericItem<number>(StorageKey.FLIGHT_DURATION),
        storageService.getGenericItem<FlightBooking>(StorageKey.FLIGHT_BOOKING),
        storageService.getGenericItem<Seat>(StorageKey.FLIGHT_SEAT),
        storageService.getGenericItem<BoardingPass>(StorageKey.BOARDING_PASS),
      ]);

      setState({
        origin: origin ?? null,
        destination: destination ?? null,
        flightDuration: duration ?? 3600,
        booking: booking ?? null,
        seat: seat ?? null,
        boardingPass: boardingPass ?? null,
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
   * Set flight booking and persist to storage.
   */
  const setBooking = useCallback(async (booking: FlightBooking | null) => {
    setState((prev) => ({ ...prev, booking }));

    try {
      if (booking) {
        await storageService.setGenericItem(StorageKey.FLIGHT_BOOKING, booking);
      } else {
        await storageService.removeGenericItem(StorageKey.FLIGHT_BOOKING);
      }
    } catch (error) {
      console.error("Failed to save booking:", error);
    }
  }, []);

  /**
   * Set selected seat and persist to storage.
   */
  const setSeat = useCallback(async (seat: Seat | null) => {
    setState((prev) => ({ ...prev, seat }));

    try {
      if (seat) {
        await storageService.setGenericItem(StorageKey.FLIGHT_SEAT, seat);
      } else {
        await storageService.removeGenericItem(StorageKey.FLIGHT_SEAT);
      }
    } catch (error) {
      console.error("Failed to save seat:", error);
    }
  }, []);

  /**
   * Set boarding pass and persist to storage.
   */
  const setBoardingPass = useCallback(async (boardingPass: BoardingPass | null) => {
    setState((prev) => ({ ...prev, boardingPass }));

    try {
      if (boardingPass) {
        await storageService.setGenericItem(StorageKey.BOARDING_PASS, boardingPass);
      } else {
        await storageService.removeGenericItem(StorageKey.BOARDING_PASS);
      }
    } catch (error) {
      console.error("Failed to save boarding pass:", error);
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
      booking: null,
      seat: null,
      boardingPass: null,
      isLoaded: true,
    });

    try {
      await Promise.all([
        storageService.removeGenericItem(StorageKey.FLIGHT_ORIGIN),
        storageService.removeGenericItem(StorageKey.FLIGHT_DESTINATION),
        storageService.removeGenericItem(StorageKey.FLIGHT_DURATION),
        storageService.removeGenericItem(StorageKey.FLIGHT_BOOKING),
        storageService.removeGenericItem(StorageKey.FLIGHT_SEAT),
        storageService.removeGenericItem(StorageKey.BOARDING_PASS),
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
    setBooking,
    setSeat,
    setBoardingPass,
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
