import { AirportCard } from '@/components/airport/AirportCard';
import { AirportListItem } from '@/components/airport/AirportListItem';
import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { getCurrentLocation, getNearestAirports, hasLocationPermission } from '@/services/locationService';
import { Airport, AirportWithDistance } from '@/types/airport';
import { Coordinates } from '@/types/location';
import { useCallback, useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

interface HomeAirportSetupStepProps {
  selectedAirport: Airport | null;
  onSelectAirport: (airport: Airport | AirportWithDistance) => void;
  onNext: () => void;
  onBack: () => void;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
  },
  title: {
    marginBottom: 12,
    textAlign: 'center',
  },
  description: {
    marginBottom: 32,
    textAlign: 'center',
    opacity: 0.8,
  },
  cardContainer: {
    marginBottom: 32,
    minHeight: 120,
    justifyContent: 'center',
  },
  placeholder: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderStyle: 'dashed',
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
  },
  buttonContainer: {
    gap: 12,
  },
  placeholderText: {
    marginBottom: 12,
  },
  changeButton: {
    marginTop: 12,
  },
  suggestionsContainer: {
    width: '100%',
  },
  suggestionsHeader: {
    marginBottom: 8,
    fontWeight: '600',
    opacity: 0.7,
  },
  suggestionItem: {
    marginHorizontal: 0, // Override AirportListItem margin
    marginBottom: 8,
  },
});

function useAirportSuggestions(selectedAirport: Airport | null) {
  const [suggestions, setSuggestions] = useState<Airport[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let mounted = true;

    const fetchSuggestions = async () => {
      // Don't fetch if we already have a selection or suggestions
      if (selectedAirport || suggestions.length > 0) return;

      const granted = await hasLocationPermission();
      if (!granted || !mounted) return;

      setLoading(true);
      try {
        const results = await getNearestAirports(3);
        if (mounted) {
          setSuggestions(results);
        }
      } catch (err) {
        console.warn('Failed to load airport suggestions', err);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    fetchSuggestions();

    return () => { mounted = false; };
  }, [selectedAirport, suggestions.length]); // Added dependencies

  return { suggestions, loading };
}

export function HomeAirportSetupStep({
  selectedAirport,
  onSelectAirport,
  onNext,
  onBack
}: HomeAirportSetupStepProps) {
  const [modalVisible, setModalVisible] = useState(false);
  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);

  const { suggestions, loading } = useAirportSuggestions(selectedAirport);

  const handleOpenModal = useCallback(async () => {
    setModalVisible(true);
    const hasPermission = await hasLocationPermission();
    if (hasPermission) {
      const location = await getCurrentLocation();
      if (location) {
        setUserLocation(location);
      }
    }
  }, []);

  const handleCloseModal = useCallback(() => {
    setModalVisible(false);
  }, []);

  const renderContent = () => {
    if (selectedAirport) {
      return (
        <View>
          <AirportCard airport={selectedAirport} />
          <Button
            title="Change Airport"
            onPress={handleOpenModal}
            variant="secondary"
            size="sm"
            style={styles.changeButton}
          />
        </View>
      );
    }

    if (suggestions.length > 0) {
      return (
        <View style={styles.suggestionsContainer}>
          <ThemedText style={styles.suggestionsHeader}>Suggested nearby:</ThemedText>
          {suggestions.map((airport) => (
            <AirportListItem
              key={airport.icao}
              airport={airport}
              onPress={onSelectAirport}
              style={styles.suggestionItem}
              showDistance={false} // Don't calculate distance again inside item for now
            />
          ))}
          <Button
            title="Search Manually"
            onPress={handleOpenModal}
            variant="ghost"
            size="sm"
            style={{ marginTop: 4 }}
          />
        </View>
      );
    }

    return (
      <View style={styles.placeholder}>
        <ThemedText style={styles.placeholderText}>No airport selected</ThemedText>
        <Button
          title={loading ? "Finding nearest..." : "Select Airport"}
          onPress={handleOpenModal}
          disabled={loading}
        />
      </View>
    );
  };

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title" style={styles.title}>
        Set Your Home Airport
      </ThemedText>
      <ThemedText style={styles.description}>
        Choose your home base and starting point. This location anchors your travel focus sessions.
      </ThemedText>

      <View style={styles.cardContainer}>
        {renderContent()}
      </View>

      <View style={styles.buttonContainer}>
        <Button
          title="Continue"
          onPress={onNext}
          disabled={!selectedAirport}
          size="lg"
        />
        <Button
          title="Back"
          onPress={onBack}
          variant="ghost"
          size="md"
        />
      </View>

      <SelectAirportModal
        visible={modalVisible}
        onClose={handleCloseModal}
        onSelectAirport={onSelectAirport}
        title="Search Home Airport"
        origin={userLocation}
      />
    </ThemedView>
  );
}
