import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';
import { TouchableOpacity } from 'react-native-gesture-handler';

export default function HomeScreen() {
  const [homeAirport, setHomeAirport] = useState<Airport | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadHomeAirport();
  }, []);

  async function loadHomeAirport() {
    try {
      const saved = await storageService.getHomeAirport();
      setHomeAirport(saved);
    } catch (error) {
      console.error('Failed to load home airport', error);
    } finally {
      setIsLoading(false);
    }
  }

  const handleSelectAirport = async (airport: Airport) => {
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
    }
  };

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
            <TouchableOpacity onPress={() => setIsModalVisible(true)} style={styles.editButton}>
              <IconSymbol name="pencil" size={20} color="#687076" />
            </TouchableOpacity>
          </View>
        ) : (
          <View style={styles.emptyState}>
            <IconSymbol name="airplane.departure" size={64} color="#687076" style={{ marginBottom: 16 }} />
            <ThemedText type="subtitle" style={{ textAlign: 'center' }}>Set Your Home Base</ThemedText>
            <ThemedText style={styles.emptyText}>
              Choose your starting airport to begin your focus journey.
            </ThemedText>
            <TouchableOpacity
              onPress={() => setIsModalVisible(true)}
              style={styles.button}
            >
              <ThemedText type="defaultSemiBold" style={{ color: '#fff' }}>Select Airport</ThemedText>
            </TouchableOpacity>
          </View>
        )}
      </View>

      <SelectAirportModal
        visible={isModalVisible}
        onClose={() => setIsModalVisible(false)}
        onSelectAirport={handleSelectAirport}
        title="Select Home Base"
      />
    </ThemedView>
  );
}

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
