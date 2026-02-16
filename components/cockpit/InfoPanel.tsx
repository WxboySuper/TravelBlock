/**
 * InfoPanel component - displays static flight information
 * 
 * Shows flight details like origin, destination, flight number,
 * aircraft type, gate, etc.
 * 
 * @module components/cockpit/InfoPanel
 */

import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { FlightBooking } from '@/types/flight';
import { StyleSheet, View } from 'react-native';

export interface InfoPanelProps {
  /** Flight booking data */
  booking: FlightBooking;
}

const styles = StyleSheet.create({
  container: {
    padding: Spacing.md,
  },
  section: {
    marginBottom: Spacing.lg,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as any,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: Spacing.sm,
    opacity: 0.7,
  },
  card: {
    padding: Spacing.md,
    borderRadius: BorderRadius.md,
    borderWidth: 1,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: Spacing.xs,
  },
  label: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
  },
  value: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium as any,
  },
  routeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.md,
  },
  routeCode: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as any,
  },
  routeArrow: {
    fontSize: Typography.fontSize.lg,
    marginHorizontal: Spacing.md,
    opacity: 0.5,
  },
  routeName: {
    fontSize: Typography.fontSize.xs,
    textAlign: 'center',
    opacity: 0.7,
    marginTop: Spacing.xs,
  },
});

/**
 * Format date/time for display
 */
function formatDateTime(isoString: string): string {
  const date = new Date(isoString);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * InfoPanel component
 * 
 * Displays static flight information in a clean, organized layout.
 * 
 * @example
 * ```tsx
 * <InfoPanel booking={flightBooking} />
 * ```
 */
export function InfoPanel({ booking }: InfoPanelProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.container}>
      {/* Route */}
      <View style={styles.section}>
        <ThemedText style={styles.sectionTitle}>Route</ThemedText>
        
        <View style={styles.routeContainer}>
          <View style={{ alignItems: 'center' }}>
            <ThemedText style={styles.routeCode}>
              {booking.origin.iata}
            </ThemedText>
            <ThemedText style={styles.routeName}>
              {booking.origin.city}
            </ThemedText>
          </View>
          
          <ThemedText style={styles.routeArrow}>→</ThemedText>
          
          <View style={{ alignItems: 'center' }}>
            <ThemedText style={styles.routeCode}>
              {booking.destination.iata}
            </ThemedText>
            <ThemedText style={styles.routeName}>
              {booking.destination.city}
            </ThemedText>
          </View>
        </View>
      </View>

      {/* Flight Details */}
      <View style={styles.section}>
        <ThemedText style={styles.sectionTitle}>Flight Details</ThemedText>
        
        <View style={[styles.card, { borderColor: colors.border }]}>
          <View style={styles.row}>
            <ThemedText style={styles.label}>Flight Number</ThemedText>
            <ThemedText style={styles.value}>{booking.flightNumber}</ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Aircraft</ThemedText>
            <ThemedText style={styles.value}>{booking.aircraft.name}</ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Distance</ThemedText>
            <ThemedText style={styles.value}>
              {Math.round(booking.distanceKm * 0.621371)} mi
            </ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Gate</ThemedText>
            <ThemedText style={styles.value}>{booking.gate}</ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Terminal</ThemedText>
            <ThemedText style={styles.value}>{booking.terminal}</ThemedText>
          </View>
        </View>
      </View>

      {/* Times */}
      <View style={styles.section}>
        <ThemedText style={styles.sectionTitle}>Schedule</ThemedText>
        
        <View style={[styles.card, { borderColor: colors.border }]}>
          <View style={styles.row}>
            <ThemedText style={styles.label}>Departure</ThemedText>
            <ThemedText style={styles.value}>
              {formatDateTime(booking.departureTime)}
            </ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Arrival</ThemedText>
            <ThemedText style={styles.value}>
              {formatDateTime(booking.arrivalTime)}
            </ThemedText>
          </View>
          
          <View style={styles.row}>
            <ThemedText style={styles.label}>Boarding</ThemedText>
            <ThemedText style={styles.value}>
              {formatDateTime(booking.boardingTime)}
            </ThemedText>
          </View>
        </View>
      </View>
    </View>
  );
}
