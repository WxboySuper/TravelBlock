import { AirportCard } from '@/components/airport/AirportCard';
import { DestinationsList } from '@/components/destinations/DestinationsList';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { TimeSlider } from '@/components/time-slider/TimeSlider';
import { TimeValue } from '@/components/time-slider/TimeValue';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useDestinations } from '@/hooks/useDestinations';
import { useHomeAirport } from '@/hooks/useHomeAirport';
import type { Airport } from '@/types/airport';
import { AirportWithFlightTime } from '@/types/radius';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.lg,
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.md,
  },
  closeButton: {
    padding: Spacing.sm,
  },
  section: {
    marginBottom: Spacing.lg,
    paddingHorizontal: Spacing.lg,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: Spacing.sm,
    opacity: 0.7,
  },
  timeSliderSection: {
    marginBottom: Spacing.xl,
    paddingHorizontal: Spacing.lg,
  },
  destinationsSection: {
    flex: 1,
    minHeight: 300,
  },
  footer: {
    padding: Spacing.lg,
    borderTopWidth: 1,
  },
});

function SectionHeader({ title }: { title: string }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
      {title}
    </ThemedText>
  );
}

function DepartureSection({ homeAirport }: { homeAirport: Airport | null }) {
  return (
    <View style={styles.section}>
      <SectionHeader title="Departure (Origin)" />
      {homeAirport ? (
        <AirportCard airport={homeAirport} showEdit={false} />
      ) : (
        <ThemedText>No Home Base Selected</ThemedText>
      )}
    </View>
  );
}

function TimeSliderSection({
  flightTime,
  onFlightTimeChange,
}: {
  flightTime: number;
  onFlightTimeChange: (time: number) => void;
}) {
  return (
    <View style={styles.timeSliderSection}>
      <SectionHeader title="Flight Duration" />
      <TimeValue seconds={flightTime} />
      <TimeSlider value={flightTime} onValueChange={onFlightTimeChange} />
    </View>
  );
}

function DestinationsSection({
  destinations,
  selectedDestination,
  onSelectDestination,
  isLoading,
  error,
}: {
  destinations: AirportWithFlightTime[];
  selectedDestination: AirportWithFlightTime | null;
  onSelectDestination: (airport: AirportWithFlightTime) => void;
  isLoading: boolean;
  error: string | null;
}) {
  return (
    <View style={styles.destinationsSection}>
      <View style={{ paddingHorizontal: Spacing.lg, marginBottom: Spacing.sm }}>
        <SectionHeader title="Available Destinations" />
      </View>
      <DestinationsList
        destinations={destinations}
        selectedDestination={selectedDestination}
        onSelectDestination={onSelectDestination}
        isLoading={isLoading}
        error={error}
      />
    </View>
  );
}

function FlightSetupHeader({ onClose }: { onClose: () => void }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.header}>
      <ThemedText type="title">Flight Setup</ThemedText>
      <TouchableOpacity
        onPress={onClose}
        style={styles.closeButton}
        accessibilityLabel="Close flight setup"
        accessibilityRole="button"
        hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
      >
        <IconSymbol name="xmark" size={24} color={colors.text} />
      </TouchableOpacity>
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

  return (
    <View style={[styles.footer, { borderTopColor: colors.border }]}>
      <Button
        title="Review Flight"
        onPress={onStart}
        size="lg"
        disabled={isDisabled}
      />
    </View>
  );
}

function FlightSetupContent({
  homeAirport,
  flightTime,
  onFlightTimeChange,
  destinations,
  selectedDestination,
  onSelectDestination,
  isLoadingDestinations,
  destinationsError,
  onClose,
}: {
  homeAirport: Airport | null;
  flightTime: number;
  onFlightTimeChange: (time: number) => void;
  destinations: AirportWithFlightTime[];
  selectedDestination: AirportWithFlightTime | null;
  onSelectDestination: (airport: AirportWithFlightTime) => void;
  isLoadingDestinations: boolean;
  destinationsError: string | null;
  onClose: () => void;
}) {
  return (
    <View style={styles.content}>
      <FlightSetupHeader onClose={onClose} />
      <DepartureSection homeAirport={homeAirport} />
      <TimeSliderSection
        flightTime={flightTime}
        onFlightTimeChange={onFlightTimeChange}
      />
      <DestinationsSection
        destinations={destinations}
        selectedDestination={selectedDestination}
        onSelectDestination={onSelectDestination}
        isLoading={isLoadingDestinations}
        error={destinationsError}
      />
    </View>
  );
}

export default function FlightSetupScreen() {
  const { homeAirport } = useHomeAirport();
  const router = useRouter();
  const {
    origin,
    destination,
    flightDuration,
    setOrigin,
    setDestination,
    setFlightDuration,
  } = useFlight();

  // Local state for slider value (updates immediately)
  const [localFlightTime, setLocalFlightTime] = useState(flightDuration);

  // Sync origin with home airport
  useEffect(() => {
    if (homeAirport && (!origin || origin.icao !== homeAirport.icao)) {
      setOrigin(homeAirport);
    }
  }, [homeAirport, origin, setOrigin]);

  // Fetch destinations based on current flight time
  const {
    destinations,
    isLoading: isLoadingDestinations,
    error: destinationsError,
  } = useDestinations({
    origin: homeAirport,
    flightTimeInSeconds: localFlightTime,
    useTimeRange: true,
  });

  // Find the selected destination in the destinations array
  // (needed because destination from context is Airport, but we need AirportWithFlightTime)
  const selectedDestinationWithTime = destination
    ? destinations.find((d) => d.icao === destination.icao) || null
    : null;

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

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {
      // Ignore haptics error
    });

    // Navigate to flight review screen
    router.push('/flight/review');
  }, [destination, router]);

  const handleClose = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {
      // Ignore haptics error
    });
    router.back();
  }, [router]);

  const isStartDisabled = !destination || !homeAirport;

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <FlightSetupContent
          homeAirport={homeAirport}
          flightTime={localFlightTime}
          onFlightTimeChange={handleFlightTimeChange}
          destinations={destinations}
          selectedDestination={selectedDestinationWithTime}
          onSelectDestination={handleSelectDestination}
          isLoadingDestinations={isLoadingDestinations}
          destinationsError={destinationsError}
          onClose={handleClose}
        />
        <FlightSetupFooter
          onStart={handleReviewFlight}
          isDisabled={isStartDisabled}
        />
      </SafeAreaView>
    </ThemedView>
  );
}
