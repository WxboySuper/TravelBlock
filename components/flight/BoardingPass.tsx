/**
 * Boarding Pass Component
 *
 * Displays a realistic airline boarding pass with tear/scan interaction.
 *
 * @module components/flight/BoardingPass
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { StyleSheet, TouchableOpacity, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { BoardingPassDimensions, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { BoardingPass as BoardingPassType } from '@/types/flight';

const styles = StyleSheet.create({
  passContainer: {
    width: BoardingPassDimensions.width,
    height: BoardingPassDimensions.height,
    borderRadius: BoardingPassDimensions.cornerRadius,
    overflow: 'hidden',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  passContent: {
    flex: 1,
    padding: Spacing.lg,
  },
  header: {
    paddingBottom: Spacing.md,
    borderBottomWidth: 2,
    borderStyle: 'dashed',
    marginBottom: Spacing.md,
  },
  airline: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold,
    marginBottom: Spacing.xs,
  },
  flightNumber: {
    fontSize: Typography.fontSize.base,
    fontFamily: 'monospace',
  },
  routeSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: Spacing.lg,
    paddingVertical: Spacing.md,
  },
  airportBlock: {
    flex: 1,
  },
  airportCode: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    marginBottom: Spacing.xs,
  },
  airportName: {
    fontSize: Typography.fontSize.xs,
  },
  arrow: {
    paddingHorizontal: Spacing.md,
    justifyContent: 'center',
  },
  arrowText: {
    fontSize: Typography.fontSize.xl,
  },
  detailsGrid: {
    marginBottom: Spacing.lg,
  },
  detailRow: {
    flexDirection: 'row',
    marginBottom: Spacing.md,
  },
  detailBlock: {
    flex: 1,
  },
  detailLabel: {
    fontSize: Typography.fontSize.xs,
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: Spacing.xs,
  },
  detailValue: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold,
  },
  perforationLine: {
    position: 'absolute',
    left: 0,
    right: 0,
    height: 2,
    borderStyle: 'dashed',
    borderWidth: 1,
  },
  stubSection: {
    paddingTop: Spacing.lg,
  },
  qrPlaceholder: {
    width: 120,
    height: 120,
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    alignSelf: 'center',
    marginBottom: Spacing.md,
    justifyContent: 'center',
    alignItems: 'center',
  },
  qrText: {
    fontSize: Typography.fontSize.xs,
    textAlign: 'center',
  },
  serialNumber: {
    fontSize: Typography.fontSize.xs,
    fontFamily: 'monospace',
    textAlign: 'center',
  },
  passengerName: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold,
    textTransform: 'uppercase',
    letterSpacing: 2,
  },
});

interface BoardingPassProps {
  /** Boarding pass data */
  boardingPass: BoardingPassType;
  /** Whether the pass can be interacted with */
  interactive?: boolean;
  /** Callback when pass is tapped (for scan interaction) */
  onTap?: () => void;
}

export function BoardingPass({
  boardingPass,
  interactive = false,
  onTap,
}: BoardingPassProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  // Pass uses dedicated boarding color scheme
  const passColors = {
    background: '#1E3A8A', // Deep blue
    text: '#FFFFFF',
    textSecondary: '#93C5FD', // Light blue
    border: '#3B82F6', // Medium blue
  };

  const { booking, seat, passengerName, serialNumber } = boardingPass;
  const originCode = booking.origin.iata || booking.origin.icao;
  const destCode = booking.destination.iata || booking.destination.icao;

  // Format times (simple formatting for now)
  const boardingTime = new Date(booking.boardingTime).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
  const departureTime = new Date(booking.departureTime).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const handleTap = () => {
    if (!interactive || !onTap) return;
    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => {});
    onTap();
  };

  return (
    <TouchableOpacity
      style={[styles.passContainer, { backgroundColor: passColors.background }]}
      onPress={handleTap}
      disabled={!interactive}
      activeOpacity={interactive ? 0.8 : 1}
    >
      <View style={styles.passContent}>
        {/* Header */}
        <View style={[styles.header, { borderBottomColor: passColors.border }]}>
          <ThemedText style={[styles.airline, { color: passColors.text }]}>
            TravelBlock Airlines
          </ThemedText>
          <ThemedText style={[styles.flightNumber, { color: passColors.textSecondary }]}>
            {booking.flightNumber}
          </ThemedText>
        </View>

        {/* Route */}
        <View style={styles.routeSection}>
          <View style={styles.airportBlock}>
            <ThemedText style={[styles.airportCode, { color: passColors.text }]}>
              {originCode}
            </ThemedText>
            <ThemedText style={[styles.airportName, { color: passColors.textSecondary }]}>
              {booking.origin.city}
            </ThemedText>
          </View>

          <View style={styles.arrow}>
            <ThemedText style={[styles.arrowText, { color: passColors.textSecondary }]}>
              →
            </ThemedText>
          </View>

          <View style={[styles.airportBlock, { alignItems: 'flex-end' }]}>
            <ThemedText style={[styles.airportCode, { color: passColors.text }]}>
              {destCode}
            </ThemedText>
            <ThemedText style={[styles.airportName, { color: passColors.textSecondary }]}>
              {booking.destination.city}
            </ThemedText>
          </View>
        </View>

        {/* Passenger name */}
        <View style={{ marginBottom: Spacing.lg }}>
          <ThemedText style={[styles.detailLabel, { color: passColors.textSecondary }]}>
            Passenger
          </ThemedText>
          <ThemedText style={[styles.passengerName, { color: passColors.text }]}>
            {passengerName}
          </ThemedText>
        </View>

        {/* Flight details grid */}
        <View style={styles.detailsGrid}>
          {/* Row 1 */}
          <View style={styles.detailRow}>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, { color: passColors.textSecondary }]}>
                Boarding
              </ThemedText>
              <ThemedText style={[styles.detailValue, { color: passColors.text }]}>
                {boardingTime}
              </ThemedText>
            </View>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, { color: passColors.textSecondary }]}>
                Departure
              </ThemedText>
              <ThemedText style={[styles.detailValue, { color: passColors.text }]}>
                {departureTime}
              </ThemedText>
            </View>
          </View>

          {/* Row 2 */}
          <View style={styles.detailRow}>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, { color: passColors.textSecondary }]}>
                Gate
              </ThemedText>
              <ThemedText style={[styles.detailValue, { color: passColors.text }]}>
                {booking.gate}
              </ThemedText>
            </View>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, { color: passColors.textSecondary }]}>
                Seat
              </ThemedText>
              <ThemedText style={[styles.detailValue, { color: passColors.text }]}>
                {seat.row}
                {seat.letter}
              </ThemedText>
            </View>
          </View>
        </View>

        {/* Perforation line */}
        <View
          style={[
            styles.perforationLine,
            {
              top: BoardingPassDimensions.perforationY,
              borderColor: passColors.border,
            },
          ]}
        />

        {/* Stub section (below perforation) */}
        <View style={[styles.stubSection, { position: 'absolute', top: BoardingPassDimensions.perforationY + 16, left: Spacing.lg, right: Spacing.lg }]}>
          {/* QR Code placeholder */}
          <View style={styles.qrPlaceholder}>
            <ThemedText style={[styles.qrText, { color: '#000' }]}>
              QR CODE{'\n'}
              {serialNumber}
            </ThemedText>
          </View>

          {/* Booking reference */}
          <ThemedText style={[styles.serialNumber, { color: passColors.textSecondary }]}>
            {booking.bookingReference}
          </ThemedText>
        </View>
      </View>
    </TouchableOpacity>
  );
}
