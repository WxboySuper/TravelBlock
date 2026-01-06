import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, View, Alert } from 'react-native';

import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    paddingTop: 60,
  },
  header: {
    marginBottom: 40,
  },
  content: {
    flex: 1,
  },
  airportCard: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderRadius: 12,
    backgroundColor: 'rgba(10, 126, 164, 0.05)',
    borderWidth: 1,
    borderColor: 'rgba(10, 126, 164, 0.1)',
  },
  airportIcon: {
    marginRight: 16,
  },
  airportInfo: {
    flex: 1,
  },
  subText: {
    color: '#687076',
    marginTop: 2,
  },
  editButton: {
    padding: 8,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 100,
  },
  emptyText: {
    textAlign: 'center',
    color: '#687076',
    marginTop: 8,
    marginBottom: 24,
  },
  button: {
    backgroundColor: '#0a7ea4',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
});

export default function HomeScreen() {
  const [homeAirport, setHomeAirport] = useState<Airport | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

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

      // Optimistically update UI
      setHomeAirport(airport);

      try {
        await storageService.saveHomeAirport(airport);
        // Only close modal when persistence succeeds
        setIsModalVisible(false);
      } catch (error) {
        console.error('Failed to save home airport', error);
        // Revert optimistic update on error to keep UI consistent
        setHomeAirport(previousAirport ?? null);
        // Show user-visible feedback about the failure
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
        <ThemedText>Loading...</ThemedText>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <View style={styles.header}>
        <ThemedText type="title">TravelBlock</ThemedText>
        <ThemedText type="subtitle">Ready for Departure</ThemedText>
      </View>

      <View style={styles.content}>
        {homeAirport ? (
          <View style={styles.airportCard}>
            <View style={styles.airportIcon}>
              <IconSymbol name="house.fill" size={32} color="#0a7ea4" />
            </View>
            <View style={styles.airportInfo}>
              <ThemedText type="defaultSemiBold">Home Base</ThemedText>
              <ThemedText>{homeAirport.name}</ThemedText>
              <ThemedText type="small" style={styles.subText}>
                {homeAirport.city}, {homeAirport.country} ({homeAirport.icao})
              </ThemedText>
            </View>
            <Pressable onPress={openModal} style={styles.editButton}>
              <IconSymbol name="pencil" size={20} color="#687076" />
            </Pressable>
          </View>
        ) : (
          <View style={styles.emptyState}>
            <IconSymbol name="airplane.departure" size={64} color="#687076" style={{ marginBottom: 16 }} />
            <ThemedText type="subtitle" style={{ textAlign: 'center' }}>Set Your Home Base</ThemedText>
            <ThemedText style={styles.emptyText}>
              Choose your starting airport to begin your focus journey.
            </ThemedText>
            <Pressable onPress={openModal} style={styles.button}>
              <ThemedText type="defaultSemiBold" style={{ color: '#fff' }}>Select Airport</ThemedText>
            </Pressable>
          </View>
        )}
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
