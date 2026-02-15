/**
 * DestinationsList component displays a virtualized list of destination airports
 * filtered by flight time.
 *
 * @module components/destinations/DestinationsList
 */

import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { IconSymbol } from "@/components/ui/icon-symbol";
import { Colors, Spacing, Typography } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { AirportWithFlightTime } from "@/types/radius";
import { ActivityIndicator, FlatList, StyleSheet, View } from "react-native";
import { DestinationCard } from "./DestinationCard";

interface DestinationsListProps {
  /** List of destination airports */
  destinations: AirportWithFlightTime[];
  /** Currently selected destination */
  selectedDestination: AirportWithFlightTime | null;
  /** Callback when destination is selected */
  onSelectDestination: (airport: AirportWithFlightTime) => void;
  /** Whether data is loading */
  isLoading?: boolean;
  /** Error message if data loading failed */
  error?: string | null;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  listContent: {
    paddingVertical: Spacing.sm,
  },
  header: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderBottomWidth: 1,
  },
  headerText: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
    textTransform: "uppercase",
    opacity: 0.7,
  },
  emptyContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: Spacing.xl * 2,
    paddingHorizontal: Spacing.xl,
  },
  emptyIcon: {
    marginBottom: Spacing.md,
    opacity: 0.3,
  },
  emptyTitle: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.sm,
    textAlign: "center",
  },
  emptyMessage: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
    textAlign: "center",
    maxWidth: 280,
  },
  loadingContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: Spacing.xl * 2,
  },
  errorContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: Spacing.xl * 2,
    paddingHorizontal: Spacing.xl,
  },
  errorText: {
    fontSize: Typography.fontSize.sm,
    color: "#ef4444",
    textAlign: "center",
  },
});

/**
 * Renders a virtualized list of destination airports with flight information.
 *
 * Features:
 * - Optimized FlatList for smooth scrolling performance
 * - Loading and error states
 * - Empty state when no destinations match
 * - Destination selection with visual feedback
 *
 * @example
 * ```tsx
 * const [selected, setSelected] = useState(null);
 * const { destinations, isLoading } = useDestinations(origin, flightTime);
 *
 * <DestinationsList
 *   destinations={destinations}
 *   selectedDestination={selected}
 *   onSelectDestination={setSelected}
 *   isLoading={isLoading}
 * />
 * ```
 */
export function DestinationsList({
  destinations,
  selectedDestination,
  onSelectDestination,
  isLoading = false,
  error = null,
}: DestinationsListProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];

  // Loading state
  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.tint} />
        <ThemedText style={{ marginTop: Spacing.md, opacity: 0.7 }}>
          Finding destinations...
        </ThemedText>
      </View>
    );
  }

  // Error state
  if (error) {
    return (
      <View style={styles.errorContainer}>
        <IconSymbol name="exclamationmark.triangle" size={48} color="#ef4444" />
        <ThemedText style={styles.errorText}>{error}</ThemedText>
      </View>
    );
  }

  // Empty state
  if (destinations.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <IconSymbol
          name="airplane"
          size={64}
          color={colors.textTertiary}
          style={styles.emptyIcon}
        />
        <ThemedText style={styles.emptyTitle}>No Destinations Found</ThemedText>
        <ThemedText style={[styles.emptyMessage, { color: colors.textSecondary }]}>
          Try adjusting the flight duration to find reachable airports
        </ThemedText>
      </View>
    );
  }

  // Render list
  return (
    <ThemedView style={styles.container}>
      <View style={[styles.header, { borderBottomColor: colors.border }]}>
        <ThemedText style={[styles.headerText, { color: colors.textSecondary }]}>
          {destinations.length} Available Destination{destinations.length !== 1 ? "s" : ""}
        </ThemedText>
      </View>

      <FlatList
        data={destinations}
        keyExtractor={(item) => item.icao}
        renderItem={({ item }) => (
          <DestinationCard
            airport={item}
            onSelect={onSelectDestination}
            isSelected={selectedDestination?.icao === item.icao}
          />
        )}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={true}
        removeClippedSubviews={true}
        maxToRenderPerBatch={10}
        windowSize={10}
        initialNumToRender={10}
      />
    </ThemedView>
  );
}
