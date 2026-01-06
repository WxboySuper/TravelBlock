import { useCallback, useEffect, useState } from 'react';
import { Alert } from 'react-native';

import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';

function useLoadHomeAirport() {
  const [homeAirport, setHomeAirport] = useState<Airport | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const saved = await storageService.getHomeAirport();
        if (mounted) setHomeAirport(saved);
      } catch (error) {
        console.error('Failed to load home airport', error);
      } finally {
        if (mounted) setIsLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  return { homeAirport, setHomeAirport, isLoading };
}

export function useHomeAirport() {
  const { homeAirport, setHomeAirport, isLoading } = useLoadHomeAirport();

  const handleSelectAirport = useCallback(
    async (airport: Airport) => {
      const previousAirport = homeAirport;
      setHomeAirport(airport);

      try {
        await storageService.saveHomeAirport(airport);
      } catch (error) {
        console.error('Failed to save home airport', error);
        setHomeAirport(previousAirport ?? null);
        Alert.alert('Save failed', 'Unable to save home airport');
        throw error;
      }
    },
    [homeAirport, setHomeAirport]
  );

  const handleClearHomeBase = useCallback(async () => {
    const previousAirport = homeAirport;
    setHomeAirport(null);

    try {
      await storageService.clearHomeAirport();
    } catch (error) {
      console.error('Failed to clear home airport', error);
      setHomeAirport(previousAirport);
      Alert.alert('Clear failed', 'Unable to clear home airport');
    }
  }, [homeAirport, setHomeAirport]);

  return {
    homeAirport,
    isLoading,
    handleSelectAirport,
    handleClearHomeBase,
  };
}
