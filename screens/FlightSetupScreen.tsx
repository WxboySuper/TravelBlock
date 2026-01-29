import { AirportCard } from '@/components/airport/AirportCard';
import { Button } from '@/components/ui/Button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useHomeAirport } from '@/hooks/useHomeAirport';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useCallback } from 'react';
import type { Airport } from '@/types/airport';
import { useRouter } from 'expo-router';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';

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
  closeButton: {
    padding: Spacing.sm,
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

function SectionHeader({ title }: { title: string }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <ThemedText style={[styles.sectionTitle, { color: colors.textSecondary }]}>
      {title}
    </ThemedText>
  );
}

function DepartureSection({ homeAirport }: { homeAirport: Airport | null }) {
  return (
    <View style={styles.section}>
      <SectionHeader title="Departure (Origin)" />
      {homeAirport ? (
        <AirportCard airport={homeAirport} showEdit={false} />
      ) : (
        <ThemedText>No Home Base Selected</ThemedText>
      )}
    </View>
  );
}

function DestinationSection() {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.section}>
      <SectionHeader title="Destination" />
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
  );
}

function FlightSetupHeader({ onClose }: { onClose: () => void }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.header}>
      <ThemedText type="title">Flight Setup</ThemedText>
      <TouchableOpacity
        onPress={onClose}
        style={styles.closeButton}
        accessibilityLabel="Close flight setup"
        accessibilityRole="button"
        hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
      >
        <IconSymbol name="xmark" size={24} color={colors.text} />
      </TouchableOpacity>
    </View>
  );
}

function FlightSetupFooter({ onStart }: { onStart: () => void }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={[styles.footer, { borderTopColor: colors.border }]}>
      <Button
        title="Start Engine"
        onPress={onStart}
        size="lg"
        disabled
      />
    </View>
  );
}

function FlightSetupContent({
  homeAirport,
  onClose
}: {
  homeAirport: Airport | null;
  onClose: () => void;
}) {
  return (
    <View style={styles.content}>
      <FlightSetupHeader onClose={onClose} />
      <DepartureSection homeAirport={homeAirport} />
      <DestinationSection />
    </View>
  );
}

export default function FlightSetupScreen() {
  const { homeAirport } = useHomeAirport();
  const router = useRouter();

  const handleStartEngine = useCallback(() => {
    // Logic to start the flight will go here
  }, []);

  const handleClose = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {
      // Ignore haptics error
    });
    router.back();
  }, [router]);

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={{ flex: 1 }}>
        <FlightSetupContent homeAirport={homeAirport} onClose={handleClose} />
        <FlightSetupFooter onStart={handleStartEngine} />
      </SafeAreaView>
    </ThemedView>
  );
}
