/**
 * DestinationCard component displays an individual destination airport
 * with flight time and distance information.
 *
 * @module components/destinations/DestinationCard
 */

import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors, Spacing, Typography } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { AirportWithFlightTime } from "@/types/radius";
import { formatTimeValue } from "@/utils/timeSlider";
import { impactAsync, ImpactFeedbackStyle } from "expo-haptics";
import { StyleSheet, TouchableOpacity, View } from "react-native";

interface DestinationCardProps {
  /** Airport with flight time information */
  airport: AirportWithFlightTime;
  /** Callback when destination is selected */
  onSelect: (airport: AirportWithFlightTime) => void;
  /** Whether this destination is currently selected */
  isSelected?: boolean;
}

const styles = StyleSheet.create({
  card: {
    padding: Spacing.md,
    borderRadius: 12,
    marginHorizontal: Spacing.md,
    marginVertical: Spacing.sm,
    borderWidth: 2,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    marginBottom: Spacing.xs,
  },
  nameContainer: {
    flex: 1,
    marginRight: Spacing.sm,
  },
  airportName: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: 2,
  },
  cityCountry: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
  },
  codes: {
    alignItems: "flex-end",
  },
  codeText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium,
    opacity: 0.6,
  },
  metadata: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: Spacing.sm,
    paddingTop: Spacing.sm,
    borderTopWidth: 1,
  },
  metadataItem: {
    flex: 1,
  },
  metadataLabel: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium,
    opacity: 0.6,
    textTransform: "uppercase",
    marginBottom: 2,
  },
  metadataValue: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold,
  },
});

/**
 * Displays a destination airport card with flight information.
 *
 * Shows:
 * - Airport name, city, and country
 * - ICAO and IATA codes
 * - Estimated flight time
 * - Distance from origin
 *
 * @example
 * ```tsx
 * <DestinationCard
 *   airport={airportWithTime}
 *   onSelect={(airport) => console.log('Selected:', airport.name)}
 *   isSelected={false}
 * />
 * ```
 */
export function DestinationCard({
  airport,
  onSelect,
  isSelected = false,
}: DestinationCardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];

  const { formatted: flightTimeFormatted } = formatTimeValue(airport.flightTime);
  const distanceFormatted = `${Math.round(airport.distance)} mi`;

  const handlePress = () => {
    impactAsync(ImpactFeedbackStyle.Medium).catch(() => {
      // Ignore haptic errors
    });
    onSelect(airport);
  };

  return (
    <TouchableOpacity onPress={handlePress} activeOpacity={0.7}>
      <ThemedView
        style={[
          styles.card,
          {
            backgroundColor: colors.cardBackground,
            borderColor: isSelected ? colors.tint : colors.border,
          },
        ]}
      >
        <View style={styles.header}>
          <View style={styles.nameContainer}>
            <ThemedText style={styles.airportName}>{airport.name}</ThemedText>
            <ThemedText style={[styles.cityCountry, { color: colors.textSecondary }]}>
              {airport.city}, {airport.country}
            </ThemedText>
          </View>
          <View style={styles.codes}>
            <ThemedText style={[styles.codeText, { color: colors.textSecondary }]}>
              {airport.icao}
            </ThemedText>
            {airport.iata && (
              <ThemedText style={[styles.codeText, { color: colors.textSecondary }]}>
                {airport.iata}
              </ThemedText>
            )}
          </View>
        </View>

        <View
          style={[styles.metadata, { borderTopColor: colors.border }]}
        >
          <View style={styles.metadataItem}>
            <ThemedText style={[styles.metadataLabel, { color: colors.textSecondary }]}>
              Flight Time
            </ThemedText>
            <ThemedText style={styles.metadataValue}>{flightTimeFormatted}</ThemedText>
          </View>
          <View style={styles.metadataItem}>
            <ThemedText style={[styles.metadataLabel, { color: colors.textSecondary }]}>
              Distance
            </ThemedText>
            <ThemedText style={styles.metadataValue}>{distanceFormatted}</ThemedText>
          </View>
        </View>
      </ThemedView>
    </TouchableOpacity>
  );
}
