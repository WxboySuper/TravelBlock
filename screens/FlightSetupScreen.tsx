import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { TimeSlider } from '@/components/time-slider/TimeSlider';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useHomeAirport } from '@/hooks/useHomeAirport';
import type { Airport } from '@/types/airport';
import { formatFlightWindowLabel } from '@/utils/flightWindow';
import { formatTimeValue } from '@/utils/timeSlider';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

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
    paddingBottom: Spacing.md,
  },
  title: {
    fontSize: Typography.fontSize.xxl,
    fontWeight: Typography.fontWeight.bold,
  },
  closeButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  body: {
    flex: 1,
    paddingHorizontal: Spacing.lg,
    gap: Spacing.md,
  },
  card: {
    borderRadius: BorderRadius.lg,
    borderWidth: 1,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.md,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    marginBottom: Spacing.sm,
    gap: Spacing.sm,
  },
  label: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  helperText: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
  },
  codeRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: Spacing.sm,
  },
  airportCode: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
  },
  iataCode: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.semibold,
  },
  airportName: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.medium,
    marginTop: Spacing.xs,
  },
  airportMeta: {
    fontSize: Typography.fontSize.sm,
    marginTop: 2,
  },
  windowValueRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.sm,
    gap: Spacing.sm,
  },
  windowValue: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
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
  footer: {
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.md,
    paddingBottom: Spacing.xl,
  },
});

export default function FlightSetupScreen() {
  const { homeAirport } = useHomeAirport();
  const { origin, flightDuration, setOrigin, setDestination, setFlightDuration } = useFlight();
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const [localFlightTime, setLocalFlightTime] = useState(flightDuration);

  useEffect(() => {
    if (homeAirport && (!origin || origin.icao !== homeAirport.icao)) {
      setOrigin(homeAirport).catch(() => undefined);
    }
  }, [homeAirport, origin, setOrigin]);

  const bucketLabel = useMemo(() => formatFlightWindowLabel(localFlightTime), [localFlightTime]);
  const formattedDuration = useMemo(
    () => formatTimeValue(localFlightTime).formatted,
    [localFlightTime]
  );

  const handleFlightTimeChange = useCallback(
    (time: number) => {
      setLocalFlightTime(time);
      setFlightDuration(time).catch(() => undefined);
      setDestination(null).catch(() => undefined);
    },
    [setDestination, setFlightDuration]
  );

  const handleContinue = useCallback(async () => {
    if (!homeAirport) {
      return;
    }

    await Promise.all([
      setOrigin(homeAirport),
      setFlightDuration(localFlightTime),
      setDestination(null),
    ]);

    await impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);
    router.push('/flight/destination');
  }, [homeAirport, localFlightTime, router, setDestination, setFlightDuration, setOrigin]);

  const handleClose = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
    router.back();
  }, [router]);

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.header}>
          <ThemedText style={styles.title}>Flight Setup</ThemedText>
          <TouchableOpacity
            onPress={handleClose}
            style={[styles.closeButton, { backgroundColor: colors.cardBackground }]}
            accessibilityLabel="Close flight setup"
            accessibilityRole="button"
            testID="close-flight-setup"
          >
            <IconSymbol name="xmark" size={20} color={colors.text} />
          </TouchableOpacity>
        </View>

        <View style={styles.body}>
          <DepartureCard airport={homeAirport} />
          <FlightWindowCard
            flightTime={localFlightTime}
            flightTimeLabel={formattedDuration}
            bucketLabel={bucketLabel}
            onFlightTimeChange={handleFlightTimeChange}
          />
        </View>

        <View style={styles.footer}>
          <Button
            title="Continue"
            onPress={handleContinue}
            disabled={!homeAirport}
            testID="continue-to-destinations"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}

function DepartureCard({ airport }: { airport: Airport | null }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  if (!airport) {
    return (
      <View
        style={[
          styles.card,
          { backgroundColor: colors.cardBackground, borderColor: colors.border },
        ]}
      >
        <ThemedText style={[styles.label, { color: colors.textSecondary }]}>
          Departure
        </ThemedText>
        <ThemedText>No home base selected</ThemedText>
      </View>
    );
  }

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.cardBackground, borderColor: colors.border },
      ]}
    >
      <View style={styles.cardHeader}>
        <ThemedText style={[styles.label, { color: colors.textSecondary }]}>
          Departure
        </ThemedText>
        <ThemedText style={[styles.helperText, { color: colors.textSecondary }]}>
          {airport.city}
        </ThemedText>
      </View>

      <View style={styles.codeRow}>
        <ThemedText style={styles.airportCode}>{airport.icao}</ThemedText>
        {airport.iata ? (
          <ThemedText style={[styles.iataCode, { color: colors.primary }]}>
            {airport.iata}
          </ThemedText>
        ) : null}
      </View>

      <ThemedText style={styles.airportName}>{airport.name}</ThemedText>
      <ThemedText style={[styles.airportMeta, { color: colors.textSecondary }]}>
        {airport.state ? `${airport.city}, ${airport.state}` : airport.city}
      </ThemedText>
    </View>
  );
}

function FlightWindowCard({
  flightTime,
  flightTimeLabel,
  bucketLabel,
  onFlightTimeChange,
}: {
  flightTime: number;
  flightTimeLabel: string;
  bucketLabel: string;
  onFlightTimeChange: (time: number) => void;
}) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.cardBackground, borderColor: colors.border },
      ]}
    >
      <View style={styles.cardHeader}>
        <ThemedText style={[styles.label, { color: colors.textSecondary }]}>
          Flight Window
        </ThemedText>
        <View style={[styles.bucketPill, { backgroundColor: colors.surfaceElevated }]}>
          <ThemedText style={[styles.bucketText, { color: colors.primary }]}>
            {bucketLabel}
          </ThemedText>
        </View>
      </View>

      <View style={styles.windowValueRow}>
        <ThemedText style={styles.windowValue}>{flightTimeLabel}</ThemedText>
        <ThemedText style={[styles.helperText, { color: colors.textSecondary }]}>
          Current duration
        </ThemedText>
      </View>

      <TimeSlider value={flightTime} onValueChange={onFlightTimeChange} />
    </View>
  );
}
