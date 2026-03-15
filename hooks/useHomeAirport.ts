import { useCallback, useEffect, useState } from 'react';
import { Alert } from 'react-native';

import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';

type HomeAirportState = {
  homeAirport: Airport | null;
  isLoading: boolean;
};

const listeners = new Set<(state: HomeAirportState) => void>();
let homeAirportState: HomeAirportState = {
  homeAirport: null,
  isLoading: true,
};
let loadPromise: Promise<Airport | null> | null = null;

export function resetHomeAirportStoreForTests() {
  listeners.clear();
  homeAirportState = {
    homeAirport: null,
    isLoading: true,
  };
  loadPromise = null;
}

function emitHomeAirportState() {
  for (const listener of listeners) {
    listener(homeAirportState);
  }
}

function setHomeAirportState(nextState: Partial<HomeAirportState>) {
  homeAirportState = { ...homeAirportState, ...nextState };
  emitHomeAirportState();
}

export function getHomeAirportSnapshot(): HomeAirportState {
  return homeAirportState;
}

export function loadHomeAirport(forceRefresh = false): Promise<Airport | null> {
  if (loadPromise && !forceRefresh) {
    return loadPromise;
  }

  setHomeAirportState({ isLoading: true });

  loadPromise = (async () => {
    try {
      const savedAirport = await storageService.getHomeAirport();
      setHomeAirportState({ homeAirport: savedAirport, isLoading: false });
      return savedAirport;
    } catch (error) {
      console.error('Failed to load home airport', error);
      setHomeAirportState({ homeAirport: null, isLoading: false });
      throw error;
    } finally {
      loadPromise = null;
    }
  })();

  return loadPromise;
}

export function subscribeToHomeAirportState(listener: (state: HomeAirportState) => void) {
  listeners.add(listener);
  listener(homeAirportState);

  return () => {
    listeners.delete(listener);
  };
}

export function useHomeAirport() {
  const [state, setState] = useState<HomeAirportState>(homeAirportState);

  useEffect(() => {
    const unsubscribe = subscribeToHomeAirportState(setState);

    if (homeAirportState.isLoading) {
      loadHomeAirport().catch(() => {
        // Errors are already logged in the shared loader.
      });
    }

    return unsubscribe;
  }, []);

  const handleSelectAirport = useCallback(
    async (airport: Airport) => {
      let previousAirport: Airport | null = null;
      previousAirport = homeAirportState.homeAirport;
      setHomeAirportState({ homeAirport: airport });

      try {
        await storageService.saveHomeAirport(airport);
      } catch (error) {
        console.error('Failed to save home airport', error);
        setHomeAirportState({ homeAirport: previousAirport });
        Alert.alert('Save failed', 'Unable to save home airport');
        throw error;
      }
    },
    []
  );

  const handleClearHomeBase = useCallback(async () => {
    let previousAirport: Airport | null = null;
    previousAirport = homeAirportState.homeAirport;
    setHomeAirportState({ homeAirport: null });

    try {
      await storageService.clearHomeAirport();
    } catch (error) {
      console.error('Failed to clear home airport', error);
      setHomeAirportState({ homeAirport: previousAirport });
      Alert.alert('Clear failed', 'Unable to clear home airport');
      throw error;
    }
  }, []);

  const refreshHomeAirport = useCallback(async () => {
    await loadHomeAirport(true);
  }, []);

  return {
    homeAirport: state.homeAirport,
    isLoading: state.isLoading,
    handleSelectAirport,
    handleClearHomeBase,
    refreshHomeAirport,
  };
}
