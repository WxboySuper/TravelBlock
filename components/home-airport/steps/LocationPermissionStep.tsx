import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { requestLocationPermission } from '@/services/locationService';
import { useCallback, useState } from 'react';
import { StyleSheet, View } from 'react-native';

interface LocationPermissionStepProps {
  onNext: () => void;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 24,
  },
  title: {
    marginBottom: 16,
    textAlign: 'center',
  },
  description: {
    marginBottom: 32,
    textAlign: 'center',
    opacity: 0.8,
  },
  buttonContainer: {
    gap: 12,
  },
});

export function LocationPermissionStep({ onNext }: LocationPermissionStepProps) {
  const [requesting, setRequesting] = useState(false);

  const handleEnableLocation = useCallback(async () => {
    setRequesting(true);
    try {
      await requestLocationPermission();
      // Proceed whether granted or not - the user has made their choice
      onNext();
    } catch (error) {
      console.error('Error requesting location permission', error);
      onNext();
    } finally {
      setRequesting(false);
    }
  }, [onNext]);

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title" style={styles.title}>
        Enable Location Services
      </ThemedText>
      <ThemedText style={styles.description}>
        We use your location to find the nearest airports and calculate distances.
        This improves your experience when searching for airports.
      </ThemedText>

      <View style={styles.buttonContainer}>
        <Button
          title={requesting ? "Requesting..." : "Enable Location"}
          onPress={handleEnableLocation}
          size="lg"
          disabled={requesting}
        />
        <Button
          title="Skip"
          onPress={onNext}
          variant="ghost"
          size="md"
          disabled={requesting}
        />
      </View>
    </ThemedView>
  );
}
