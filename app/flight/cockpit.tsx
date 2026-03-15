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
import { useCallback, useEffect, useRef, useState } from 'react';
import { Alert, StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing, Typography } from '@/constants/theme';
import { useFlight } from '@/context/FlightContext';

// Cockpit components
import { CockpitTab, CockpitTabs } from '@/components/cockpit/CockpitTabs';
import { DivertModal } from '@/components/cockpit/DivertModal';
import { FlightMapView } from '@/components/cockpit/FlightMapView';
import { InfoPanel } from '@/components/cockpit/InfoPanel';
import { MetricsGrid } from '@/components/cockpit/MetricsGrid';
import { TimerDisplay } from '@/components/cockpit/TimerDisplay';

// Services
import { calculateDivertRoute, findNearestAirports, getDivertReason } from '@/services/divertService';
import { persistDivertedFlightState } from '@/services/flightStateService';
import { flightTimerService } from '@/services/flightTimerService';

// Types
import type { DivertOption, FlightBooking, FlightProgress } from '@/types/flight';
import type { Dispatch, SetStateAction } from 'react';

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

type FlightActions = Pick<
  ReturnType<typeof useFlight>,
  'boardingPass' | 'clearFlightState' | 'setBoardingPass' | 'setBooking' | 'setDestination'
>;

type TimerConfig = {
  booking: FlightBooking | null;
  isLoaded: boolean;
  onArrival: () => void;
  router: ReturnType<typeof useRouter>;
  setActiveBooking: Dispatch<SetStateAction<FlightBooking | null>>;
  setProgress: Dispatch<SetStateAction<FlightProgress | null>>;
};

type CockpitFlightState = {
  activeBooking: FlightBooking | null;
  isDiverted: boolean;
  progress: FlightProgress | null;
  setActiveBooking: Dispatch<SetStateAction<FlightBooking | null>>;
  setIsDiverted: Dispatch<SetStateAction<boolean>>;
  setProgress: Dispatch<SetStateAction<FlightProgress | null>>;
};

type DivertControls = {
  divertOptions: DivertOption[];
  divertReason: string;
  handleDivertClose: () => void;
  handleDivertPress: () => Promise<void>;
  handleDivertSelect: (option: DivertOption) => Promise<void>;
  isLoadingDiverts: boolean;
  showDivertModal: boolean;
};

type DivertConfig = {
  activeBooking: FlightBooking | null;
  flightActions: FlightActions;
  progress: FlightProgress | null;
  setActiveBooking: Dispatch<SetStateAction<FlightBooking | null>>;
  setIsDiverted: Dispatch<SetStateAction<boolean>>;
};

function getFlightCompletionMessage(
  booking: FlightBooking | null,
  isDiverted: boolean
): string {
  const routeSummary = booking
    ? `${booking.origin.iata} → ${booking.destination.iata}`
    : 'Route unavailable';

  return isDiverted
    ? `Diverted flight completed!\n\n${routeSummary}`
    : `Flight completed successfully!\n\n${routeSummary}`;
}

function renderSelectedTabContent(
  selectedTab: CockpitTab,
  activeBooking: FlightBooking,
  progress: FlightProgress,
  isDiverted: boolean
) {
  switch (selectedTab) {
    case 'map':
      return (
        <FlightMapView
          origin={activeBooking.origin}
          destination={activeBooking.destination}
          currentPosition={progress.currentPosition}
          heading={progress.heading}
          isDiverted={isDiverted}
        />
      );
    case 'metrics':
      return <MetricsGrid progress={progress} />;
    case 'info':
      return <InfoPanel booking={activeBooking} />;
    default:
      return null;
  }
}

function useCockpitFlightState(booking: FlightBooking | null): CockpitFlightState {
  const [activeBooking, setActiveBooking] = useState(booking);
  const [progress, setProgress] = useState<FlightProgress | null>(null);
  const [isDiverted, setIsDiverted] = useState(false);

  useEffect(() => {
    if (booking) {
      setActiveBooking(booking);
    }
  }, [booking]);

  return {
    activeBooking,
    isDiverted,
    progress,
    setActiveBooking,
    setIsDiverted,
    setProgress,
  };
}

function useCockpitTimer(
  config: TimerConfig
) {
  const { booking, isLoaded, onArrival, router, setActiveBooking, setProgress } = config;
  const timerStartedRef = useRef(false);
  const arrivalHandlerRef = useRef(onArrival);

  useEffect(() => {
    arrivalHandlerRef.current = onArrival;
  }, [onArrival]);

  useEffect(() => {
    if (!isLoaded || timerStartedRef.current) {
      return undefined;
    }

    if (!booking) {
      console.warn('[Cockpit] No booking data, returning to home');
      router.replace('/(tabs)');
      return undefined;
    }

    timerStartedRef.current = true;
    setActiveBooking(booking);
    flightTimerService.startFlight(booking);

    const unsubscribeTick = flightTimerService.onTick((newProgress) => {
      setProgress(newProgress);
    });
    const unsubscribePhaseChange = flightTimerService.onPhaseChange(() => {
      impactAsync(ImpactFeedbackStyle.Medium).catch(() => undefined);
    });
    const unsubscribeArrival = flightTimerService.onArrival(() => {
      arrivalHandlerRef.current();
    });

    function cleanupTimer() {
      unsubscribeTick();
      unsubscribePhaseChange();
      unsubscribeArrival();
      timerStartedRef.current = false;
      flightTimerService.cleanup();
    }

    return cleanupTimer;
  }, [booking, isLoaded, router, setActiveBooking, setProgress]);
}

function useDivertControls(
  config: DivertConfig
): DivertControls {
  const {
    activeBooking,
    flightActions,
    progress,
    setActiveBooking,
    setIsDiverted,
  } = config;
  const [showDivertModal, setShowDivertModal] = useState(false);
  const [divertOptions, setDivertOptions] = useState<DivertOption[]>([]);
  const [divertReason, setDivertReason] = useState('');
  const [isLoadingDiverts, setIsLoadingDiverts] = useState(false);

  const handleDivertPress = useCallback(async () => {
    if (!progress || !activeBooking) {
      return;
    }

    flightTimerService.pauseFlight();
    setIsLoadingDiverts(true);
    setDivertReason(getDivertReason());

    const nearestAirports = await findNearestAirports(
      progress.currentPosition,
      activeBooking.destination.icao,
      5,
      500
    );

    setDivertOptions(nearestAirports);
    setIsLoadingDiverts(false);
    setShowDivertModal(true);
    await impactAsync(ImpactFeedbackStyle.Medium).catch(() => undefined);
  }, [activeBooking, progress]);

  const handleDivertSelect = useCallback(async (option: DivertOption) => {
    if (!activeBooking || !progress) {
      return;
    }

    setShowDivertModal(false);
    setIsDiverted(true);

    const newBooking = calculateDivertRoute(
      activeBooking,
      progress.currentPosition,
      option.airport,
      progress.elapsedSeconds
    );

    setActiveBooking(newBooking);
    await persistDivertedFlightState(newBooking, flightActions.boardingPass, {
      setBooking: flightActions.setBooking,
      setBoardingPass: flightActions.setBoardingPass,
      setDestination: flightActions.setDestination,
    });

    flightTimerService.updateFlight(newBooking, 0);
    flightTimerService.resumeFlight();
    await impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);

    Alert.alert(
      '⚠️ Flight Diverted',
      `Rerouting to ${option.airport.name} (${option.airport.iata})\nDistance: ${option.distanceFromCurrent.toFixed(1)} km\nETA: ${Math.round(option.estimatedTime / 60)} minutes`,
      [{ text: 'OK' }]
    );
  }, [activeBooking, flightActions, progress, setActiveBooking, setIsDiverted]);

  const handleDivertClose = useCallback(() => {
    setShowDivertModal(false);
    flightTimerService.resumeFlight();
  }, []);

  return {
    divertOptions,
    divertReason,
    handleDivertClose,
    handleDivertPress,
    handleDivertSelect,
    isLoadingDiverts,
    showDivertModal,
  };
}

export default function CockpitScreen() {
  const router = useRouter();
  const {
    booking,
    boardingPass,
    clearFlightState,
    isLoaded,
    setBoardingPass,
    setBooking,
    setDestination,
  } = useFlight();
  const {
    activeBooking,
    isDiverted,
    progress,
    setActiveBooking,
    setIsDiverted,
    setProgress,
  } = useCockpitFlightState(booking);

  // UI state
  const [selectedTab, setSelectedTab] = useState<CockpitTab>('map');

  /**
   * Handle flight arrival (completion)
   */
  const handleArrival = useCallback(async () => {
    // Heavy haptic feedback
    await impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);

    Alert.alert(
      '✅ Flight Completed',
      getFlightCompletionMessage(activeBooking, isDiverted),
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
  }, [activeBooking, isDiverted, clearFlightState, router]);

  useCockpitTimer({
    booking,
    isLoaded,
    onArrival: handleArrival,
    router,
    setActiveBooking,
    setProgress,
  });

  const {
    divertOptions,
    divertReason,
    handleDivertClose,
    handleDivertPress,
    handleDivertSelect,
    isLoadingDiverts,
    showDivertModal,
  } = useDivertControls({
    activeBooking,
    flightActions: {
      boardingPass,
      clearFlightState,
      setBoardingPass,
      setBooking,
      setDestination,
    },
    progress,
    setActiveBooking,
    setIsDiverted,
  });

  // Show loading state while initializing
  if (!isLoaded || !activeBooking || !progress) {
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
        <View style={styles.content}>{renderSelectedTabContent(selectedTab, activeBooking, progress, isDiverted)}</View>

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
