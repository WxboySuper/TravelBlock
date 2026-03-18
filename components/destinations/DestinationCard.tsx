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
  /** Whether to render the denser flight setup presentation */
  compact?: boolean;
}

const styles = StyleSheet.create({
  card: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm + 2,
    borderRadius: 12,
    marginVertical: Spacing.xs,
    borderWidth: 1.5,
  },
  compactCard: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
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
  compactAirportName: {
    fontSize: Typography.fontSize.sm,
  },
  cityCountry: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
  },
  compactCityCountry: {
    fontSize: Typography.fontSize.xs,
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
  compactMetadata: {
    marginTop: Spacing.xs,
    paddingTop: Spacing.xs,
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
  compactMetadataValue: {
    fontSize: Typography.fontSize.xs,
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
  compact = false,
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
          compact && styles.compactCard,
          {
            backgroundColor: colors.cardBackground,
            borderColor: isSelected ? colors.tint : colors.border,
          },
        ]}
      >
        <View style={styles.header}>
          <View style={styles.nameContainer}>
            <ThemedText style={[styles.airportName, compact && styles.compactAirportName]}>
              {airport.name}
            </ThemedText>
            <ThemedText
              style={[
                styles.cityCountry,
                compact && styles.compactCityCountry,
                { color: colors.textSecondary },
              ]}
            >
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
          style={[
            styles.metadata,
            compact && styles.compactMetadata,
            { borderTopColor: colors.border },
          ]}
        >
          <View style={styles.metadataItem}>
            <ThemedText style={[styles.metadataLabel, { color: colors.textSecondary }]}>
              Flight Time
            </ThemedText>
            <ThemedText style={[styles.metadataValue, compact && styles.compactMetadataValue]}>
              {flightTimeFormatted}
            </ThemedText>
          </View>
          <View style={styles.metadataItem}>
            <ThemedText style={[styles.metadataLabel, { color: colors.textSecondary }]}>
              Distance
            </ThemedText>
            <ThemedText style={[styles.metadataValue, compact && styles.compactMetadataValue]}>
              {distanceFormatted}
            </ThemedText>
          </View>
        </View>
      </ThemedView>
    </TouchableOpacity>
  );
}
