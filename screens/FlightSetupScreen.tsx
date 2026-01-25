import { AirportCard } from '@/components/airport/AirportCard';
import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useHomeAirport } from '@/hooks/use-home-airport';
import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { IconSymbol } from '@/components/ui/icon-symbol';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    padding: Spacing.lg,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.xl,
  },
  section: {
    marginBottom: Spacing.xl,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: Spacing.sm,
    opacity: 0.7,
  },
  destinationPlaceholder: {
    borderWidth: 1,
    borderStyle: 'dashed',
    borderRadius: 12,
    padding: Spacing.xl,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 120,
  },
  footer: {
    padding: Spacing.lg,
    borderTopWidth: 1,
  },
});

export default function FlightSetupScreen() {
  const { homeAirport } = useHomeAirport();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <View style={styles.content}>
          <View style={styles.header}>
            <ThemedText type="title">Flight Setup</ThemedText>
          </View>

          <View style={styles.section}>
            <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
              Departure (Origin)
            </ThemedText>
            {homeAirport ? (
              <AirportCard airport={homeAirport} showEdit={false} />
            ) : (
              <ThemedText>No Home Base Selected</ThemedText>
            )}
          </View>

          <View style={styles.section}>
            <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
              Destination
            </ThemedText>
            <View style={[styles.destinationPlaceholder, { borderColor: colors.border }]}>
              <IconSymbol name="map" size={32} color={colors.textTertiary} />
              <ThemedText style={{ color: colors.textSecondary, marginTop: Spacing.sm }}>
                Select Destination
              </ThemedText>
              <ThemedText style={{ color: colors.textTertiary, fontSize: Typography.fontSize.xs }}>
                (Coming Soon)
              </ThemedText>
            </View>
          </View>
        </View>

        <View style={[styles.footer, { borderTopColor: colors.border }]}>
          <Button
            title="Start Engine"
            onPress={() => {}}
            size="lg"
            disabled={true}
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}
