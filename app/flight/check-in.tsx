/**
 * Check-In Screen
 *
 * Mobile check-in flow before boarding pass generation.
 *
 * @module app/flight/check-in
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { generateBoardingPass } from '@/utils/flightGenerator';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    flex: 1,
  },
  content: {
    padding: Spacing.xl,
  },
  header: {
    alignItems: 'center',
    marginBottom: Spacing.xl,
  },
  title: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    textAlign: 'center',
    marginBottom: Spacing.sm,
  },
  subtitle: {
    fontSize: Typography.fontSize.lg,
    textAlign: 'center',
  },
  card: {
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.lg,
  },
  cardTitle: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.md,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
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
  passengerCard: {
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.lg,
    alignItems: 'center',
  },
  passengerName: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold,
    textTransform: 'uppercase',
  },
  checkInMessage: {
    fontSize: Typography.fontSize.base,
    textAlign: 'center',
    marginBottom: Spacing.xl,
  },
  footer: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
  },
  processingContainer: {
    alignItems: 'center',
    gap: Spacing.md,
  },
  processingText: {
    fontSize: Typography.fontSize.base,
  },
});

export default function CheckInScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { booking, seat, setBoardingPass } = useFlight();

  const [isProcessing, setIsProcessing] = useState(false);

  const handleCheckIn = useCallback(async () => {
    if (!booking || !seat) return;

    setIsProcessing(true);
    impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});

    // Simulate check-in processing (2-3 seconds)
    await new Promise((resolve) => setTimeout(resolve, 2500));

    // Generate boarding pass
    const boardingPass = generateBoardingPass(booking, seat);

    // Save to context
    await setBoardingPass(boardingPass);

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    // Navigate to boarding pass screen
    router.push('/flight/boarding-pass');
  }, [booking, seat, setBoardingPass, router]);

  if (!booking || !seat) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={{ flex: 1 }}>
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
        <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
          <View style={styles.content}>
            {/* Header */}
            <View style={styles.header}>
              <ThemedText style={styles.title}>Mobile Check-In</ThemedText>
              <ThemedText style={[styles.subtitle, { color: colors.textSecondary }]}>
                Get your boarding pass
              </ThemedText>
            </View>

            {/* Passenger info */}
            <View
              style={[styles.passengerCard, { backgroundColor: colors.cardBackground }]}
            >
              <ThemedText style={styles.passengerName}>Traveler</ThemedText>
            </View>

            {/* Flight summary */}
            <View style={[styles.card, { backgroundColor: colors.cardBackground }]}>
              <ThemedText style={styles.cardTitle}>Flight Summary</ThemedText>

              <View style={styles.detailRow}>
                <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                  Flight
                </ThemedText>
                <ThemedText style={[styles.detailValue, { fontFamily: 'monospace' }]}>
                  {booking.flightNumber}
                </ThemedText>
              </View>

              <View style={[styles.divider, { backgroundColor: colors.border }]} />

              <View style={styles.detailRow}>
                <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                  Route
                </ThemedText>
                <ThemedText style={styles.detailValue}>
                  {originCode} → {destCode}
                </ThemedText>
              </View>

              <View style={[styles.divider, { backgroundColor: colors.border }]} />

              <View style={styles.detailRow}>
                <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                  Seat
                </ThemedText>
                <ThemedText style={[styles.detailValue, { color: colors.primary }]}>
                  {seat.row}
                  {seat.letter}
                </ThemedText>
              </View>

              <View style={[styles.divider, { backgroundColor: colors.border }]} />

              <View style={styles.detailRow}>
                <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                  Gate
                </ThemedText>
                <ThemedText style={styles.detailValue}>{booking.gate}</ThemedText>
              </View>
            </View>

            {/* Check-in message */}
            <ThemedText
              style={[styles.checkInMessage, { color: colors.textSecondary }]}
            >
              Confirm your details to complete check-in and receive your boarding pass
            </ThemedText>
          </View>
        </ScrollView>

        {/* Footer */}
        <View style={styles.footer}>
          {isProcessing ? (
            <View style={styles.processingContainer}>
              <ActivityIndicator size="large" color={colors.primary} />
              <ThemedText style={[styles.processingText, { color: colors.textSecondary }]}>
                Processing check-in...
              </ThemedText>
            </View>
          ) : (
            <Button
              title="Check In & Get Boarding Pass"
              onPress={handleCheckIn}
              size="lg"
            />
          )}
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
