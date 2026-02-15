/**
 * Seat Selection Screen
 *
 * Allows user to select a seat from the aircraft cabin.
 *
 * @module app/flight/seat-selection
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { SeatMap } from '@/components/flight/SeatMap';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { Seat } from '@/types/flight';
import { generateSeatMap } from '@/utils/flightGenerator';

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
  content: {
    flex: 1,
  },
  flightInfo: {
    padding: Spacing.lg,
    paddingBottom: Spacing.md,
  },
  flightRoute: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.xs,
  },
  flightNumber: {
    fontSize: Typography.fontSize.sm,
    fontFamily: 'monospace',
  },
  seatMapContainer: {
    flex: 1,
  },
  footer: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
    borderTopWidth: 1,
    gap: Spacing.md,
  },
  selectedSeatCard: {
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
    marginBottom: Spacing.sm,
  },
  selectedSeatRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  selectedSeatLabel: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
  },
  selectedSeatValue: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold,
  },
});

export default function SeatSelectionScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { booking, setSeat } = useFlight();

  const [selectedSeat, setSelectedSeat] = useState<Seat | null>(null);

  // Generate seat map from booking
  const seats = useMemo(() => {
    if (!booking) return [];
    return generateSeatMap(booking.aircraft);
  }, [booking]);

  const handleBack = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {});
    router.back();
  }, [router]);

  const handleSelectSeat = useCallback((seat: Seat) => {
    setSelectedSeat(seat);
  }, []);

  const handleRandomSeat = useCallback(() => {
    const availableSeats = seats.filter((s) => s.isAvailable);
    if (availableSeats.length === 0) return;

    const randomSeat =
      availableSeats[Math.floor(Math.random() * availableSeats.length)];
    
    impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});
    setSelectedSeat(randomSeat);
  }, [seats]);

  const handleConfirmSeat = useCallback(async () => {
    if (!selectedSeat) return;

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    // Save seat to context
    await setSeat(selectedSeat);

    // Navigate to booking confirmation
    router.push('/flight/booking-confirmation');
  }, [selectedSeat, setSeat, router]);

  if (!booking) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={{ flex: 1 }}>
          <View style={styles.header}>
            <TouchableOpacity
              onPress={handleBack}
              style={styles.backButton}
              accessibilityLabel="Go back"
              accessibilityRole="button"
            >
              <IconSymbol name="chevron.left" size={24} color={colors.text} />
            </TouchableOpacity>
            <ThemedText type="title">Seat Selection</ThemedText>
            <View style={{ width: 40 }} />
          </View>
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ThemedText>No booking data available</ThemedText>
          </View>
        </SafeAreaView>
      </ThemedView>
    );
  }

  const originCode = booking.origin.iata || booking.origin.icao;
  const destCode = booking.destination.iata || booking.destination.icao;

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <View style={styles.header}>
          <TouchableOpacity
            onPress={handleBack}
            style={styles.backButton}
            accessibilityLabel="Go back"
            accessibilityRole="button"
          >
            <IconSymbol name="chevron.left" size={24} color={colors.text} />
          </TouchableOpacity>
          <ThemedText type="title">Select Your Seat</ThemedText>
          <View style={{ width: 40 }} />
        </View>

        <View style={styles.content}>
          {/* Flight info */}
          <View style={styles.flightInfo}>
            <ThemedText style={styles.flightRoute}>
              {originCode} → {destCode}
            </ThemedText>
            <ThemedText
              style={[styles.flightNumber, { color: colors.textSecondary }]}
            >
              {booking.flightNumber} • {booking.aircraft.name}
            </ThemedText>
          </View>

          {/* Seat map */}
          <View style={styles.seatMapContainer}>
            <SeatMap
              aircraft={booking.aircraft}
              seats={seats}
              selectedSeat={selectedSeat}
              onSelectSeat={handleSelectSeat}
            />
          </View>
        </View>

        {/* Footer */}
        <View
          style={[
            styles.footer,
            { backgroundColor: colors.surface, borderTopColor: colors.border },
          ]}
        >
          {/* Selected seat info */}
          {selectedSeat && (
            <View
              style={[
                styles.selectedSeatCard,
                { backgroundColor: colors.cardBackground },
              ]}
            >
              <View style={styles.selectedSeatRow}>
                <ThemedText
                  style={[
                    styles.selectedSeatLabel,
                    { color: colors.textSecondary },
                  ]}
                >
                  Selected Seat
                </ThemedText>
                <ThemedText style={[styles.selectedSeatValue, { color: colors.primary }]}>
                  {selectedSeat.row}
                  {selectedSeat.letter}
                </ThemedText>
              </View>
            </View>
          )}

          {/* Buttons */}
          <View style={{ flexDirection: 'row', gap: Spacing.md }}>
            <View style={{ flex: 1 }}>
              <Button
                title="Random Seat"
                onPress={handleRandomSeat}
                size="lg"
                variant="secondary"
              />
            </View>
            <View style={{ flex: 1 }}>
              <Button
                title="Confirm Seat"
                onPress={handleConfirmSeat}
                size="lg"
                disabled={!selectedSeat}
              />
            </View>
          </View>
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
