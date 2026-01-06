import { useCallback, useEffect, useState } from 'react';
import { Alert, StyleSheet, View } from 'react-native';

import { AirportCard } from '@/components/airport/AirportCard';
import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { EmptyHomeBase } from '@/components/home/EmptyHomeBase';
import { HomeHeader } from '@/components/home/HomeHeader';
import { StatusBar } from '@/components/home/StatusBar';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    padding: Spacing.lg,
  },
  airportSection: {
    flex: 1,
    paddingTop: Spacing.md,
  },
});

export default function HomeScreen() {
  const [homeAirport, setHomeAirport] = useState<Airport | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const saved = await storageService.getHomeAirport();
        if (mounted) setHomeAirport(saved);
      } catch (error) {
        console.error('Failed to load home airport', error);
      } finally {
        if (mounted) setIsLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const handleSelectAirport = useCallback(
    async (airport: Airport) => {
      const previousAirport = homeAirport;
      setHomeAirport(airport);

      try {
        await storageService.saveHomeAirport(airport);
        setIsModalVisible(false);
      } catch (error) {
        console.error('Failed to save home airport', error);
        setHomeAirport(previousAirport ?? null);
        try {
          const message = error instanceof Error ? error.message : String(error);
          Alert.alert('Save failed', `Unable to save home airport. ${message}`);
        } catch (e) {
          // ignore any Alert errors
        }
      }
    },
    [homeAirport]
  );

  const openModal = useCallback(() => setIsModalVisible(true), []);
  const closeModal = useCallback(() => setIsModalVisible(false), []);

  if (isLoading) {
    return (
      <ThemedView style={styles.container}>
        <View style={styles.contentContainer}>
          <HomeHeader />
          <ThemedText
            style={{
              fontSize: Typography.fontSize.sm,
              color: colors.textSecondary,
              fontWeight: Typography.fontWeight.medium,
            }}>
            Loading...
          </ThemedText>
        </View>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <StatusBar />

      <View style={styles.contentContainer}>
        <HomeHeader />

        <View style={styles.airportSection}>
          {homeAirport ? (
            <AirportCard airport={homeAirport} onEdit={openModal} />
          ) : (
            <EmptyHomeBase onSelectAirport={openModal} />
          )}
        </View>
      </View>

      <SelectAirportModal
        visible={isModalVisible}
        onClose={closeModal}
        onSelectAirport={handleSelectAirport}
        title="Select Home Base"
      />
    </ThemedView>
  );
}
