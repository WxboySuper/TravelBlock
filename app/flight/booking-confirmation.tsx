/**
 * Booking Confirmation Screen
 *
 * Shows booking success with flight summary.
 *
 * @module app/flight/booking-confirmation
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useRef } from 'react';
import { Animated, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.xl,
  },
  checkmarkContainer: {
    marginBottom: Spacing.xl,
  },
  title: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  message: {
    fontSize: Typography.fontSize.lg,
    textAlign: 'center',
    marginBottom: Spacing.xl,
  },
  detailsCard: {
    width: '100%',
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.xl,
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
  footer: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
  },
  bookingRef: {
    fontSize: Typography.fontSize.sm,
    textAlign: 'center',
    marginBottom: Spacing.md,
    fontFamily: 'monospace',
  },
});

export default function BookingConfirmationScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { booking, seat } = useFlight();

  // Animation for checkmark
  const scaleAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    // Trigger haptic feedback
    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    // Animate checkmark
    Animated.spring(scaleAnim, {
      toValue: 1,
      friction: 5,
      tension: 40,
      useNativeDriver: true,
    }).start();
  }, [scaleAnim]);

  const handleContinue = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});
    router.push('/flight/check-in');
  }, [router]);

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
        <View style={styles.content}>
          {/* Animated checkmark */}
          <Animated.View
            style={[styles.checkmarkContainer, { transform: [{ scale: scaleAnim }] }]}
          >
            <IconSymbol
              name="checkmark.circle.fill"
              size={80}
              color={colors.success}
            />
          </Animated.View>

          {/* Success message */}
          <ThemedText style={styles.title}>Booking Confirmed!</ThemedText>
          <ThemedText
            style={[styles.message, { color: colors.textSecondary }]}
          >
            Your flight has been successfully booked
          </ThemedText>

          {/* Booking reference */}
          <ThemedText
            style={[styles.bookingRef, { color: colors.textSecondary }]}
          >
            Booking Reference: {booking.bookingReference}
          </ThemedText>

          {/* Flight details */}
          <View style={[styles.detailsCard, { backgroundColor: colors.cardBackground }]}>
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
                {seat.letter} ({seat.seatClass})
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
        </View>

        {/* Footer */}
        <View style={styles.footer}>
          <Button
            title="Continue to Check-In"
            onPress={handleContinue}
            size="lg"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
