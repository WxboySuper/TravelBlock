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
import type { AirportWithFlightTime } from "@/types/radius";
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
  const cardBorderColor = isSelected ? colors.tint : colors.border;

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
            borderColor: cardBorderColor,
          },
        ]}
      >
        <DestinationHeader airport={airport} compact={compact} textSecondary={colors.textSecondary} />
        <DestinationMetadata
          compact={compact}
          textSecondary={colors.textSecondary}
          borderColor={colors.border}
          flightTimeFormatted={flightTimeFormatted}
          distanceFormatted={distanceFormatted}
        />
      </ThemedView>
    </TouchableOpacity>
  );
}

function DestinationHeader({
  airport,
  compact,
  textSecondary,
}: {
  airport: AirportWithFlightTime;
  compact: boolean;
  textSecondary: string;
}) {
  return (
    <View style={styles.header}>
      <View style={styles.nameContainer}>
        <ThemedText style={[styles.airportName, compact && styles.compactAirportName]}>
          {airport.name}
        </ThemedText>
        <ThemedText
          style={[
            styles.cityCountry,
            compact && styles.compactCityCountry,
            { color: textSecondary },
          ]}
        >
          {airport.city}, {airport.country}
        </ThemedText>
      </View>
      <DestinationCodes airport={airport} textSecondary={textSecondary} />
    </View>
  );
}

function DestinationCodes({
  airport,
  textSecondary,
}: {
  airport: AirportWithFlightTime;
  textSecondary: string;
}) {
  const codeColor = { color: textSecondary };
  const iataCode = airport.iata ? (
    <ThemedText style={[styles.codeText, codeColor]}>{airport.iata}</ThemedText>
  ) : null;

  return (
    <View style={styles.codes}>
      <ThemedText style={[styles.codeText, codeColor]}>{airport.icao}</ThemedText>
      {iataCode}
    </View>
  );
}

function DestinationMetadata({
  compact,
  textSecondary,
  borderColor,
  flightTimeFormatted,
  distanceFormatted,
}: {
  compact: boolean;
  textSecondary: string;
  borderColor: string;
  flightTimeFormatted: string;
  distanceFormatted: string;
}) {
  return (
    <View
      style={[
        styles.metadata,
        compact && styles.compactMetadata,
        { borderTopColor: borderColor },
      ]}
    >
      <MetadataItem
        label="Flight Time"
        value={flightTimeFormatted}
        compact={compact}
        textSecondary={textSecondary}
      />
      <MetadataItem
        label="Distance"
        value={distanceFormatted}
        compact={compact}
        textSecondary={textSecondary}
      />
    </View>
  );
}

function MetadataItem({
  label,
  value,
  compact,
  textSecondary,
}: {
  label: string;
  value: string;
  compact: boolean;
  textSecondary: string;
}) {
  return (
    <View style={styles.metadataItem}>
      <ThemedText style={[styles.metadataLabel, { color: textSecondary }]}>
        {label}
      </ThemedText>
      <ThemedText style={[styles.metadataValue, compact && styles.compactMetadataValue]}>
        {value}
      </ThemedText>
    </View>
  );
}
