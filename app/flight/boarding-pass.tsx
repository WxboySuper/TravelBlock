/**
 * Boarding Pass Screen
 *
 * Displays boarding pass with scan/board interaction.
 *
 * @module app/flight/boarding-pass
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useRef } from 'react';
import { Animated, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { BoardingPass } from '@/components/flight/BoardingPass';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
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
  header: {
    alignItems: 'center',
    marginBottom: Spacing.xl,
  },
  title: {
    fontSize: Typography.fontSize.xxl,
    fontWeight: Typography.fontWeight.bold,
    textAlign: 'center',
    marginBottom: Spacing.sm,
  },
  subtitle: {
    fontSize: Typography.fontSize.base,
    textAlign: 'center',
  },
  passContainer: {
    marginBottom: Spacing.xl,
  },
  instruction: {
    fontSize: Typography.fontSize.base,
    textAlign: 'center',
    marginBottom: Spacing.xl,
  },
  footer: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xl,
  },
  statusText: {
    fontSize: Typography.fontSize.sm,
    textAlign: 'center',
    marginBottom: Spacing.md,
    fontWeight: Typography.fontWeight.semibold,
  },
});

export default function BoardingPassScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { boardingPass, setBoardingPass } = useFlight();

  // Animation for boarding pass entrance
  const slideAnim = useRef(new Animated.Value(300)).current;

  useEffect(() => {
    // Animate boarding pass sliding up
    Animated.spring(slideAnim, {
      toValue: 0,
      friction: 8,
      tension: 40,
      useNativeDriver: true,
    }).start();
  }, [slideAnim]);

  const handleBoardFlight = useCallback(async () => {
    if (!boardingPass) return;

    // Heavy haptic feedback for boarding
    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    // Update boarding pass status
    const updatedPass = {
      ...boardingPass,
      isBoarded: true,
    };
    await setBoardingPass(updatedPass);

    // Navigate to cockpit
    router.push('/flight/cockpit');
  }, [boardingPass, setBoardingPass, router]);

  if (!boardingPass) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={{ flex: 1 }}>
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ThemedText>No boarding pass available</ThemedText>
          </View>
        </SafeAreaView>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <View style={styles.content}>
          {/* Header */}
          <View style={styles.header}>
            <ThemedText style={styles.title}>Boarding Pass</ThemedText>
            <ThemedText style={[styles.subtitle, { color: colors.success }]}>
              ✓ Checked In
            </ThemedText>
          </View>

          {/* Boarding Pass (animated) */}
          <Animated.View
            style={[
              styles.passContainer,
              { transform: [{ translateY: slideAnim }] },
            ]}
          >
            <BoardingPass
              boardingPass={boardingPass}
              interactive={false}
            />
          </Animated.View>

          {/* Instruction */}
          <ThemedText style={[styles.instruction, { color: colors.textSecondary }]}>
            Ready to board! Tap the button below to begin your flight.
          </ThemedText>
        </View>

        {/* Footer */}
        <View style={styles.footer}>
          <ThemedText style={[styles.statusText, { color: colors.success }]}>
            Ready to Board
          </ThemedText>
          <Button
            title="Board Flight"
            onPress={handleBoardFlight}
            size="lg"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
