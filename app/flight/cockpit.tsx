/**
 * Cockpit Screen (Placeholder for v0.5.0)
 *
 * Temporary placeholder showing flight in progress.
 * v0.5.0 will implement full real-time flight timer and controls.
 *
 * @module app/flight/cockpit
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback } from 'react';
import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors, Spacing, Typography } from '@/constants/theme';
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
  iconContainer: {
    marginBottom: Spacing.xl,
  },
  title: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  subtitle: {
    fontSize: Typography.fontSize.lg,
    textAlign: 'center',
    marginBottom: Spacing.xl,
  },
  routeText: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.semibold,
    textAlign: 'center',
    marginBottom: Spacing.sm,
  },
  message: {
    fontSize: Typography.fontSize.base,
    textAlign: 'center',
    marginBottom: Spacing.xl,
  },
  footer: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
  },
  note: {
    fontSize: Typography.fontSize.sm,
    textAlign: 'center',
    marginBottom: Spacing.md,
    fontStyle: 'italic',
  },
});

export default function CockpitScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { booking, clearFlightState } = useFlight();

  const handleReturnHome = useCallback(async () => {
    impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});

    // Clear flight state
    await clearFlightState();

    // Navigate back to home
    router.replace('/(tabs)');
  }, [clearFlightState, router]);

  if (!booking) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={{ flex: 1 }}>
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ThemedText>No flight data available</ThemedText>
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
          {/* Airplane icon */}
          <View style={styles.iconContainer}>
            <IconSymbol
              name="airplane"
              size={100}
              color={colors.primary}
            />
          </View>

          {/* Title */}
          <ThemedText style={styles.title}>Flight in Progress</ThemedText>

          {/* Route */}
          <ThemedText style={[styles.routeText, { color: colors.primary }]}>
            {originCode} → {destCode}
          </ThemedText>
          <ThemedText style={[styles.subtitle, { color: colors.textSecondary }]}>
            {booking.flightNumber}
          </ThemedText>

          {/* Message */}
          <ThemedText style={[styles.message, { color: colors.textSecondary }]}>
            The real-time flight cockpit experience will be implemented in v0.5.0.
            Stay tuned for the full simulation!
          </ThemedText>
        </View>

        {/* Footer */}
        <View style={styles.footer}>
          <ThemedText style={[styles.note, { color: colors.textSecondary }]}>
            v0.5.0 Preview: Coming Soon
          </ThemedText>
          <Button
            title="Return to Home"
            onPress={handleReturnHome}
            size="lg"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
