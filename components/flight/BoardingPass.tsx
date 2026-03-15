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

const flexOne = { flex: 1 } as const;
const alignEnd = { alignItems: 'flex-end' } as const;

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
  passengerSection: {
    marginBottom: Spacing.lg,
  },
  passengerName: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold,
    textTransform: 'uppercase',
    letterSpacing: 2,
  },
});

interface BoardingPassProps {
  boardingPass: BoardingPassType;
  interactive?: boolean;
  onTap?: () => void;
}

interface PassColors {
  background: string;
  text: string;
  textSecondary: string;
  border: string;
}

interface ScaledStyles {
  airline: { fontSize: number };
  flightNumber: { fontSize: number };
  airportCode: { fontSize: number };
  airportName: { fontSize: number };
  arrowText: { fontSize: number };
  detailLabel: { fontSize: number };
  detailValue: { fontSize: number };
  passengerSection: { marginBottom: number };
  passengerName: { fontSize: number; letterSpacing: number };
  qrPlaceholder: { width: number; height: number };
  qrText: { fontSize: number; lineHeight: number };
  serialNumber: { fontSize: number };
  qrNote: { fontSize: number };
  passPadding: { padding: number };
  headerSpacing: { paddingBottom: number; marginBottom: number };
  routeSpacing: { marginBottom: number; paddingVertical: number };
  detailRow: { marginBottom: number };
  stubSection: { left: number; right: number };
}

interface DetailPairProps {
  colors: PassColors;
  label: string;
  scaledStyles: ScaledStyles;
  value: string;
}

function DetailPair({ colors, label, scaledStyles, value }: DetailPairProps) {
  return (
    <View style={flexOne}>
      <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: colors.textSecondary }]}>
        {label}
      </ThemedText>
      <ThemedText style={[styles.detailValue, scaledStyles.detailValue, { color: colors.text }]}>
        {value}
      </ThemedText>
    </View>
  );
}

interface BoardingPassHeaderProps {
  colors: PassColors;
  flightNumber: string;
  scaledStyles: ScaledStyles;
}

function BoardingPassHeader({ colors, flightNumber, scaledStyles }: BoardingPassHeaderProps) {
  return (
    <View
      style={[
        styles.header,
        scaledStyles.headerSpacing,
        { borderBottomColor: colors.border },
      ]}
    >
      <ThemedText style={[styles.airline, scaledStyles.airline, { color: colors.text }]}>
        TravelBlock Airlines
      </ThemedText>
      <ThemedText
        style={[styles.flightNumber, scaledStyles.flightNumber, { color: colors.textSecondary }]}
      >
        {flightNumber}
      </ThemedText>
    </View>
  );
}

interface RouteSectionProps {
  colors: PassColors;
  destinationCity: string;
  destinationCode: string;
  originCity: string;
  originCode: string;
  scaledStyles: ScaledStyles;
}

function RouteSection({
  colors,
  destinationCity,
  destinationCode,
  originCity,
  originCode,
  scaledStyles,
}: RouteSectionProps) {
  return (
    <View style={[styles.routeSection, scaledStyles.routeSpacing]}>
      <View style={flexOne}>
        <ThemedText style={[styles.airportCode, scaledStyles.airportCode, { color: colors.text }]}>
          {originCode}
        </ThemedText>
        <ThemedText style={[styles.airportName, scaledStyles.airportName, { color: colors.textSecondary }]}>
          {originCity}
        </ThemedText>
      </View>
      <View style={styles.arrow}>
        <ThemedText style={[styles.arrowText, scaledStyles.arrowText, { color: colors.textSecondary }]}>
          →
        </ThemedText>
      </View>
      <View style={[flexOne, alignEnd]}>
        <ThemedText style={[styles.airportCode, scaledStyles.airportCode, { color: colors.text }]}>
          {destinationCode}
        </ThemedText>
        <ThemedText style={[styles.airportName, scaledStyles.airportName, { color: colors.textSecondary }]}>
          {destinationCity}
        </ThemedText>
      </View>
    </View>
  );
}

interface PassengerSectionProps {
  colors: PassColors;
  passengerName: string;
  scaledStyles: ScaledStyles;
}

function PassengerSection({ colors, passengerName, scaledStyles }: PassengerSectionProps) {
  return (
    <View style={[styles.passengerSection, scaledStyles.passengerSection]}>
      <ThemedText style={[styles.detailLabel, scaledStyles.detailLabel, { color: colors.textSecondary }]}>
        Passenger
      </ThemedText>
      <ThemedText style={[styles.passengerName, scaledStyles.passengerName, { color: colors.text }]}>
        {passengerName}
      </ThemedText>
    </View>
  );
}

interface DetailsGridProps {
  boardingTime: string;
  colors: PassColors;
  departureTime: string;
  gate: string;
  scaledStyles: ScaledStyles;
  seatLabel: string;
}

function DetailsGrid({
  boardingTime,
  colors,
  departureTime,
  gate,
  scaledStyles,
  seatLabel,
}: DetailsGridProps) {
  return (
    <View style={styles.detailsGrid}>
      <View style={[styles.detailRow, scaledStyles.detailRow]}>
        <DetailPair colors={colors} label="Boarding" scaledStyles={scaledStyles} value={boardingTime} />
        <DetailPair colors={colors} label="Departure" scaledStyles={scaledStyles} value={departureTime} />
      </View>
      <View style={[styles.detailRow, scaledStyles.detailRow]}>
        <DetailPair colors={colors} label="Gate" scaledStyles={scaledStyles} value={gate} />
        <DetailPair colors={colors} label="Seat" scaledStyles={scaledStyles} value={seatLabel} />
      </View>
    </View>
  );
}

interface StubSectionProps {
  bookingReference: string;
  colors: PassColors;
  scaledStyles: ScaledStyles;
  serialNumber: string;
  stubTop: number;
}

function StubSection({
  bookingReference,
  colors,
  scaledStyles,
  serialNumber,
  stubTop,
}: StubSectionProps) {
  return (
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
          {bookingReference}
        </ThemedText>
      </View>
      <ThemedText style={[styles.qrNote, scaledStyles.qrNote, { color: colors.textSecondary }]}>
        QR rendering is not wired up yet in this build.
      </ThemedText>
      <ThemedText style={[styles.serialNumber, scaledStyles.serialNumber, { color: colors.textSecondary }]}>
        {serialNumber}
      </ThemedText>
    </View>
  );
}

export function BoardingPass({
  boardingPass,
  interactive = false,
  onTap,
}: BoardingPassProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const { width: screenWidth } = useWindowDimensions();
  const passColors: PassColors = {
    background: '#1E3A8A',
    text: '#FFFFFF',
    textSecondary: '#93C5FD',
    border: '#3B82F6',
  };

  const { booking, seat, passengerName, serialNumber } = boardingPass;
  const originCode = booking.origin.iata || booking.origin.icao;
  const destinationCode = booking.destination.iata || booking.destination.icao;
  const cardWidth = Math.min(screenWidth - Spacing.xl * 2, BoardingPassDimensions.width);
  const scale = cardWidth / BoardingPassDimensions.width;
  const cardHeight = BoardingPassDimensions.height * scale;
  const perforationTop = BoardingPassDimensions.perforationY * scale;
  const stubTop = perforationTop + 16 * scale;
  const seatLabel = `${seat.row}${seat.letter}`;
  const scaledStyles = useMemo<ScaledStyles>(
    () => ({
      airline: { fontSize: Typography.fontSize.xl * scale },
      flightNumber: { fontSize: Typography.fontSize.base * scale },
      airportCode: { fontSize: Typography.fontSize.xxxl * scale },
      airportName: { fontSize: Math.max(11, Typography.fontSize.xs * scale) },
      arrowText: { fontSize: Typography.fontSize.xl * scale },
      detailLabel: { fontSize: Math.max(10, Typography.fontSize.xs * scale) },
      detailValue: { fontSize: Typography.fontSize.lg * scale },
      passengerSection: { marginBottom: Spacing.lg * scale },
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
    impactAsync(ImpactFeedbackStyle.Heavy).catch(() => undefined);
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
        <BoardingPassHeader
          colors={passColors}
          flightNumber={booking.flightNumber}
          scaledStyles={scaledStyles}
        />
        <RouteSection
          colors={passColors}
          destinationCity={booking.destination.city}
          destinationCode={destinationCode}
          originCity={booking.origin.city}
          originCode={originCode}
          scaledStyles={scaledStyles}
        />
        <PassengerSection
          colors={passColors}
          passengerName={passengerName}
          scaledStyles={scaledStyles}
        />
        <DetailsGrid
          boardingTime={boardingTime}
          colors={passColors}
          departureTime={departureTime}
          gate={booking.gate}
          scaledStyles={scaledStyles}
          seatLabel={seatLabel}
        />
        <View
          style={[
            styles.perforationLine,
            {
              top: perforationTop,
              borderColor: passColors.border,
            },
          ]}
        />
        <StubSection
          bookingReference={booking.bookingReference}
          colors={passColors}
          scaledStyles={scaledStyles}
          serialNumber={serialNumber}
          stubTop={stubTop}
        />
      </View>
    </TouchableOpacity>
  );
}
