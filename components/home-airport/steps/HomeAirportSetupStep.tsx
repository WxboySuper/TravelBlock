import { AirportCard } from '@/components/airport/AirportCard';
import { SelectAirportModal } from '@/components/airport/SelectAirportModal';
import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { getNearestAirport, hasLocationPermission } from '@/services/locationService';
import { Airport, AirportWithDistance } from '@/types/airport';
import { useEffect, useState } from 'react';
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
    borderColor: '#ccc', // Will be overridden by theme usually, but keeping simple for now
    borderStyle: 'dashed',
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
  },
  buttonContainer: {
    gap: 12,
  },
});

export function HomeAirportSetupStep({
  selectedAirport,
  onSelectAirport,
  onNext,
  onBack
}: HomeAirportSetupStepProps) {
  const [modalVisible, setModalVisible] = useState(false);
  const [suggesting, setSuggesting] = useState(false);

  // Attempt to suggest nearest airport if permissions granted and none selected
  useEffect(() => {
    let mounted = true;
    async function checkLocationAndSuggest() {
      if (selectedAirport) return;

      const granted = await hasLocationPermission();
      if (!granted) return;

      if (mounted) setSuggesting(true);
      try {
        const nearest = await getNearestAirport();
        if (nearest && mounted && !selectedAirport) {
          onSelectAirport(nearest);
        }
      } catch (err) {
        console.warn('Failed to suggest airport', err);
      } finally {
        if (mounted) setSuggesting(false);
      }
    }
    checkLocationAndSuggest();
    return () => { mounted = false; };
  }, []); // Run once on mount

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title" style={styles.title}>
        Set Your Home Airport
      </ThemedText>
      <ThemedText style={styles.description}>
        Choose your home base. This will be used for quick calculations and default searches.
      </ThemedText>

      <View style={styles.cardContainer}>
        {selectedAirport ? (
          <View>
            <AirportCard airport={selectedAirport} />
            <Button
              title="Change Airport"
              onPress={() => setModalVisible(true)}
              variant="secondary"
              size="sm"
              style={{ marginTop: 12 }}
            />
          </View>
        ) : (
          <View style={styles.placeholder}>
            <ThemedText style={{ marginBottom: 12 }}>No airport selected</ThemedText>
            <Button
              title={suggesting ? "Finding nearest..." : "Select Airport"}
              onPress={() => setModalVisible(true)}
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
        onClose={() => setModalVisible(false)}
        onSelectAirport={onSelectAirport}
        title="Search Home Airport"
      />
    </ThemedView>
  );
}
