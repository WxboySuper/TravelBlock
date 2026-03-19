import { DestinationsList } from '@/components/destinations/DestinationsList';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useDestinations } from '@/hooks/useDestinations';
import type { AirportWithFlightTime } from '@/types/radius';
import { formatFlightWindowLabel } from '@/utils/flightWindow';
import { TIME_SLIDER_CONFIG } from '@/utils/timeSlider';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { LayoutChangeEvent, StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

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
    paddingTop: Spacing.sm,
    paddingBottom: Spacing.sm,
  },
  headerButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitle: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold,
  },
  body: {
    flex: 1,
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.sm,
    gap: Spacing.md,
  },
  summaryCard: {
    borderRadius: BorderRadius.lg,
    borderWidth: 1,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.md,
    gap: Spacing.sm,
  },
  summaryTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.sm,
  },
  label: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  bucketPill: {
    borderRadius: BorderRadius.full,
    paddingHorizontal: Spacing.sm,
    paddingVertical: 6,
  },
  bucketText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  summaryCodeRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: Spacing.sm,
  },
  airportCode: {
    fontSize: Typography.fontSize.xxl,
    fontWeight: Typography.fontWeight.bold,
  },
  airportIata: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.semibold,
  },
  airportMeta: {
    fontSize: Typography.fontSize.sm,
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
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing.xl,
    gap: Spacing.sm,
  },
});

export default function FlightDestinationScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const insets = useSafeAreaInsets();
  const { origin, destination, flightDuration, setDestination } = useFlight();
  const [footerHeight, setFooterHeight] = useState(0);

  const bucketLabel = useMemo(() => formatFlightWindowLabel(flightDuration), [flightDuration]);
  const {
    destinations,
    isLoading,
    error,
  } = useDestinations({
    origin,
    flightTimeInSeconds: flightDuration,
    useTimeRange: false,
    bucketIntervalSeconds: TIME_SLIDER_CONFIG.SNAP_INTERVAL,
    initialBucketMaxTime: TIME_SLIDER_CONFIG.MIN_TIME,
  });

  const selectedDestinationWithTime = useMemo(() => {
    if (!destination) {
      return null;
    }

    return destinations.find((currentDestination) => currentDestination.icao === destination.icao) ?? null;
  }, [destination, destinations]);

  const footerInset = footerHeight + Spacing.xl;

  const handleBack = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
    router.back();
  }, [router]);

  const handleSelectDestination = useCallback(
    (airport: AirportWithFlightTime) => {
      setDestination(airport).catch(() => undefined);
    },
    [setDestination]
  );

  const handleReviewFlight = useCallback(() => {
    if (!selectedDestinationWithTime) {
      return;
    }

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);
    router.push('/flight/review');
  }, [router, selectedDestinationWithTime]);

  const handleFooterLayout = useCallback((event: LayoutChangeEvent) => {
    const nextHeight = event.nativeEvent.layout.height;
    setFooterHeight((currentHeight) => {
      return currentHeight === nextHeight ? currentHeight : nextHeight;
    });
  }, []);

  if (!origin) {
    return <MissingOriginState onBack={handleBack} colors={colors} />;
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <DestinationStepHeader onBack={handleBack} colors={colors} />

        <View style={styles.body}>
          <DestinationSummaryCard origin={origin} bucketLabel={bucketLabel} colors={colors} />

          <View
            style={[
              styles.destinationsShell,
              { backgroundColor: colors.cardBackground, borderColor: colors.border },
            ]}
          >
            <DestinationsList
              destinations={destinations}
              selectedDestination={selectedDestinationWithTime}
              onSelectDestination={handleSelectDestination}
              isLoading={isLoading}
              error={error}
              compactCards
              contentBottomInset={footerInset}
              testID="destination-list"
            />
          </View>
        </View>

        <DestinationReviewFooter
          onLayout={handleFooterLayout}
          onReview={handleReviewFlight}
          disabled={!selectedDestinationWithTime}
          bottomInset={Math.max(insets.bottom, Spacing.md)}
          backgroundColor={colors.background}
        />
      </SafeAreaView>
    </ThemedView>
  );
}

function DestinationStepHeader({
  onBack,
  colors,
}: {
  onBack: () => void;
  colors: typeof Colors.light;
}) {
  return (
    <View style={styles.header}>
      <TouchableOpacity
        onPress={onBack}
        style={[styles.headerButton, { backgroundColor: colors.cardBackground }]}
        accessibilityLabel="Back to flight setup"
        accessibilityRole="button"
        testID="back-to-flight-setup"
        hitSlop={{ top: 10, right: 10, bottom: 10, left: 10 }}
      >
        <IconSymbol name="chevron.left" size={20} color={colors.text} />
      </TouchableOpacity>
      <ThemedText style={styles.headerTitle}>Choose Destination</ThemedText>
      <View style={styles.headerButton} />
    </View>
  );
}

function DestinationSummaryCard({
  origin,
  bucketLabel,
  colors,
}: {
  origin: NonNullable<ReturnType<typeof useFlight>['origin']>;
  bucketLabel: string;
  colors: typeof Colors.light;
}) {
  return (
    <View
      style={[
        styles.summaryCard,
        { backgroundColor: colors.cardBackground, borderColor: colors.border },
      ]}
    >
      <View style={styles.summaryTopRow}>
        <ThemedText style={[styles.label, { color: colors.textSecondary }]}>
          Departing From
        </ThemedText>
        <View style={[styles.bucketPill, { backgroundColor: colors.surfaceElevated }]}>
          <ThemedText
            style={[styles.bucketText, { color: colors.primary }]}
            testID="destination-summary-bucket"
          >
            {bucketLabel}
          </ThemedText>
        </View>
      </View>

      <View style={styles.summaryCodeRow}>
        <ThemedText style={styles.airportCode} testID="destination-summary-airport">
          {origin.icao}
        </ThemedText>
        {origin.iata ? (
          <ThemedText style={[styles.airportIata, { color: colors.primary }]}>
            {origin.iata}
          </ThemedText>
        ) : null}
      </View>

      <ThemedText style={styles.airportMeta}>{origin.name}</ThemedText>
      <ThemedText style={[styles.airportMeta, { color: colors.textSecondary }]}>
        {origin.state ? `${origin.city}, ${origin.state}` : origin.city}
      </ThemedText>
    </View>
  );
}

function DestinationReviewFooter({
  onLayout,
  onReview,
  disabled,
  bottomInset,
  backgroundColor,
}: {
  onLayout: (event: LayoutChangeEvent) => void;
  onReview: () => void;
  disabled: boolean;
  bottomInset: number;
  backgroundColor: string;
}) {
  return (
    <View
      onLayout={onLayout}
      style={[
        styles.footer,
        {
          backgroundColor,
          paddingBottom: bottomInset,
        },
      ]}
    >
      <Button
        title="Review Flight"
        onPress={onReview}
        disabled={disabled}
        testID="review-flight-button"
      />
    </View>
  );
}

function MissingOriginState({
  onBack,
  colors,
}: {
  onBack: () => void;
  colors: typeof Colors.light;
}) {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <DestinationStepHeader onBack={onBack} colors={colors} />
        <View style={styles.emptyState}>
          <ThemedText type="subtitle">No departure selected</ThemedText>
          <ThemedText style={{ color: colors.textSecondary, textAlign: 'center' }}>
            Go back and choose your departure airport and flight window first.
          </ThemedText>
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
