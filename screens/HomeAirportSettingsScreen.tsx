import { HomeAirportDisplay } from '@/components/home-airport/HomeAirportDisplay';
import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useHomeAirport } from '@/hooks/useHomeAirport';
import { getCurrentLocation, hasLocationPermission } from '@/services/locationService';
import { storageService } from '@/services/storageService';
import { Button } from '@/components/ui/Button';
import type { AirportWithDistance } from '@/types/airport';
import type { Coordinates } from '@/types/location';
import { useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { StyleSheet, View, ScrollView, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderBottomWidth: 1,
  },
  headerTitle: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold,
  },
  contentContainer: {
    flexGrow: 1,
    padding: Spacing.lg,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.sm,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  description: {
    fontSize: Typography.fontSize.sm,
    marginBottom: Spacing.lg,
    lineHeight: 20,
  },
  devSection: {
    marginTop: Spacing.xxl,
    borderTopWidth: 1,
    paddingTop: Spacing.lg,
  },
});

export default function HomeAirportSettingsScreen() {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);
  const { homeAirport, isLoading, handleSelectAirport } = useHomeAirport();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const router = useRouter();

  const handleRestartOnboarding = useCallback(async () => {
    Alert.alert(
      "Restart Onboarding",
      "Are you sure you want to reset the onboarding status? This will return you to the welcome screen.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Restart",
          style: "destructive",
          onPress: async () => {
            await storageService.setOnboardingCompleted(false);
            router.replace('/onboarding');
          }
        }
      ]
    );
  }, [router]);

  const handleSelect = useCallback(
    async (airportWithDistance: AirportWithDistance) => {
      // Extract Airport fields from AirportWithDistance
      // skipcq: JS-0356
      const { distance, ...airport } = airportWithDistance;
      try {
        await handleSelectAirport(airport);
        setIsModalVisible(false);
      } catch {
        // Error already handled in hook
      }
    },
    [handleSelectAirport]
  );

  const openModal = useCallback(async () => {
    const hasPermission = await hasLocationPermission();
    if (hasPermission) {
      const location = await getCurrentLocation();
      if (location) {
        setUserLocation(location);
      }
    }
    setIsModalVisible(true);
  }, []);

  const closeModal = useCallback(() => setIsModalVisible(false), []);

  if (isLoading) {
    return (
      <ThemedView style={styles.container}>
         <SafeAreaView edges={['top']} style={{ flex: 1 }}>
           <View style={styles.contentContainer}>
            <ThemedText>Loading...</ThemedText>
           </View>
         </SafeAreaView>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView edges={['top']} style={{ flex: 1 }}>
        <View style={[styles.header, { borderBottomColor: colors.borderLight }]}>
            <ThemedText style={styles.headerTitle}>Settings</ThemedText>
        </View>
        <ScrollView contentContainerStyle={styles.contentContainer} testID="settings-scroll-view">
          <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
            Current Home Base
          </ThemedText>

          <ThemedText style={[styles.description, { color: colors.textSecondary }]}>
            This airport is used as the starting point for all your flight simulations.
          </ThemedText>

          {homeAirport ? (
            <HomeAirportDisplay airport={homeAirport} onEdit={openModal} />
          ) : (
            <ThemedText testID="no-home-airport-text">No home airport set.</ThemedText>
          )}

          <View style={[styles.devSection, { borderTopColor: colors.borderLight }]}>
            <ThemedText style={[styles.sectionTitle, { color: colors.error }]}>
              Development Zone
            </ThemedText>
            <ThemedText style={[styles.description, { color: colors.textSecondary }]}>
              Resetting onboarding will take you back to the initial setup flow.
            </ThemedText>
            <Button
              title="Restart Onboarding"
              onPress={handleRestartOnboarding}
              variant="secondary"
              style={{ borderColor: colors.error }}
            />
          </View>

        </ScrollView>
      </SafeAreaView>

      <SelectAirportModal
        visible={isModalVisible}
        onClose={closeModal}
        onSelectAirport={handleSelect}
        title="Change Home Base"
        origin={userLocation}
      />
    </ThemedView>
  );
}
