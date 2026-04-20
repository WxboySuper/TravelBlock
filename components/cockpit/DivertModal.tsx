/**
 * DivertModal component - modal for selecting divert airport
 * 
 * Displays nearest airports when flight needs to be diverted.
 * Shows distance, estimated time, and allows selection.
 * 
 * @module components/cockpit/DivertModal
 */

import { AppIcon } from '@/components/ui/AppIcon';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/Button';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { DivertOption } from '@/types/flight';
import {
    Modal,
    ScrollView,
    StyleSheet,
    TouchableOpacity,
    View,
    type ViewStyle,
} from 'react-native';

export interface DivertModalProps {
  /** Whether modal is visible */
  visible: boolean;
  /** List of divert airport options */
  divertOptions: DivertOption[];
  /** Divert reason to display */
  reason: string;
  /** Callback when airport is selected */
  onSelect: (option: DivertOption) => void;
  /** Callback when modal is closed */
  onClose: () => void;
  /** Whether options are loading */
  isLoading?: boolean;
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    borderTopLeftRadius: BorderRadius.xl,
    borderTopRightRadius: BorderRadius.xl,
    maxHeight: '80%',
    overflow: 'hidden',
  },
  header: {
    padding: Spacing.lg,
    borderBottomWidth: 1,
  },
  title: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as any,
    marginBottom: Spacing.sm,
  },
  reason: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
  },
  listContainer: {
    padding: Spacing.md,
  },
  airportCard: {
    padding: Spacing.md,
    marginBottom: Spacing.sm,
    borderRadius: BorderRadius.md,
    borderWidth: 1,
  },
  airportHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  airportCode: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold as any,
  },
  airportName: {
    fontSize: Typography.fontSize.sm,
    opacity: 0.7,
    marginBottom: Spacing.xs,
  },
  airportDetails: {
    flexDirection: 'row',
    gap: Spacing.md,
  },
  detail: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
  detailText: {
    fontSize: Typography.fontSize.xs,
    opacity: 0.8,
  },
  emptyText: {
    textAlign: 'center',
    padding: Spacing.xl,
    opacity: 0.7,
  },
  footer: {
    padding: Spacing.lg,
    borderTopWidth: 1,
  },
});

type DivertModalColors = typeof Colors.light;
type DivertOptionCardProps = {
  colors: DivertModalColors;
  index: number;
  onSelect: (option: DivertOption) => void;
  option: DivertOption;
};

/**
 * Format time for display
 */
function formatTime(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  
  return `${minutes}m`;
}

function DivertHeader({ colors, reason }: { colors: DivertModalColors; reason: string }) {
  return (
    <View style={[styles.header, { borderBottomColor: colors.border }]}>
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: Spacing.sm }}>
        <View style={headerIconShell(colors)}>
          <AppIcon color="#FFFFFF" name="warning" size={20} />
        </View>
        <ThemedText style={[styles.title, { color: colors.text }]}>Flight Divert</ThemedText>
      </View>
      <ThemedText style={[styles.reason, { color: colors.cockpitTextSecondary }]}>{reason}</ThemedText>
    </View>
  );
}

function DivertOptionCard({ colors, index, onSelect, option }: DivertOptionCardProps) {
  return (
    <TouchableOpacity
      style={[styles.airportCard, { borderColor: colors.border }]}
      onPress={() => onSelect(option)}
      accessibilityLabel={`Divert to ${option.airport.name}`}
      accessibilityRole="button"
    >
      <View style={styles.airportHeader}>
        <View>
          <ThemedText style={styles.airportCode}>
            {option.airport.iata}
          </ThemedText>
          <ThemedText style={styles.airportName}>
            {option.airport.name}
          </ThemedText>
        </View>
        {index === 0 ? <NearestBadge /> : null}
      </View>

      <View style={styles.airportDetails}>
        <DivertDetail color={colors.cockpitAccent} icon="distance" value={`${option.distanceFromCurrent.toFixed(1)} km`} />
        <DivertDetail color={colors.cockpitAccent} icon="time" value={formatTime(option.estimatedTime)} />
        <DivertDetail color={colors.cockpitAccent} icon="airport" value={option.airport.icao} />
      </View>
    </TouchableOpacity>
  );
}

function DivertDetail({
  color,
  icon,
  value,
}: {
  color: string;
  icon: 'distance' | 'time' | 'airport';
  value: string;
}) {
  return (
    <View style={styles.detail}>
      <AppIcon color={color} name={icon} size={14} />
      <ThemedText style={styles.detailText}>
        {value}
      </ThemedText>
    </View>
  );
}

function NearestBadge() {
  return (
    <View style={nearestBadgeStyle()}>
      <ThemedText
        style={{ color: '#FFFFFF', fontSize: Typography.fontSize.xs }}
      >
        NEAREST
      </ThemedText>
    </View>
  );
}

function DivertBody({
  colors,
  divertOptions,
  isLoading,
  onSelect,
}: {
  colors: DivertModalColors;
  divertOptions: DivertOption[];
  isLoading: boolean;
  onSelect: (option: DivertOption) => void;
}) {
  if (isLoading) {
    return (
      <ScrollView style={styles.listContainer}>
        <ThemedText style={styles.emptyText}>
          Finding nearest airports...
        </ThemedText>
      </ScrollView>
    );
  }

  if (divertOptions.length === 0) {
    return (
      <ScrollView style={styles.listContainer}>
        <ThemedText style={styles.emptyText}>
          No suitable airports found nearby
        </ThemedText>
      </ScrollView>
    );
  }

  return (
    <ScrollView style={styles.listContainer}>
      {divertOptions.map((option, index) => (
        <DivertOptionCard
          key={option.airport.icao}
          colors={colors}
          index={index}
          onSelect={onSelect}
          option={option}
        />
      ))}
    </ScrollView>
  );
}

function DivertFooter({ colors, onClose }: { colors: DivertModalColors; onClose: () => void }) {
  return (
    <View style={[styles.footer, { borderTopColor: colors.border }]}>
      <Button
        title="Cancel"
        onPress={onClose}
        variant="secondary"
      />
    </View>
  );
}

function headerIconShell(colors: DivertModalColors): ViewStyle {
  return {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: colors.cockpitWarning,
    alignItems: 'center',
    justifyContent: 'center',
  };
}

function nearestBadgeStyle(): ViewStyle {
  return {
    backgroundColor: '#10B981',
    paddingHorizontal: Spacing.sm,
    paddingVertical: Spacing.xs,
    borderRadius: 8,
  };
}

/**
 * DivertModal component
 * 
 * Modal dialog for selecting a divert airport from nearest options.
 * 
 * @example
 * ```tsx
 * <DivertModal
 *   visible={showModal}
 *   divertOptions={nearestAirports}
 *   reason="Weather ahead"
 *   onSelect={handleDivert}
 *   onClose={() => setShowModal(false)}
 * />
 * ```
 */
export function DivertModal({
  visible,
  divertOptions,
  reason,
  onSelect,
  onClose,
  isLoading = false,
}: DivertModalProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={onClose}
    >
      <View style={styles.modalOverlay}>
        <ThemedView style={[styles.modalContent, { backgroundColor: colors.cockpitSurface }]}>
          <DivertHeader colors={colors} reason={reason} />
          <DivertBody
            colors={colors}
            divertOptions={divertOptions}
            isLoading={isLoading}
            onSelect={onSelect}
          />
          <DivertFooter colors={colors} onClose={onClose} />
        </ThemedView>
      </View>
    </Modal>
  );
}
