import { DestinationsList } from '@/components/destinations/DestinationsList';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { TimeSlider } from '@/components/time-slider/TimeSlider';
import { TimeValue } from '@/components/time-slider/TimeValue';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useDestinations } from '@/hooks/useDestinations';
import { useHomeAirport } from '@/hooks/useHomeAirport';
import { getFlightTimeBucket } from '@/services/radiusService';
import type { Airport } from '@/types/airport';
import { AirportWithFlightTime } from '@/types/radius';
import { formatTimeValue, TIME_SLIDER_CONFIG } from '@/utils/timeSlider';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

const FOOTER_HEIGHT = 94;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.xs,
    paddingBottom: Spacing.md,
  },
  closeButton: {
    width: 42,
    height: 42,
    borderRadius: 21,
    alignItems: 'center',
    justifyContent: 'center',
  },
  body: {
    flex: 1,
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.sm,
    gap: Spacing.md,
  },
  topStack: {
    gap: Spacing.md,
  },
  panel: {
    borderRadius: BorderRadius.lg,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderWidth: 1,
  },
  panelHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.sm,
  },
  panelLabel: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  originCodeRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: Spacing.sm,
  },
  originIata: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold,
  },
  originName: {
    fontSize: Typography.fontSize.sm,
    marginTop: 4,
  },
  originMeta: {
    fontSize: Typography.fontSize.sm,
    marginTop: Spacing.xs,
  },
  sliderPanelTop: {
    marginBottom: Spacing.sm,
  },
  sliderValueRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  bucketPill: {
    alignSelf: 'flex-start',
    borderRadius: 999,
    paddingHorizontal: Spacing.sm,
    paddingVertical: 6,
  },
  bucketPillText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  destinationsShell: {
    flex: 1,
    minHeight: 0,
    borderRadius: BorderRadius.lg,
    overflow: 'hidden',
    borderWidth: 1,
  },
  footer: {
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.sm,
    shadowColor: '#0f172a',
    shadowOffset: { width: 0, height: -6 },
    shadowOpacity: 0.08,
    shadowRadius: 16,
    elevation: 10,
  },
});

function FlightSetupHeader({ onClose }: { onClose: () => void }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.header}>
      <View>
        <ThemedText type="title">Flight Setup</ThemedText>
        <ThemedText style={{ color: colors.textSecondary }}>
          Pick a flight window, then choose a destination.
        </ThemedText>
      </View>
      <TouchableOpacity
        onPress={onClose}
        style={[styles.closeButton, { backgroundColor: colors.cardBackground }]}
        accessibilityLabel="Close flight setup"
        accessibilityRole="button"
        hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
      >
        <IconSymbol name="xmark" size={20} color={colors.text} />
      </TouchableOpacity>
    </View>
  );
}

function OriginSummaryCard({ airport }: { airport: Airport | null }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  if (!airport) {
    return (
      <View
        style={[
          styles.panel,
          {
            backgroundColor: colors.cardBackground,
            borderColor: colors.border,
          },
        ]}
      >
        <ThemedText style={[styles.panelLabel, { color: colors.textSecondary }]}>
          Departure
        </ThemedText>
        <ThemedText>No home base selected</ThemedText>
      </View>
    );
  }

  return (
    <View
      style={[
        styles.panel,
        {
          backgroundColor: colors.cardBackground,
          borderColor: colors.border,
        },
      ]}
    >
      <View style={styles.panelHeader}>
        <ThemedText style={[styles.panelLabel, { color: colors.textSecondary }]}>
          Departure
        </ThemedText>
        <ThemedText style={[styles.originIata, { color: colors.textSecondary }]}>
          {airport.city}
        </ThemedText>
      </View>
      <View style={styles.originCodeRow}>
        <ThemedText type="title">{airport.icao}</ThemedText>
        {!!airport.iata && (
          <ThemedText style={[styles.originIata, { color: colors.primary }]}>
            {airport.iata}
          </ThemedText>
        )}
      </View>
      <ThemedText style={styles.originName}>{airport.name}</ThemedText>
      <ThemedText style={[styles.originMeta, { color: colors.textSecondary }]}>
        {airport.state ? `${airport.city}, ${airport.state}` : airport.city}
      </ThemedText>
    </View>
  );
}

function FlightTimeCard({
  flightTime,
  onFlightTimeChange,
  bucketLabel,
}: {
  flightTime: number;
  onFlightTimeChange: (time: number) => void;
  bucketLabel: string;
}) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View
      style={[
        styles.panel,
        {
          backgroundColor: colors.cardBackground,
          borderColor: colors.border,
        },
      ]}
    >
      <View style={styles.sliderPanelTop}>
        <View style={styles.sliderValueRow}>
          <ThemedText style={[styles.panelLabel, { color: colors.textSecondary }]}>
            Flight Window
          </ThemedText>
          <View style={[styles.bucketPill, { backgroundColor: colors.surfaceElevated }]}>
            <ThemedText style={[styles.bucketPillText, { color: colors.primary }]}>
              {bucketLabel}
            </ThemedText>
          </View>
        </View>
        <TimeValue seconds={flightTime} />
      </View>
      <TimeSlider value={flightTime} onValueChange={onFlightTimeChange} />
    </View>
  );
}

function FlightSetupFooter({
  onStart,
  isDisabled,
}: {
  onStart: () => void;
  isDisabled: boolean;
}) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const insets = useSafeAreaInsets();

  return (
    <View
      style={[
        styles.footer,
        {
          backgroundColor: colors.background,
          paddingBottom: Math.max(insets.bottom, Spacing.md),
        },
      ]}
    >
      <Button
        title="Review Flight"
        onPress={onStart}
        size="md"
        disabled={isDisabled}
        testID="review-flight-button"
      />
    </View>
  );
}

function formatBucketLabel(flightTime: number): string {
  const bucket = getFlightTimeBucket(
    flightTime,
    TIME_SLIDER_CONFIG.SNAP_INTERVAL,
    TIME_SLIDER_CONFIG.MIN_TIME
  );
  const maxLabel = formatTimeValue(bucket.maxTimeInSeconds).formatted;

  if (bucket.minTimeInSeconds === 0) {
    return `Up to ${maxLabel}`;
  }

  const minLabel = formatTimeValue(bucket.minTimeInSeconds).formatted;
  return `${minLabel} - ${maxLabel}`;
}

export default function FlightSetupScreen() {
  const { homeAirport } = useHomeAirport();
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const insets = useSafeAreaInsets();
  const {
    origin,
    destination,
    flightDuration,
    setOrigin,
    setDestination,
    setFlightDuration,
  } = useFlight();

  const [localFlightTime, setLocalFlightTime] = useState(flightDuration);

  useEffect(() => {
    if (homeAirport && (!origin || origin.icao !== homeAirport.icao)) {
      setOrigin(homeAirport);
    }
  }, [homeAirport, origin, setOrigin]);

  const {
    destinations,
    isLoading: isLoadingDestinations,
    error: destinationsError,
  } = useDestinations({
    origin: homeAirport,
    flightTimeInSeconds: localFlightTime,
    useTimeRange: false,
    bucketIntervalSeconds: TIME_SLIDER_CONFIG.SNAP_INTERVAL,
    initialBucketMaxTime: TIME_SLIDER_CONFIG.MIN_TIME,
  });

  const selectedDestinationWithTime = destination
    ? destinations.find((currentDestination) => currentDestination.icao === destination.icao) || null
    : null;

  const bucketLabel = useMemo(() => formatBucketLabel(localFlightTime), [localFlightTime]);
  const footerInset = FOOTER_HEIGHT + insets.bottom + Spacing.xl;

  const handleFlightTimeChange = useCallback((time: number) => {
    setLocalFlightTime(time);
    setFlightDuration(time);
  }, [setFlightDuration]);

  const handleSelectDestination = useCallback(
    (airport: AirportWithFlightTime) => {
      setDestination(airport);
    },
    [setDestination]
  );

  const handleReviewFlight = useCallback(() => {
    if (!destination) return;

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);
    router.push('/flight/review');
  }, [destination, router]);

  const handleClose = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
    router.back();
  }, [router]);

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <FlightSetupHeader onClose={handleClose} />
        <View style={styles.body}>
          <View style={styles.topStack}>
            <OriginSummaryCard airport={homeAirport} />
            <FlightTimeCard
              flightTime={localFlightTime}
              onFlightTimeChange={handleFlightTimeChange}
              bucketLabel={bucketLabel}
            />
          </View>

          <View
            style={[
              styles.destinationsShell,
              {
                backgroundColor: colors.cardBackground,
                borderColor: colors.border,
              },
            ]}
          >
            <DestinationsList
              destinations={destinations}
              selectedDestination={selectedDestinationWithTime}
              onSelectDestination={handleSelectDestination}
              isLoading={isLoadingDestinations}
              error={destinationsError}
              compactCards={true}
              contentBottomInset={footerInset}
              headerSubtitle={bucketLabel}
              testID="destinations-list"
            />
          </View>
        </View>

        <FlightSetupFooter
          onStart={handleReviewFlight}
          isDisabled={!destination || !homeAirport}
        />
      </SafeAreaView>
    </ThemedView>
  );
}
