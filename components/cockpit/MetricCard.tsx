/**
 * MetricCard component - displays a single flight metric
 * 
 * Shows label, value, and unit in a styled card format.
 * Used in the MetricsGrid to display flight data.
 * 
 * @module components/cockpit/MetricCard
 */

import { AppIcon, type AppIconName } from '@/components/ui/AppIcon';
import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { StyleSheet, View, type TextStyle } from 'react-native';

export interface MetricCardProps {
  /** Metric label (e.g., "Speed", "Altitude") */
  label: string;
  /** Metric value (e.g., "450", "35,000") */
  value: string;
  /** Optional icon to display */
  icon?: AppIconName;
  /** Color theme variant */
  variant?: 'default' | 'climbing' | 'cruising' | 'descending';
  /** Optional supporting copy for larger cards */
  description?: string;
  /** Optional compact status tag */
  emphasisLabel?: string;
  /** Whether this card should span the row */
  fullWidth?: boolean;
}

const styles = StyleSheet.create({
  card: {
    flex: 1,
    padding: Spacing.md,
    borderRadius: BorderRadius.lg,
    minHeight: 96,
    justifyContent: 'space-between',
    borderWidth: 1,
  },
  fullWidthCard: {
    flex: 0,
    marginBottom: Spacing.md,
    minHeight: 118,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  headerContent: {
    flex: 1,
  },
  iconWrap: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: Spacing.sm,
  },
  label: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium as TextStyle['fontWeight'],
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    opacity: 0.7,
  },
  value: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as TextStyle['fontWeight'],
    letterSpacing: -0.5,
  },
  emphasisPill: {
    borderRadius: BorderRadius.full,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  emphasisText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold as TextStyle['fontWeight'],
    letterSpacing: 0.4,
    textTransform: 'uppercase',
  },
  description: {
    fontSize: Typography.fontSize.sm,
    lineHeight: 20,
    marginTop: Spacing.sm,
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
export function MetricCard({
  description,
  emphasisLabel,
  fullWidth = false,
  label,
  value,
  icon,
  variant = 'default',
}: MetricCardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const isDark = colorScheme === 'dark';

  const backgroundColor = getVariantColor(variant, isDark);
  const textColor = variant !== 'default' ? '#FFFFFF' : colors.text;
  const borderColor = variant !== 'default' ? 'rgba(255,255,255,0.14)' : colors.cockpitBorder;
  const emphasisBackground =
    variant !== 'default' ? 'rgba(255,255,255,0.16)' : colors.cockpitAccentSoft;
  const emphasisColor = variant !== 'default' ? '#FFFFFF' : colors.cockpitAccent;

  return (
    <View style={[styles.card, fullWidth ? styles.fullWidthCard : null, { backgroundColor, borderColor }]}>
      <View style={styles.header}>
        {icon ? (
          <View
            style={[
              styles.iconWrap,
              { backgroundColor: variant !== 'default' ? 'rgba(255,255,255,0.14)' : colors.cockpitAccentSoft },
            ]}
          >
            <AppIcon color={variant !== 'default' ? '#FFFFFF' : colors.cockpitAccent} name={icon} size={16} />
          </View>
        ) : null}
        <View style={styles.headerContent}>
          <ThemedText style={[styles.label, { color: textColor }]}>
            {label}
          </ThemedText>
        </View>
        {emphasisLabel ? (
          <View style={[styles.emphasisPill, { backgroundColor: emphasisBackground }]}>
            <ThemedText style={[styles.emphasisText, { color: emphasisColor }]}>
              {emphasisLabel}
            </ThemedText>
          </View>
        ) : null}
      </View>
      <ThemedText style={[styles.value, { color: textColor }]}>
        {value}
      </ThemedText>
      {description ? (
        <ThemedText style={[styles.description, { color: variant !== 'default' ? 'rgba(255,255,255,0.84)' : colors.cockpitTextSecondary }]}>
          {description}
        </ThemedText>
      ) : null}
    </View>
  );
}
