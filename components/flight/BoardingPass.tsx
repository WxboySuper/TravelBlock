/**
 * Boarding Pass Component
 *
 * Displays a realistic airline boarding pass with tear/scan interaction.
 *
 * @module components/flight/BoardingPass
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useMemo } from 'react';
import { StyleSheet, TouchableOpacity, View, useWindowDimensions } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { BoardingPassDimensions, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { BoardingPass as BoardingPassType } from '@/types/flight';

const styles = StyleSheet.create({
  passContainer: {
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
    width: 128,
    height: 128,
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
    lineHeight: 18,
    paddingHorizontal: Spacing.sm,
  },
  serialNumber: {
    fontSize: Typography.fontSize.xs,
    fontFamily: 'monospace',
    textAlign: 'center',
  },
  qrNote: {
    fontSize: Typography.fontSize.xs,
    textAlign: 'center',
    marginTop: Spacing.xs,
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
  const { width: screenWidth } = useWindowDimensions();

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
  const cardWidth = Math.min(screenWidth - Spacing.xl * 2, BoardingPassDimensions.width);
  const scale = cardWidth / BoardingPassDimensions.width;
  const cardHeight = BoardingPassDimensions.height * scale;
  const perforationTop = BoardingPassDimensions.perforationY * scale;
  const stubTop = perforationTop + 16 * scale;
  const scaledStyles = useMemo(
    () => ({
      airline: { fontSize: Typography.fontSize.xl * scale },
      flightNumber: { fontSize: Typography.fontSize.base * scale },
      airportCode: { fontSize: Typography.fontSize.xxxl * scale },
      airportName: { fontSize: Math.max(11, Typography.fontSize.xs * scale) },
      arrowText: { fontSize: Typography.fontSize.xl * scale },
      detailLabel: { fontSize: Math.max(10, Typography.fontSize.xs * scale) },
      detailValue: { fontSize: Typography.fontSize.lg * scale },
      passengerName: {
        fontSize: Typography.fontSize.lg * scale,
        letterSpacing: Math.max(1, 2 * scale),
      },
      qrPlaceholder: {
        width: 128 * scale,
        height: 128 * scale,
      },
      qrText: {
        fontSize: Math.max(10, Typography.fontSize.xs * scale),
        lineHeight: Math.max(14, 18 * scale),
      },
      serialNumber: { fontSize: Math.max(10, Typography.fontSize.xs * scale) },
      qrNote: { fontSize: Math.max(10, Typography.fontSize.xs * scale) },
      passPadding: { padding: Spacing.lg * scale },
      headerSpacing: { paddingBottom: Spacing.md * scale, marginBottom: Spacing.md * scale },
      routeSpacing: { marginBottom: Spacing.lg * scale, paddingVertical: Spacing.md * scale },
      detailRow: { marginBottom: Spacing.md * scale },
      stubSection: { left: Spacing.lg * scale, right: Spacing.lg * scale },
    }),
    [scale]
  );

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
      style={[
        styles.passContainer,
        {
          backgroundColor: passColors.background,
          width: cardWidth,
          height: cardHeight,
        },
      ]}
      onPress={handleTap}
      disabled={!interactive}
      activeOpacity={interactive ? 0.8 : 1}
    >
      <View style={[styles.passContent, scaledStyles.passPadding]}>
        {/* Header */}
        <View
          style={[
            styles.header,
            scaledStyles.headerSpacing,
            { borderBottomColor: passColors.border },
          ]}
        >
          <ThemedText style={[styles.airline, scaledStyles.airline, { color: passColors.text }]}>
            TravelBlock Airlines
          </ThemedText>
          <ThemedText
            style={[styles.flightNumber, scaledStyles.flightNumber, { color: passColors.textSecondary }]}
          >
            {booking.flightNumber}
          </ThemedText>
        </View>

        {/* Route */}
        <View style={[styles.routeSection, scaledStyles.routeSpacing]}>
          <View style={styles.airportBlock}>
            <ThemedText style={[styles.airportCode, scaledStyles.airportCode, { color: passColors.text }]}>
              {originCode}
            </ThemedText>
            <ThemedText style={[styles.airportName, scaledStyles.airportName, { color: passColors.textSecondary }]}>
              {booking.origin.city}
            </ThemedText>
          </View>

          <View style={styles.arrow}>
            <ThemedText style={[styles.arrowText, scaledStyles.arrowText, { color: passColors.textSecondary }]}>
              →
            </ThemedText>
          </View>

          <View style={[styles.airportBlock, { alignItems: 'flex-end' }]}>
            <ThemedText style={[styles.airportCode, scaledStyles.airportCode, { color: passColors.text }]}>
              {destCode}
            </ThemedText>
            <ThemedText style={[styles.airportName, scaledStyles.airportName, { color: passColors.textSecondary }]}>
              {booking.destination.city}
            </ThemedText>
          </View>
        </View>

        {/* Passenger name */}
        <View style={{ marginBottom: Spacing.lg * scale }}>
          <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: passColors.textSecondary }]}>
            Passenger
          </ThemedText>
          <ThemedText style={[styles.passengerName, scaledStyles.passengerName, { color: passColors.text }]}>
            {passengerName}
          </ThemedText>
        </View>

        {/* Flight details grid */}
        <View style={styles.detailsGrid}>
          {/* Row 1 */}
          <View style={[styles.detailRow, scaledStyles.detailRow]}>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: passColors.textSecondary }]}>
                Boarding
              </ThemedText>
              <ThemedText style={[styles.detailValue, scaledStyles.detailValue, { color: passColors.text }]}>
                {boardingTime}
              </ThemedText>
            </View>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: passColors.textSecondary }]}>
                Departure
              </ThemedText>
              <ThemedText style={[styles.detailValue, scaledStyles.detailValue, { color: passColors.text }]}>
                {departureTime}
              </ThemedText>
            </View>
          </View>

          {/* Row 2 */}
          <View style={[styles.detailRow, scaledStyles.detailRow]}>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: passColors.textSecondary }]}>
                Gate
              </ThemedText>
              <ThemedText style={[styles.detailValue, scaledStyles.detailValue, { color: passColors.text }]}>
                {booking.gate}
              </ThemedText>
            </View>
            <View style={styles.detailBlock}>
              <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: passColors.textSecondary }]}>
                Seat
              </ThemedText>
              <ThemedText style={[styles.detailValue, scaledStyles.detailValue, { color: passColors.text }]}>
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
              top: perforationTop,
              borderColor: passColors.border,
            },
          ]}
        />

        {/* Stub section (below perforation) */}
        <View
          style={[
            styles.stubSection,
            scaledStyles.stubSection,
            { position: 'absolute', top: stubTop },
          ]}
        >
          <View style={[styles.qrPlaceholder, scaledStyles.qrPlaceholder]}>
            <ThemedText style={[styles.qrText, scaledStyles.qrText, { color: '#000000' }]}>
              BOARDING CODE{'\n'}
              {booking.bookingReference}
            </ThemedText>
          </View>
          <ThemedText style={[styles.qrNote, scaledStyles.qrNote, { color: passColors.textSecondary }]}>
            QR rendering is not wired up yet in this build.
          </ThemedText>

          {/* Booking reference */}
          <ThemedText style={[styles.serialNumber, scaledStyles.serialNumber, { color: passColors.textSecondary }]}>
            {serialNumber}
          </ThemedText>
        </View>
      </View>
    </TouchableOpacity>
  );
}
