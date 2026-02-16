/**
 * Cockpit Screen
 *
 * Real-time flight simulation cockpit with interactive map, metrics display,
 * and flight controls. Features include:
 * - Live position tracking on map
 * - Real-time flight metrics (speed, altitude, heading, etc.)
 * - Flight information panel
 * - Divert functionality to nearest airports
 *
 * @module app/flight/cockpit
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useRouter } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Alert, StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';
import { useColorScheme } from '@/hooks/use-color-scheme';

// Cockpit components
import { CockpitTab, CockpitTabs } from '@/components/cockpit/CockpitTabs';
import { DivertModal } from '@/components/cockpit/DivertModal';
import { FlightMapView } from '@/components/cockpit/FlightMapView';
import { InfoPanel } from '@/components/cockpit/InfoPanel';
import { MetricsGrid } from '@/components/cockpit/MetricsGrid';
import { TimerDisplay } from '@/components/cockpit/TimerDisplay';

// Services
import { calculateDivertRoute, findNearestAirports, getDivertReason } from '@/services/divertService';
import { flightTimerService } from '@/services/flightTimerService';

// Types
import type { DivertOption, FlightProgress } from '@/types/flight';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
  },
  divertButton: {
    position: 'absolute',
    top: 12,
    right: 12,
    backgroundColor: '#F59E0B',
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: 20,
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
    zIndex: 999,
  },
  divertButtonText: {
    color: '#FFFFFF',
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.bold as any,
  },
});

export default function CockpitScreen() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { booking, clearFlightState } = useFlight();

  // UI state
  const [selectedTab, setSelectedTab] = useState<CockpitTab>('map');
  const [showDivertModal, setShowDivertModal] = useState(false);
  const [divertOptions, setDivertOptions] = useState<DivertOption[]>([]);
  const [divertReason, setDivertReason] = useState('');
  const [isLoadingDiverts, setIsLoadingDiverts] = useState(false);

  // Flight progress state
  const [progress, setProgress] = useState<FlightProgress | null>(null);
  const [isDiverted, setIsDiverted] = useState(false);

  // Initialize flight timer on mount
  useEffect(() => {
    if (!booking) {
      console.warn('[Cockpit] No booking data, returning to home');
      router.replace('/(tabs)');
      return;
    }

    // Start flight simulation
    flightTimerService.startFlight(booking);

    // Subscribe to timer updates
    const unsubscribeTick = flightTimerService.onTick((newProgress) => {
      setProgress(newProgress);
    });

    const unsubscribePhaseChange = flightTimerService.onPhaseChange((phase) => {
      // Haptic feedback on phase change
      impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});
    });

    const unsubscribeArrival = flightTimerService.onArrival(() => {
      handleArrival();
    });

    // Cleanup on unmount
    return () => {
      unsubscribeTick();
      unsubscribePhaseChange();
      unsubscribeArrival();
      flightTimerService.cleanup();
    };
  }, [booking]);

  /**
   * Handle flight arrival (completion)
   */
  const handleArrival = useCallback(async () => {
    // Heavy haptic feedback
    await impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    Alert.alert(
      '✅ Flight Completed',
      isDiverted
        ? `Diverted flight completed!\n\n${booking?.origin.iata} → ${booking?.destination.iata}`
        : `Flight completed successfully!\n\n${booking?.origin.iata} → ${booking?.destination.iata}`,
      [
        {
          text: 'Return to Home',
          onPress: async () => {
            await clearFlightState();
            router.replace('/(tabs)');
          },
        },
      ],
      { cancelable: false }
    );
  }, [booking, isDiverted, clearFlightState, router]);

  /**
   * Handle divert button press
   */
  const handleDivertPress = useCallback(async () => {
    if (!progress || !booking) return;

    // Pause flight
    flightTimerService.pauseFlight();

    // Get nearest airports
    setIsLoadingDiverts(true);
    setDivertReason(getDivertReason());
    
    const nearestAirports = await findNearestAirports(
      progress.currentPosition,
      booking.destination.icao,
      5,
      500 // 500km max range
    );

    setDivertOptions(nearestAirports);
    setIsLoadingDiverts(false);
    setShowDivertModal(true);

    await impactAsync(ImpactFeedbackStyle.Medium).catch(() => {});
  }, [progress, booking]);

  /**
   * Handle divert airport selection
   */
  const handleDivertSelect = useCallback(async (option: DivertOption) => {
    if (!booking || !progress) return;

    setShowDivertModal(false);
    setIsDiverted(true);

    // Calculate new route to divert airport
    const newBooking = calculateDivertRoute(
      booking,
      progress.currentPosition,
      option.airport,
      progress.elapsedSeconds
    );

    // Update timer with new booking
    flightTimerService.updateFlight(newBooking, 0); // Start from 0 for new route

    // Resume flight
    flightTimerService.resumeFlight();

    await impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});

    // Show toast notification
    Alert.alert(
      '⚠️ Flight Diverted',
      `Rerouting to ${option.airport.name} (${option.airport.iata})\nDistance: ${option.distanceFromCurrent.toFixed(1)} km\nETA: ${Math.round(option.estimatedTime / 60)} minutes`,
      [{ text: 'OK' }]
    );
  }, [booking, progress]);

  /**
   * Handle divert modal close
   */
  const handleDivertClose = useCallback(() => {
    setShowDivertModal(false);
    // Resume flight
    flightTimerService.resumeFlight();
  }, []);

  // Show loading state while initializing
  if (!booking || !progress) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={{ flex: 1 }}>
          <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <ThemedText>Initializing flight...</ThemedText>
          </View>
        </SafeAreaView>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }} edges={['top']}>
        {/* Timer Display */}
        <TimerDisplay
          remainingSeconds={progress.remainingSeconds}
          phase={progress.currentPhase}
          progressPercent={progress.progressPercent}
        />

        {/* Tab Selector */}
        <CockpitTabs
          selectedTab={selectedTab}
          onTabChange={setSelectedTab}
        />

        {/* Content based on selected tab */}
        <View style={styles.content}>
          {selectedTab === 'map' && (
            <FlightMapView
              origin={booking.origin}
              destination={booking.destination}
              currentPosition={progress.currentPosition}
              heading={progress.heading}
              isDiverted={isDiverted}
            />
          )}
          
          {selectedTab === 'metrics' && (
            <MetricsGrid progress={progress} />
          )}
          
          {selectedTab === 'info' && (
            <InfoPanel booking={booking} />
          )}
        </View>

        {/* Divert Button (overlays map on map tab) */}
        {selectedTab === 'map' && (
          <TouchableOpacity
            style={styles.divertButton}
            onPress={handleDivertPress}
            accessibilityLabel="Divert flight"
            accessibilityRole="button"
          >
            <ThemedText style={styles.divertButtonText}>⚠️ Divert</ThemedText>
          </TouchableOpacity>
        )}

        {/* Divert Modal */}
        <DivertModal
          visible={showDivertModal}
          divertOptions={divertOptions}
          reason={divertReason}
          onSelect={handleDivertSelect}
          onClose={handleDivertClose}
          isLoading={isLoadingDiverts}
        />
      </SafeAreaView>
    </ThemedView>
  );
}
