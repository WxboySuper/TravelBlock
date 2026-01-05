import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { useThemeColor } from '@/hooks/use-theme-color';
import type { AirportWithDistance } from '@/types/airport';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useCallback } from 'react';
import {
  StyleSheet,
  TouchableOpacity,
  View,
  ViewProps,
} from 'react-native';

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
  },
  touchable: {
    paddingVertical: 4,
  },
  contentRow: {
    marginBottom: 4,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 2,
  },
  airportCode: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  distance: {
    fontSize: 13,
    fontWeight: '500',
  },
  airportName: {
    fontSize: 15,
    fontWeight: '600',
    marginBottom: 2,
  },
  secondaryInfo: {
    fontSize: 13,
    opacity: 0.7,
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
  showDistance = airport.distance !== undefined,  distanceUnit = 'mi',
  style,
  ...props
}: AirportListItemProps) {
  const borderColor = useThemeColor(
    { light: '#e0e0e0', dark: '#444' },
    'text'
  );

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
  const distanceText = showDistance && distance !== undefined
    ? `${distance.toFixed(1)} ${distanceUnit}`
    : null;

  return (
    <ThemedView
      style={[
        styles.container,
        { borderBottomColor: borderColor },
        style,
      ]}
      {...props}
    >
      <TouchableOpacity
        style={styles.touchable}
        onPress={handlePress}
        activeOpacity={0.6}
        testID={`airport-item-${icao}`}
        accessibilityLabel={`${name}, ${city}, ${country}`}
        accessibilityHint={distanceText ? `Distance: ${distanceText}` : undefined}
      >
        {/* Header row: ICAO code + distance */}
        <View style={styles.header}>
          <ThemedText type="defaultSemiBold" style={styles.airportCode}>
            {icao}
            {iata && ` · ${iata}`}
          </ThemedText>
          {distanceText && (
            <ThemedText type="defaultSemiBold" style={styles.distance}>
              {distanceText}
            </ThemedText>
          )}
        </View>

        {/* Airport name */}
        <View style={styles.contentRow}>
          <ThemedText type="defaultSemiBold" style={styles.airportName}>
            {name}
          </ThemedText>
        </View>

        {/* City and country */}
        <View style={styles.contentRow}>
          <ThemedText style={styles.secondaryInfo}>
            {city}, {country}
          </ThemedText>
        </View>
      </TouchableOpacity>
    </ThemedView>
  );
}
