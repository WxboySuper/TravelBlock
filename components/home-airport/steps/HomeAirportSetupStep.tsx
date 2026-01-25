import { AirportCard } from '@/components/airport/AirportCard';
import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { getNearestAirport, hasLocationPermission } from '@/services/locationService';
import { Airport, AirportWithDistance } from '@/types/airport';
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
    minHeight: 120, // Reserve space for the card or placeholder
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
});

function useAirportSuggestion(
  selectedAirport: Airport | null,
  onSelectAirport: (airport: Airport | AirportWithDistance) => void
) {
  const [suggesting, setSuggesting] = useState(false);

  useEffect(() => {
    let mounted = true;

    // Split logic to reduce complexity
    const suggest = async () => {
      const granted = await hasLocationPermission();
      if (!granted) return;
      if (!mounted) return;

      setSuggesting(true);
      try {
        const nearest = await getNearestAirport();
        // Check mount status again after async call
        if (!mounted) return;

        if (nearest && !selectedAirport) {
          onSelectAirport(nearest);
        }
      } catch (err) {
        console.warn('Failed to suggest airport', err);
      } finally {
        if (mounted) setSuggesting(false);
      }
    };

    if (!selectedAirport) {
      suggest();
    }

    return () => { mounted = false; };
  }, []); // Only run on mount

  return suggesting;
}

export function HomeAirportSetupStep({
  selectedAirport,
  onSelectAirport,
  onNext,
  onBack
}: HomeAirportSetupStepProps) {
  const [modalVisible, setModalVisible] = useState(false);

  const suggesting = useAirportSuggestion(selectedAirport, onSelectAirport);

  const handleOpenModal = useCallback(() => {
    setModalVisible(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setModalVisible(false);
  }, []);

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title" style={styles.title}>
        Set Your Home Airport
      </ThemedText>
      <ThemedText style={styles.description}>
        Choose your home base and starting point. This location anchors your travel focus sessions.
      </ThemedText>

      <View style={styles.cardContainer}>
        {selectedAirport ? (
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
        ) : (
          <View style={styles.placeholder}>
            <ThemedText style={styles.placeholderText}>No airport selected</ThemedText>
            <Button
              title={suggesting ? "Finding nearest..." : "Select Airport"}
              onPress={handleOpenModal}
              disabled={suggesting}
            />
          </View>
        )}
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
      />
    </ThemedView>
  );
}
