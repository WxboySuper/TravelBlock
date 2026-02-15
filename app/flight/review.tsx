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
  footerContent: {
    flexDirection: 'row',
    gap: Spacing.md,
  },
});

export default function FlightReviewScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { origin, destination, flightDuration, setBooking } = useFlight();

  // Generate booking on mount (memoized)
  const booking = useMemo(() => {
    if (!origin || !destination) return null;
    return generateFlightBooking(origin, destination, flightDuration);
  }, [origin, destination, flightDuration]);

  const formattedTime = formatTimeValue(flightDuration);
  const distanceKm = booking?.distanceKm ?? 0;
  const distanceMiles = Math.round(distanceKm * 0.621371);

  const handleBack = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {});
    router.back();
  }, [router]);

  const handleBookFlight = useCallback(async () => {
    if (!booking) return;

    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});
    
    // Save booking to context
    await setBooking(booking);
    
    // Navigate to seat selection
    router.push('/flight/seat-selection');
  }, [booking, setBooking, router]);

  if (!origin || !destination || !booking) {
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
            <ThemedText type="title">Flight Review</ThemedText>
            <View style={{ width: 40 }} />
          </View>
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ThemedText>No flight data available</ThemedText>
          </View>
        </SafeAreaView>
      </ThemedView>
    );
  }

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
          <ThemedText type="title">Review Flight</ThemedText>
          <View style={{ width: 40 }} />
        </View>

        <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
          <View style={styles.content}>
            {/* Route Section */}
            <View style={styles.section}>
              <ThemedText
                style={[styles.sectionTitle, { color: colors.textSecondary }]}
              >
                Your Route
              </ThemedText>
              <View style={[styles.routeCard, { backgroundColor: colors.cardBackground }]}>
                <View style={styles.routeHeader}>
                  {/* Departure */}
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

                  {/* Arrow */}
                  <View style={styles.routeArrow}>
                    <IconSymbol name="arrow.right" size={28} color={colors.textSecondary} />
                  </View>

                  {/* Destination */}
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
            </View>

            {/* Flight Details Section */}
            <View style={styles.section}>
              <ThemedText
                style={[styles.sectionTitle, { color: colors.textSecondary }]}
              >
                Flight Details
              </ThemedText>
              <View style={[styles.detailsCard, { backgroundColor: colors.cardBackground }]}>
                <View style={styles.detailRow}>
                  <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                    Flight Number
                  </ThemedText>
                  <ThemedText style={[styles.detailValue, { fontFamily: 'monospace' }]}>
                    {booking.flightNumber}
                  </ThemedText>
                </View>

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

                <View style={styles.detailRow}>
                  <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                    Duration
                  </ThemedText>
                  <ThemedText style={styles.detailValue}>
                    {formattedTime.formatted}
                  </ThemedText>
                </View>

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

                <View style={styles.detailRow}>
                  <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                    Distance
                  </ThemedText>
                  <ThemedText style={styles.detailValue}>
                    {distanceMiles} mi ({Math.round(distanceKm)} km)
                  </ThemedText>
                </View>

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

                <View style={styles.detailRow}>
                  <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                    Aircraft
                  </ThemedText>
                  <ThemedText style={styles.detailValue}>
                    {booking.aircraft.name}
                  </ThemedText>
                </View>

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

                <View style={styles.detailRow}>
                  <ThemedText style={[styles.detailLabel, { color: colors.textSecondary }]}>
                    Gate
                  </ThemedText>
                  <ThemedText style={styles.detailValue}>
                    {booking.gate}
                  </ThemedText>
                </View>
              </View>
            </View>
          </View>
        </ScrollView>

        {/* Footer with Book Button */}
        <View style={[styles.footer, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
          <Button
            title="Book Flight"
            onPress={handleBookFlight}
            size="lg"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
