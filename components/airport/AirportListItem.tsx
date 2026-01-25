import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { AirportWithDistance } from '@/types/airport';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useCallback } from 'react';
import { StyleSheet, TouchableOpacity, View, ViewProps } from 'react-native';

const styles = StyleSheet.create({
  container: {
    marginHorizontal: Spacing.lg,
    marginVertical: Spacing.xs,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    borderWidth: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 3,
  },
  touchable: {
    paddingVertical: Spacing.xs,
  },
  contentRow: {
    marginBottom: Spacing.xs,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: Spacing.xs,
  },
  airportCode: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.bold,
  },
  distance: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
  },
  airportName: {
    fontSize: Typography.fontSize.base,
    fontWeight: Typography.fontWeight.semibold,
    marginBottom: Spacing.xs,
  },
  secondaryInfo: {
    fontSize: Typography.fontSize.sm,
  },
});

export interface AirportListItemProps extends ViewProps {
  /** The airport data to display */
  airport: AirportWithDistance;
  /** Called when the item is pressed */
  onPress?: (airport: AirportWithDistance) => void;
  /** Whether to show distance (default: true if distance is available) */
  showDistance?: boolean;
  /** Unit label for distance (e.g., "mi" or "km", default: "mi") */
  distanceUnit?: string;
}

/**
 * List item component that displays airport information in a touchable row.
 *
 * Displays:
 * - ICAO code (left) and distance if available (right)
 * - Airport name
 * - City, country, and optional IATA code
 * - Haptic feedback on press
 * - Theme-aware styling with dark/light mode support
 *
 * @param airport - Airport data with optional calculated distance
 * @param onPress - Callback when item is tapped
 * @param showDistance - Whether to display distance (default: true if available)
 * @param distanceUnit - Unit label for distance (default: "mi")
 * @param props - Additional View props
 * @returns A themed, touchable airport list item
 *
 * @example
 * ```tsx
 * const airport = {
 *   icao: 'KJFK',
 *   iata: 'JFK',
 *   name: 'John F. Kennedy International Airport',
 *   city: 'New York',
 *   country: 'United States',
 *   lat: 40.6413,
 *   lon: -73.7781,
 *   distance: 5.2
 * };
 * <AirportListItem
 *   airport={airport}
 *   onPress={(a) => console.log('Selected:', a)}
 *   distanceUnit="mi"
 * />
 * ```
 */
export function AirportListItem({
  airport,
  onPress,
  showDistance = airport.distance !== undefined,
  distanceUnit = 'mi',
  style,
  ...props
}: AirportListItemProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const handlePress = useCallback(async () => {
    // Trigger haptic feedback on press
    try {
      await impactAsync(ImpactFeedbackStyle.Light);
    } catch (e) {
      // Haptics not available on all platforms; silently fail
    }

    onPress?.(airport);
  }, [airport, onPress]);

  const { icao, iata, name, city, country, distance } = airport;
  const distanceText = showDistance && distance !== undefined ? `${distance.toFixed(1)} ${distanceUnit}` : null;

  return (
    <ThemedView 
      style={[
        styles.container, 
        { 
          backgroundColor: colors.surface,
          borderColor: colors.border,
        }, 
        style
      ]} 
      {...props}>
      <TouchableOpacity
        style={styles.touchable}
        onPress={handlePress}
        activeOpacity={0.6}
        testID={`airport-item-${icao}`}
        accessibilityLabel={`${name}, ${city}, ${country}`}
        accessibilityHint={distanceText ? `Distance: ${distanceText}` : undefined}>
        {/* Header row: ICAO code + distance */}
        <View style={styles.header}>
          <ThemedText style={[styles.airportCode, { color: colors.text }]}>
            {icao}
            {iata && ` · ${iata}`}
          </ThemedText>
          {distanceText && (
            <ThemedText style={[styles.distance, { color: colors.textSecondary }]}>{distanceText}</ThemedText>
          )}
        </View>

        {/* Airport name */}
        <View style={styles.contentRow}>
          <ThemedText style={[styles.airportName, { color: colors.text }]}>{name}</ThemedText>
        </View>

        {/* City and country */}
        <View style={styles.contentRow}>
          <ThemedText style={[styles.secondaryInfo, { color: colors.textSecondary }]}>
            {city}, {country}
          </ThemedText>
        </View>
      </TouchableOpacity>
    </ThemedView>
  );
}
