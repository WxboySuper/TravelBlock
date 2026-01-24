import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { StyleSheet, View, Image } from 'react-native';

interface WelcomeStepProps {
  onNext: () => void;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  content: {
    alignItems: 'center',
    marginBottom: 48,
  },
  title: {
    marginTop: 24,
    marginBottom: 12,
    textAlign: 'center',
  },
  description: {
    textAlign: 'center',
    marginBottom: 24,
    opacity: 0.8,
  },
  buttonContainer: {
    width: '100%',
  },
});

export function WelcomeStep({ onNext }: WelcomeStepProps) {
  return (
    <ThemedView style={styles.container}>
      <View style={styles.content}>
        <ThemedText type="title" style={styles.title}>
          Welcome to TravelBlock
        </ThemedText>
        <ThemedText style={styles.description}>
          Your personal aviation companion. Let's get you set up in just a few steps.
        </ThemedText>
      </View>
      <View style={styles.buttonContainer}>
        <Button title="Get Started" onPress={onNext} size="lg" />
      </View>
    </ThemedView>
  );
}
