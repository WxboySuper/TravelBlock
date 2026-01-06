import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    paddingTop: 60,
  },
  header: {
    marginBottom: 40,
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
  },
});

export default function ExploreScreen() {
  return (
    <ThemedView style={styles.container}>
      <View style={styles.header}>
        <ThemedText type="title">Logbook</ThemedText>
        <ThemedText type="subtitle">Your Flight History</ThemedText>
      </View>
      
      <View style={styles.emptyState}>
        <IconSymbol name="book.fill" size={64} color="#687076" style={{ marginBottom: 16 }} />
        <ThemedText type="subtitle">No Flights Logged</ThemedText>
        <ThemedText style={styles.emptyText}>
          Complete focus sessions to see your flight history here.
        </ThemedText>
      </View>
    </ThemedView>
  );
}
