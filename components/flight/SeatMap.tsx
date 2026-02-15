/**
 * Seat Map Component
 *
 * Interactive seat selection grid showing aircraft cabin layout.
 * 
 * @module components/flight/SeatMap
 */

import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { ScrollView, StyleSheet, TouchableOpacity, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { AviationColors, Colors, SeatMapConfig, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { AircraftConfig, Seat, SeatClass } from '@/types/flight';

const styles = StyleSheet.create({
  container: {
    paddingVertical: Spacing.md,
  },
  cabinHeader: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: Spacing.md,
    marginBottom: Spacing.sm,
  },
  legendContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    flexWrap: 'wrap',
    gap: Spacing.md,
    paddingHorizontal: Spacing.lg,
    marginBottom: Spacing.lg,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
  legendSeat: {
    width: 24,
    height: 24,
    borderRadius: 4,
    borderWidth: 2,
  },
  legendText: {
    fontSize: Typography.fontSize.xs,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.xs,
  },
  rowLabel: {
    width: SeatMapConfig.rowLabelWidth,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowNumber: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold,
    fontFamily: 'monospace',
  },
  seatsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
  seat: {
    width: SeatMapConfig.seatWidth,
    height: SeatMapConfig.rowHeight,
    borderRadius: 6,
    borderWidth: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  seatLetter: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
  },
  aisle: {
    width: SeatMapConfig.aisleWidth,
  },
  classLabel: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.bold,
    textTransform: 'uppercase',
    letterSpacing: 1,
    paddingVertical: Spacing.md,
    textAlign: 'center',
  },
});

interface SeatMapProps {
  /** Aircraft configuration */
  aircraft: AircraftConfig;
  /** All available seats */
  seats: Seat[];
  /** Currently selected seat */
  selectedSeat: Seat | null;
  /** Callback when seat is selected */
  onSelectSeat: (seat: Seat) => void;
}

export function SeatMap({
  aircraft,
  seats,
  selectedSeat,
  onSelectSeat,
}: SeatMapProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  // Get seat class color
  const getSeatColor = (seat: Seat, isSelected: boolean) => {
    if (isSelected) {
      return AviationColors.seatSelected;
    }

    switch (seat.seatClass) {
      case SeatClass.Business:
        return AviationColors.seatBusiness;
      case SeatClass.PremiumEconomy:
        return AviationColors.seatPremium;
      case SeatClass.Economy:
      default:
        return AviationColors.seatEconomy;
    }
  };

  // Group seats by row
  const seatsByRow: Record<number, Seat[]> = {};
  seats.forEach((seat) => {
    if (!seatsByRow[seat.row]) {
      seatsByRow[seat.row] = [];
    }
    seatsByRow[seat.row].push(seat);
  });

  // Sort rows
  const sortedRows = Object.keys(seatsByRow)
    .map(Number)
    .sort((a, b) => a - b);

  // Handle seat tap
  const handleSeatPress = (seat: Seat) => {
    if (!seat.isAvailable) return;

    impactAsync(ImpactFeedbackStyle.Light).catch(() => {});
    onSelectSeat(seat);
  };

  // Get class label for row
  const getClassLabel = (row: number): string | null => {
    if (aircraft.businessRows && row === aircraft.businessRows[0]) {
      return 'Business Class';
    }
    if (aircraft.premiumRows && row === aircraft.premiumRows[0]) {
      return 'Premium Economy';
    }
    if (aircraft.economyRows && row === aircraft.economyRows[0]) {
      return 'Economy';
    }
    return null;
  };

  // Render aisle based on seat config
  const renderSeatsWithAisles = (rowSeats: Seat[]) => {
    const elements: React.ReactElement[] = [];
    let seatIndex = 0;

    aircraft.seatConfig.forEach((sectionSize, configIndex) => {
      // Add seats for this section
      for (let i = 0; i < sectionSize; i++) {
        const seat = rowSeats[seatIndex];
        if (!seat) break;

        const isSelected =
          selectedSeat?.row === seat.row && selectedSeat?.letter === seat.letter;
        const seatColor = getSeatColor(seat, isSelected);

        elements.push(
          <TouchableOpacity
            key={`${seat.row}${seat.letter}`}
            style={[
              styles.seat,
              {
                backgroundColor: isSelected ? seatColor : colors.surface,
                borderColor: seatColor,
                opacity: seat.isAvailable ? 1 : 0.3,
              },
            ]}
            onPress={() => handleSeatPress(seat)}
            disabled={!seat.isAvailable}
            accessibilityLabel={`Seat ${seat.row}${seat.letter}, ${seat.seatClass}`}
            accessibilityRole="button"
          >
            <ThemedText
              style={[
                styles.seatLetter,
                { color: isSelected ? '#FFFFFF' : colors.text },
              ]}
            >
              {seat.letter}
            </ThemedText>
          </TouchableOpacity>
        );

        seatIndex++;
      }

      // Add aisle after sections (except the last one)
      if (configIndex < aircraft.seatConfig.length - 1) {
        elements.push(<View key={`aisle-${configIndex}`} style={styles.aisle} />);
      }
    });

    return elements;
  };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Legend */}
      <View style={styles.legendContainer}>
        {aircraft.businessRows && (
          <View style={styles.legendItem}>
            <View
              style={[
                styles.legendSeat,
                {
                  backgroundColor: colors.surface,
                  borderColor: AviationColors.seatBusiness,
                },
              ]}
            />
            <ThemedText style={[styles.legendText, { color: colors.textSecondary }]}>
              Business
            </ThemedText>
          </View>
        )}
        {aircraft.premiumRows && (
          <View style={styles.legendItem}>
            <View
              style={[
                styles.legendSeat,
                {
                  backgroundColor: colors.surface,
                  borderColor: AviationColors.seatPremium,
                },
              ]}
            />
            <ThemedText style={[styles.legendText, { color: colors.textSecondary }]}>
              Premium
            </ThemedText>
          </View>
        )}
        <View style={styles.legendItem}>
          <View
            style={[
              styles.legendSeat,
              {
                backgroundColor: colors.surface,
                borderColor: AviationColors.seatEconomy,
              },
            ]}
          />
          <ThemedText style={[styles.legendText, { color: colors.textSecondary }]}>
            Economy
          </ThemedText>
        </View>
        <View style={styles.legendItem}>
          <View
            style={[
              styles.legendSeat,
              {
                backgroundColor: AviationColors.seatSelected,
                borderColor: AviationColors.seatSelected,
              },
            ]}
          />
          <ThemedText style={[styles.legendText, { color: colors.textSecondary }]}>
            Selected
          </ThemedText>
        </View>
      </View>

      {/* Seat rows */}
      {sortedRows.map((rowNumber) => {
        const rowSeats = seatsByRow[rowNumber].sort((a, b) =>
          a.letter.localeCompare(b.letter)
        );
        const classLabel = getClassLabel(rowNumber);

        return (
          <View key={rowNumber}>
            {/* Class label */}
            {classLabel && (
              <ThemedText
                style={[styles.classLabel, { color: colors.primary }]}
              >
                {classLabel}
              </ThemedText>
            )}

            {/* Row */}
            <View style={styles.row}>
              {/* Row number */}
              <View style={styles.rowLabel}>
                <ThemedText
                  style={[styles.rowNumber, { color: colors.textSecondary }]}
                >
                  {rowNumber}
                </ThemedText>
              </View>

              {/* Seats with aisles */}
              <View style={styles.seatsContainer}>
                {renderSeatsWithAisles(rowSeats)}
              </View>

              {/* Row number (right side) */}
              <View style={styles.rowLabel}>
                <ThemedText
                  style={[styles.rowNumber, { color: colors.textSecondary }]}
                >
                  {rowNumber}
                </ThemedText>
              </View>
            </View>
          </View>
        );
      })}
    </ScrollView>
  );
}
