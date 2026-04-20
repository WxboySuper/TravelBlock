import { useColorScheme } from '@/hooks/use-color-scheme';
import type { Airport } from '@/types/airport';
import type { Position } from '@/utils/flightInterpolation';
import { StyleSheet, View } from 'react-native';

import { FlightMapGoogleAdapter } from './FlightMapGoogleAdapter';
import { FlightMapLibreAdapter } from './FlightMapLibreAdapter';
import type { FlightMapAdapterProps, FlightMapProvider } from './mapTypes';

export interface FlightMapViewProps {
  /** Origin airport */
  origin: Airport;
  /** Destination airport */
  destination: Airport;
  /** Current position along route */
  currentPosition: Position;
  /** Current heading in degrees */
  heading: number;
  /** Whether flight has been diverted */
  isDiverted?: boolean;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
});

/**
 * FlightMapView component
 * 
 * Interactive map showing flight route with real-time position updates.
 * 
 * @example
 * ```tsx
 * <FlightMapView
 *   origin={booking.origin}
 *   destination={booking.destination}
 *   currentPosition={progress.currentPosition}
 *   heading={progress.heading}
 * />
 * ```
 */
const ACTIVE_MAP_PROVIDER: FlightMapProvider = 'google';

export function FlightMapView({
  origin,
  destination,
  currentPosition,
  heading,
  isDiverted = false,
}: FlightMapViewProps) {
  const colorScheme = useColorScheme();
  const adapterProps: FlightMapAdapterProps = {
    currentPosition,
    destination,
    heading,
    isDiverted,
    origin,
    theme: colorScheme === 'dark' ? 'dark' : 'light',
  };

  return (
    <View style={styles.container}>
      {ACTIVE_MAP_PROVIDER === 'maplibre' ? (
        <FlightMapLibreAdapter {...adapterProps} />
      ) : (
        <FlightMapGoogleAdapter {...adapterProps} />
      )}
    </View>
  );
}
