/**
 * FlightMapView component - interactive map showing flight route
 * 
 * Displays origin, destination, current position, and route polyline
 * on a MapView. Updates in real-time as flight progresses.
 * 
 * @module components/cockpit/FlightMapView
 */

import { ThemedText } from '@/components/themed-text';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { Airport } from '@/types/airport';
import type { Position } from '@/utils/flightInterpolation';
import { generateRouteWaypoints } from '@/utils/flightInterpolation';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import MapView, { Marker, Polyline, PROVIDER_GOOGLE } from 'react-native-maps';

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
export function FlightMapView({
  origin,
  destination,
  currentPosition,
  heading,
  isDiverted = false,
}: FlightMapViewProps) {
  const mapRef = useRef<MapView>(null);
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const [isMapReady, setIsMapReady] = useState(false);

  const routeWaypoints = useMemo(() => {
    return generateRouteWaypoints(origin, destination, 100);
  }, [destination, origin]);

  const routeCoordinates = useMemo(() => {
    return routeWaypoints.map((waypoint) => ({
      latitude: waypoint.lat,
      longitude: waypoint.lon,
    }));
  }, [routeWaypoints]);

  const fitRoute = useCallback(() => {
    if (!mapRef.current || !isMapReady || routeCoordinates.length === 0) {
      return;
    }

    setTimeout(() => {
      mapRef.current?.fitToCoordinates(routeCoordinates, {
        edgePadding: { top: 120, right: 56, bottom: 120, left: 56 },
        animated: true,
      });
    }, 150);
  }, [isMapReady, routeCoordinates]);

  useEffect(() => {
    fitRoute();
  }, [fitRoute]);

  return (
    <View style={styles.container}>
      <MapView
        ref={mapRef}
        style={styles.map}
        provider={PROVIDER_GOOGLE}
        mapType={isDark ? 'hybrid' : 'standard'}
        showsUserLocation={false}
        showsMyLocationButton={false}
        showsCompass={true}
        showsScale={true}
        rotateEnabled={true}
        pitchEnabled={false}
        onMapReady={() => setIsMapReady(true)}
      >
        {/* Route polyline */}
        <Polyline
          coordinates={routeWaypoints.map(wp => ({
            latitude: wp.lat,
            longitude: wp.lon,
          }))}
          strokeColor={isDiverted ? '#F59E0B' : '#3B82F6'}
          strokeWidth={3}
          lineDashPattern={isDiverted ? [10, 5] : undefined}
        />

        {/* Origin marker (green) */}
        <Marker
          coordinate={{
            latitude: origin.lat,
            longitude: origin.lon,
          }}
          title={origin.iata}
          description={origin.name}
          pinColor="#10B981"
        />

        {/* Destination marker (red if not diverted, gray if diverted) */}
        <Marker
          coordinate={{
            latitude: destination.lat,
            longitude: destination.lon,
          }}
          title={destination.iata}
          description={destination.name}
          pinColor={isDiverted ? '#6B7280' : '#EF4444'}
        />

        {/* Current position marker (blue plane) */}
        <Marker
          coordinate={{
            latitude: currentPosition.lat,
            longitude: currentPosition.lon,
          }}
          title="Current Position"
          anchor={{ x: 0.5, y: 0.5 }}
          rotation={heading}
          flat
        >
          <View
            style={{
              width: 40,
              height: 40,
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <ThemedText style={{ fontSize: 32 }}>✈️</ThemedText>
          </View>
        </Marker>
      </MapView>
    </View>
  );
}
