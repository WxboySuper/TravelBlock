/**
 * DivertModal component - modal for selecting divert airport
 * 
 * Displays nearest airports when flight needs to be diverted.
 * Shows distance, estimated time, and allows selection.
 * 
 * @module components/cockpit/DivertModal
 */

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
  detailIcon: {
    fontSize: Typography.fontSize.sm,
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
        <ThemedView style={styles.modalContent}>
          {/* Header */}
          <View style={[styles.header, { borderBottomColor: colors.border }]}>
            <ThemedText style={styles.title}>⚠️ Flight Divert</ThemedText>
            <ThemedText style={styles.reason}>{reason}</ThemedText>
          </View>

          {/* Airport list */}
          <ScrollView style={styles.listContainer}>
            {isLoading ? (
              <ThemedText style={styles.emptyText}>
                Finding nearest airports...
              </ThemedText>
            ) : divertOptions.length === 0 ? (
              <ThemedText style={styles.emptyText}>
                No suitable airports found nearby
              </ThemedText>
            ) : (
              divertOptions.map((option, index) => (
                <TouchableOpacity
                  key={option.airport.icao}
                  style={[
                    styles.airportCard,
                    { borderColor: colors.border },
                  ]}
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
                    {index === 0 && (
                      <View
                        style={{
                          backgroundColor: '#10B981',
                          paddingHorizontal: Spacing.sm,
                          paddingVertical: Spacing.xs,
                          borderRadius: 8,
                        }}
                      >
                        <ThemedText
                          style={{ color: '#FFFFFF', fontSize: Typography.fontSize.xs }}
                        >
                          NEAREST
                        </ThemedText>
                      </View>
                    )}
                  </View>

                  <View style={styles.airportDetails}>
                    <View style={styles.detail}>
                      <ThemedText style={styles.detailIcon}>📍</ThemedText>
                      <ThemedText style={styles.detailText}>
                        {option.distanceFromCurrent.toFixed(1)} km
                      </ThemedText>
                    </View>
                    <View style={styles.detail}>
                      <ThemedText style={styles.detailIcon}>⏱️</ThemedText>
                      <ThemedText style={styles.detailText}>
                        {formatTime(option.estimatedTime)}
                      </ThemedText>
                    </View>
                    <View style={styles.detail}>
                      <ThemedText style={styles.detailIcon}>🌐</ThemedText>
                      <ThemedText style={styles.detailText}>
                        {option.airport.icao}
                      </ThemedText>
                    </View>
                  </View>
                </TouchableOpacity>
              ))
            )}
          </ScrollView>

          {/* Footer */}
          <View style={[styles.footer, { borderTopColor: colors.border }]}>
            <Button
              title="Cancel"
              onPress={onClose}
              variant="secondary"
            />
          </View>
        </ThemedView>
      </View>
    </Modal>
  );
}
