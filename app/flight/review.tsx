/**
 * Flight Review Screen
 *
 * Allows user to review flight details before booking.
 * Shows departure/destination, duration, distance, and flight metadata.
 *
 * @module app/flight/review
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useMemo } from 'react';
import { ScrollView, StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { estimateFlightTime } from '@/services/radiusService';
import type { Airport } from '@/types/airport';
import { calculateDistance } from '@/utils/distance';
import { generateFlightBooking } from '@/utils/flightGenerator';
import { formatTimeValue } from '@/utils/timeSlider';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: Spacing.lg,
    paddingBottom: Spacing.md,
  },
  backButton: {
    padding: Spacing.sm,
    marginLeft: -Spacing.sm,
  },
  scrollContent: {
    flex: 1,
  },
  content: {
    padding: Spacing.lg,
    paddingBottom: 120,
  },
  section: {
    marginBottom: Spacing.xl,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.md,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  routeCard: {
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.md,
  },
  routeHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.lg,
  },
  airportInfo: {
    flex: 1,
  },
  airportCode: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    marginBottom: Spacing.xs,
  },
  airportName: {
    fontSize: Typography.fontSize.sm,
    marginBottom: 2,
  },
  airportLocation: {
    fontSize: Typography.fontSize.xs,
  },
  routeArrow: {
    paddingHorizontal: Spacing.md,
  },
  detailsCard: {
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.sm,
  },
  detailLabel: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.medium,
  },
  detailValue: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.semibold,
  },
  divider: {
    height: 1,
    marginVertical: Spacing.sm,
  },
  footer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
    borderTopWidth: 1,
  },
});

type ReviewDetailRowProps = {
  colors: typeof Colors.light;
  isLast?: boolean;
  label: string;
  value: string;
  valueStyle?: object;
};

type ReviewContentProps = {
  booking: NonNullable<ReturnType<typeof useReviewBooking>['booking']>;
  colors: typeof Colors.light;
  destination: Airport;
  distanceKm: number;
  distanceMiles: number;
  formattedRouteEstimate: { formatted: string };
  formattedTime: { formatted: string };
  origin: Airport;
};

function ReviewDetailRow({
  colors,
  isLast = false,
  label,
  value,
  valueStyle,
}: ReviewDetailRowProps) {
  return (
    <>
      <View style={styles.detailRow}>
        <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
          {label}
        </ThemedText>
        <ThemedText style={[styles.detailValue, valueStyle]}>
          {value}
        </ThemedText>
      </View>
      {!isLast ? <View style={[styles.divider, { backgroundColor: colors.border }]} /> : null}
    </>
  );
}

function RoutePreview({
  colors,
  destination,
  origin,
}: {
  colors: typeof Colors.light;
  destination: Airport;
  origin: Airport;
}) {
  return (
    <View style={[styles.routeCard, { backgroundColor: colors.cardBackground }]}>
      <View style={styles.routeHeader}>
        <View style={styles.airportInfo}>
          <ThemedText style={[styles.airportCode, { color: colors.primary }]}>
            {origin.iata || origin.icao}
          </ThemedText>
          <ThemedText style={styles.airportName}>
            {origin.name}
          </ThemedText>
          <ThemedText style={[styles.airportLocation, { color: colors.textSecondary }]}>
            {origin.city}, {origin.country}
          </ThemedText>
        </View>

        <View style={styles.routeArrow}>
          <IconSymbol name="arrow.right" size={28} color={colors.textSecondary} />
        </View>

        <View style={[styles.airportInfo, { alignItems: 'flex-end' }]}>
          <ThemedText style={[styles.airportCode, { color: colors.primary }]}>
            {destination.iata || destination.icao}
          </ThemedText>
          <ThemedText style={[styles.airportName, { textAlign: 'right' }]}>
            {destination.name}
          </ThemedText>
          <ThemedText style={[styles.airportLocation, { color: colors.textSecondary, textAlign: 'right' }]}>
            {destination.city}, {destination.country}
          </ThemedText>
        </View>
      </View>
    </View>
  );
}

function useReviewBooking(
  origin: Airport | null,
  destination: Airport | null,
  flightDuration: number
) {
  const routeEstimateSeconds = useMemo(() => {
    if (!origin || !destination) return 0;
    const distanceMiles = calculateDistance(origin, destination);
    return estimateFlightTime({ distanceInMiles: distanceMiles });
  }, [origin, destination]);

  const booking = useMemo(() => {
    if (!origin || !destination) return null;
    return generateFlightBooking(origin, destination, flightDuration);
  }, [flightDuration, origin, destination]);

  return {
    booking,
    routeEstimateSeconds,
  };
}

function ReviewHeader({
  colors,
  handleBack,
  title,
}: {
  colors: typeof Colors.light;
  handleBack: () => void;
  title: string;
}) {
  return (
    <View style={styles.header}>
      <TouchableOpacity
        onPress={handleBack}
        style={styles.backButton}
        accessibilityLabel="Go back"
        accessibilityRole="button"
      >
        <IconSymbol name="chevron.left" size={24} color={colors.text} />
      </TouchableOpacity>
      <ThemedText type="title">{title}</ThemedText>
      <View style={{ width: 40 }} />
    </View>
  );
}

function ReviewSection({
  children,
  colors,
  title,
}: {
  children: React.ReactNode;
  colors: typeof Colors.light;
  title: string;
}) {
  return (
    <View style={styles.section}>
      <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
        {title}
      </ThemedText>
      {children}
    </View>
  );
}

function ReviewContent({
  booking,
  colors,
  destination,
  distanceKm,
  distanceMiles,
  formattedRouteEstimate,
  formattedTime,
  origin,
}: ReviewContentProps) {
  return (
    <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
      <View style={styles.content}>
        <ReviewSection colors={colors} title="Your Route">
          <RoutePreview colors={colors} destination={destination} origin={origin} />
        </ReviewSection>

        <ReviewSection colors={colors} title="Flight Details">
          <View style={[styles.detailsCard, { backgroundColor: colors.cardBackground }]}>
            <ReviewDetailRow
              colors={colors}
              label="Flight Number"
              value={booking.flightNumber}
              valueStyle={{ fontFamily: 'monospace' }}
            />
            <ReviewDetailRow
              colors={colors}
              label="Duration"
              value={formattedTime.formatted}
            />
            <ReviewDetailRow
              colors={colors}
              label="Route ETA"
              value={formattedRouteEstimate.formatted}
            />
            <ReviewDetailRow
              colors={colors}
              label="Distance"
              value={`${distanceMiles} mi (${Math.round(distanceKm)} km)`}
            />
            <ReviewDetailRow
              colors={colors}
              label="Aircraft"
              value={booking.aircraft.name}
            />
            <ReviewDetailRow
              colors={colors}
              isLast
              label="Gate"
              value={booking.gate}
            />
          </View>
        </ReviewSection>
      </View>
    </ScrollView>
  );
}

function ReviewScaffold({
  children,
  colors,
  footer,
  handleBack,
  title,
}: {
  children: React.ReactNode;
  colors: typeof Colors.light;
  footer?: React.ReactNode;
  handleBack: () => void;
  title: string;
}) {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <ReviewHeader colors={colors} handleBack={handleBack} title={title} />
        {children}
        {footer}
      </SafeAreaView>
    </ThemedView>
  );
}

function renderEmptyState(colors: typeof Colors.light, handleBack: () => void) {
  return (
    <ReviewScaffold colors={colors} handleBack={handleBack} title="Flight Review">
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ThemedText>No flight data available</ThemedText>
      </View>
    </ReviewScaffold>
  );
}

export default function FlightReviewScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { origin, destination, flightDuration, setBooking } = useFlight();

  const { booking, routeEstimateSeconds } = useReviewBooking(origin, destination, flightDuration);

  const formattedTime = formatTimeValue(booking?.durationSeconds ?? flightDuration);
  const formattedRouteEstimate = formatTimeValue(routeEstimateSeconds);
  const distanceKm = booking?.distanceKm ?? 0;
  const distanceMiles = Math.round(distanceKm * 0.621371);

  const handleBack = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {});
    router.back();
  }, [router]);

  const handleBookFlight = useCallback(async () => {
    if (!booking) return;

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});
    await setBooking(booking);
    router.push('/flight/seat-selection');
  }, [booking, setBooking, router]);

  if (!origin || !destination || !booking) {
    return renderEmptyState(colors, handleBack);
  }

  return (
    <ReviewScaffold
      colors={colors}
      handleBack={handleBack}
      title="Review Flight"
      footer={(
        <View style={[styles.footer, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
          <Button
            title="Book Flight"
            onPress={handleBookFlight}
            size="lg"
          />
        </View>
      )}
    >
      <ReviewContent
        booking={booking}
        colors={colors}
        destination={destination}
        distanceKm={distanceKm}
        distanceMiles={distanceMiles}
        formattedRouteEstimate={formattedRouteEstimate}
        formattedTime={formattedTime}
        origin={origin}
      />
    </ReviewScaffold>
  );
}
