/**
 * MetricCard component - displays a single flight metric
 * 
 * Shows label, value, and unit in a styled card format.
 * Used in the MetricsGrid to display flight data.
 * 
 * @module components/cockpit/MetricCard
 */

import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { StyleSheet, View } from 'react-native';

export interface MetricCardProps {
  /** Metric label (e.g., "Speed", "Altitude") */
  label: string;
  /** Metric value (e.g., "450", "35,000") */
  value: string;
  /** Optional icon to display */
  icon?: string;
  /** Color theme variant */
  variant?: 'default' | 'climbing' | 'cruising' | 'descending';
}

const styles = StyleSheet.create({
  card: {
    flex: 1,
    padding: Spacing.md,
    borderRadius: BorderRadius.md,
    minHeight: 80,
    justifyContent: 'space-between',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  icon: {
    marginRight: Spacing.xs,
    fontSize: Typography.fontSize.base,
  },
  label: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium as any,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    opacity: 0.7,
  },
  value: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as any,
    letterSpacing: -0.5,
  },
});

/**
 * Get color for metric card based on variant
 */
function getVariantColor(variant: MetricCardProps['variant'], isDark: boolean): string {
  switch (variant) {
    case 'climbing':
      return isDark ? '#3B82F6' : '#2563EB'; // Blue
    case 'cruising':
      return isDark ? '#14B8A6' : '#0D9488'; // Teal
    case 'descending':
      return isDark ? '#10B981' : '#059669'; // Green
    default:
      return isDark ? '#374151' : '#F3F4F6'; // Gray
  }
}

/**
 * MetricCard component
 * 
 * Displays a single flight metric in a card format with label and value.
 * 
 * @example
 * ```tsx
 * <MetricCard
 *   label="Speed"
 *   value="450 mph"
 *   icon="speedometer"
 *   variant="cruising"
 * />
 * ```
 */
export function MetricCard({ label, value, icon, variant = 'default' }: MetricCardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const isDark = colorScheme === 'dark';

  const backgroundColor = getVariantColor(variant, isDark);
  const textColor = variant !== 'default' ? '#FFFFFF' : colors.text;

  return (
    <View style={[styles.card, { backgroundColor }]}>
      <View style={styles.header}>
        {icon && <ThemedText style={[styles.icon, { color: textColor }]}>{icon}</ThemedText>}
        <ThemedText style={[styles.label, { color: textColor }]}>
          {label}
        </ThemedText>
      </View>
      <ThemedText style={[styles.value, { color: textColor }]}>
        {value}
      </ThemedText>
    </View>
  );
}
