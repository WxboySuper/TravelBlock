import { AppIcon } from "@/components/ui/AppIcon";
import { ThemedText } from "@/components/themed-text";
import { BorderRadius, Colors, Spacing, Typography } from "@/constants/theme";
import { generateRouteWaypoints } from "@/utils/flightInterpolation";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Platform, StyleSheet, Text, UIManager, View } from "react-native";
import MapView, { Polyline, PROVIDER_GOOGLE } from "react-native-maps";

import type { FlightMapAdapterProps } from "./mapTypes";

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  markerOverlay: {
    ...StyleSheet.absoluteFillObject,
  },
  pin: {
    alignItems: "center",
    borderRadius: BorderRadius.full,
    borderWidth: 1,
    height: 28,
    justifyContent: "center",
    minWidth: 54,
    paddingHorizontal: 10,
    position: "absolute",
  },
  pinText: {
    color: "#FFFFFF",
    fontSize: 11,
    fontWeight: Typography.fontWeight.bold as any,
    letterSpacing: 0.2,
  },
  planeMarker: {
    alignItems: "center",
    height: 32,
    justifyContent: "center",
    backgroundColor: "transparent",
    position: "absolute",
    width: 32,
  },
  overlayCard: {
    position: "absolute",
    left: Spacing.md,
    right: Spacing.md,
    top: Spacing.md,
    alignItems: "flex-start",
  },
  routePill: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: BorderRadius.full,
    borderWidth: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  routeText: {
    color: "#FFFFFF",
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as any,
    letterSpacing: 0.3,
  },
  fallbackShell: {
    flex: 1,
    padding: Spacing.md,
    gap: Spacing.md,
  },
  fallbackCard: {
    borderRadius: BorderRadius.xl,
    borderWidth: 1,
    padding: Spacing.lg,
    gap: Spacing.md,
  },
  fallbackHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: Spacing.sm,
  },
  fallbackTitle: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold as any,
  },
  fallbackBody: {
    fontSize: Typography.fontSize.sm,
    lineHeight: 22,
  },
  fallbackHint: {
    fontSize: Typography.fontSize.xs,
    lineHeight: 18,
  },
  routeSummary: {
    borderRadius: BorderRadius.lg,
    padding: Spacing.md,
    gap: Spacing.sm,
  },
  routeRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: Spacing.sm,
  },
  routeCode: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as any,
  },
  routeMeta: {
    fontSize: Typography.fontSize.sm,
  },
  routeSide: {
    flex: 1,
  },
  routeSideRight: {
    flex: 1,
    alignItems: "flex-end",
  },
});

function hasNativeMapView(): boolean {
  try {
    const airMapConfig = UIManager.getViewManagerConfig?.("AIRMap");
    if (airMapConfig) {
      return true;
    }

    if (Platform.OS === "android") {
      return !!UIManager.getViewManagerConfig?.("AIRGoogleMap");
    }

    return false;
  } catch {
    return false;
  }
}

function AirportPin({
  code,
  color,
  point,
}: {
  code: string;
  color: string;
  point: { x: number; y: number };
}) {
  return (
    <View
      pointerEvents="none"
      style={[
        styles.pin,
        {
          backgroundColor: color,
          borderColor: "rgba(255,255,255,0.18)",
          left: point.x - AIRPORT_PIN_WIDTH / 2,
          top: point.y - AIRPORT_PIN_HEIGHT / 2,
        },
      ]}
    >
      <Text allowFontScaling={false} style={styles.pinText}>
        {code}
      </Text>
    </View>
  );
}

const AIRPORT_PIN_WIDTH = 54;
const AIRPORT_PIN_HEIGHT = 28;
const PLANE_MARKER_SIZE = 32;

export function FlightMapGoogleAdapter({
  currentPosition,
  destination,
  heading,
  isDiverted = false,
  origin,
  theme,
}: FlightMapAdapterProps) {
  const mapRef = useRef<MapView>(null);
  const lastAutoFitRouteKeyRef = useRef<string | null>(null);
  const [isMapReady, setIsMapReady] = useState(false);
  const [overlayPoints, setOverlayPoints] = useState<{
    destination?: { x: number; y: number };
    origin?: { x: number; y: number };
    plane?: { x: number; y: number };
  }>({});
  const colors = Colors[theme];

  const routeWaypoints = useMemo(() => generateRouteWaypoints(origin, destination, 100), [destination, origin]);
  const routeCoordinates = useMemo(
    () =>
      routeWaypoints.map((waypoint) => ({
        latitude: waypoint.lat,
        longitude: waypoint.lon,
      })),
    [routeWaypoints]
  );
  const routeKey = `${origin.icao}-${destination.icao}-${isDiverted ? "diverted" : "direct"}`;
  const showOriginMarker = true;
  const showDestinationMarker = true;
  const fittedCoordinates = useMemo(
    () => [
      ...routeCoordinates,
      { latitude: currentPosition.lat, longitude: currentPosition.lon },
      { latitude: origin.lat, longitude: origin.lon },
      { latitude: destination.lat, longitude: destination.lon },
    ],
    [
      currentPosition.lat,
      currentPosition.lon,
      destination.lat,
      destination.lon,
      origin.lat,
      origin.lon,
      routeCoordinates,
    ]
  );

  const fitRoute = useCallback(() => {
    if (!mapRef.current || !isMapReady || fittedCoordinates.length === 0) {
      return;
    }

    setTimeout(() => {
      mapRef.current?.fitToCoordinates(fittedCoordinates, {
        edgePadding: { top: 180, right: 112, bottom: 180, left: 112 },
        animated: true,
      });
    }, 150);
  }, [fittedCoordinates, isMapReady]);

  const refreshOverlayPoints = useCallback(async () => {
    if (!mapRef.current || !isMapReady) {
      return;
    }

    try {
      const coordinates = await Promise.all([
        mapRef.current.pointForCoordinate({
          latitude: origin.lat,
          longitude: origin.lon,
        }),
        mapRef.current.pointForCoordinate({
          latitude: destination.lat,
          longitude: destination.lon,
        }),
        mapRef.current.pointForCoordinate({
          latitude: currentPosition.lat,
          longitude: currentPosition.lon,
        }),
      ]);

      setOverlayPoints({
        origin: coordinates[0],
        destination: coordinates[1],
        plane: coordinates[2],
      });
    } catch {
      setOverlayPoints({});
    }
  }, [
    currentPosition.lat,
    currentPosition.lon,
    destination.lat,
    destination.lon,
    isMapReady,
    origin.lat,
    origin.lon,
  ]);

  useEffect(() => {
    if (!isMapReady) {
      return;
    }

    if (lastAutoFitRouteKeyRef.current === routeKey) {
      return;
    }

    lastAutoFitRouteKeyRef.current = routeKey;
    fitRoute();
  }, [fitRoute, isMapReady, routeKey]);

  useEffect(() => {
    refreshOverlayPoints();
  }, [refreshOverlayPoints]);

  const accentColor = isDiverted ? colors.cockpitWarning : colors.cockpitAccent;
  const mapAvailable = hasNativeMapView();

  if (!mapAvailable) {
    return (
      <View style={[styles.fallbackShell, { backgroundColor: colors.cockpitBackground }]}>
        <View style={[styles.fallbackCard, { backgroundColor: colors.cockpitSurface, borderColor: colors.cockpitBorder }]}>
          <View style={styles.fallbackHeader}>
            <AppIcon color={colors.cockpitWarning} name="warning" size={20} />
            <ThemedText style={[styles.fallbackTitle, { color: colors.text }]}>Map offline in this build</ThemedText>
          </View>
          <ThemedText style={[styles.fallbackBody, { color: colors.cockpitTextSecondary }]}>
            The route view is ready, but this app build does not include the native map renderer.
          </ThemedText>
          <View style={[styles.routeSummary, { backgroundColor: colors.cockpitSurfaceMuted }]}>
            <View style={styles.routeRow}>
              <View style={styles.routeSide}>
                <ThemedText style={[styles.routeCode, { color: colors.text }]}>{origin.iata}</ThemedText>
                <ThemedText style={[styles.routeMeta, { color: colors.cockpitTextSecondary }]}>{origin.city}</ThemedText>
              </View>
              <AppIcon color={accentColor} name="aircraft" size={22} style={{ transform: [{ rotate: "90deg" }] }} />
              <View style={styles.routeSideRight}>
                <ThemedText style={[styles.routeCode, { color: colors.text }]}>{destination.iata}</ThemedText>
                <ThemedText style={[styles.routeMeta, { color: colors.cockpitTextSecondary }]}>{destination.city}</ThemedText>
              </View>
            </View>
            <ThemedText style={[styles.routeMeta, { color: colors.cockpitTextSecondary }]}>
              {currentPosition.lat.toFixed(2)}, {currentPosition.lon.toFixed(2)}
            </ThemedText>
          </View>
          <ThemedText style={[styles.fallbackHint, { color: colors.cockpitTextSecondary }]}>
            Install a build with `react-native-maps` or the upcoming MapLibre adapter to restore the live route view.
          </ThemedText>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <MapView
        ref={mapRef}
        mapType={theme === "dark" ? "hybrid" : "standard"}
        onMapReady={() => {
          setIsMapReady(true);
        }}
        onRegionChangeComplete={() => {
          refreshOverlayPoints();
        }}
        pitchEnabled={false}
        provider={PROVIDER_GOOGLE}
        rotateEnabled
        showsCompass={false}
        showsMyLocationButton={false}
        showsScale={false}
        showsTraffic={false}
        showsUserLocation={false}
        style={styles.map}
      >
        <Polyline
          coordinates={routeCoordinates}
          geodesic
          lineDashPattern={isDiverted ? [10, 6] : undefined}
          strokeColor={accentColor}
          strokeWidth={4}
        />
      </MapView>

      <View pointerEvents="none" style={styles.markerOverlay}>
        {showOriginMarker && overlayPoints.origin ? (
          <AirportPin code={origin.iata} color={colors.cockpitSuccess} point={overlayPoints.origin} />
        ) : null}

        {showDestinationMarker && overlayPoints.destination ? (
          <AirportPin
            code={destination.iata}
            color={isDiverted ? "#52627A" : colors.cockpitDanger}
            point={overlayPoints.destination}
          />
        ) : null}

        {overlayPoints.plane ? (
          <View
            pointerEvents="none"
            style={[
              styles.planeMarker,
              {
                left: overlayPoints.plane.x - PLANE_MARKER_SIZE / 2,
                top: overlayPoints.plane.y - PLANE_MARKER_SIZE / 2,
              },
            ]}
          >
            <AppIcon
              color="rgba(5, 12, 26, 0.9)"
              name="aircraft"
              size={30}
              style={{ transform: [{ rotate: `${heading}deg` }] }}
            />
            <AppIcon
              color="#FFFFFF"
              name="aircraft"
              size={22}
              style={{
                position: "absolute",
                transform: [{ rotate: `${heading}deg` }],
              }}
            />
          </View>
        ) : null}
      </View>

      <View pointerEvents="none" style={styles.overlayCard}>
        <View style={[styles.routePill, { backgroundColor: colors.cockpitGlass, borderColor: colors.cockpitBorder }]}>
          <AppIcon color="#FFFFFF" name="route" size={16} />
          <ThemedText style={styles.routeText}>
            {origin.iata} to {destination.iata}
          </ThemedText>
        </View>
      </View>
    </View>
  );
}
