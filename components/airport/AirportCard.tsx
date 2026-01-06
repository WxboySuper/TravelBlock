import { Pressable, StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { Airport } from '@/types/airport';

interface AirportCardProps {
  airport: Airport;
  onEdit?: () => void;
  showEdit?: boolean;
}

const styles = StyleSheet.create({
  card: {
    padding: Spacing.lg,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.md,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  iconBadge: {
    width: 32,
    height: 32,
    borderRadius: BorderRadius.sm,
    alignItems: 'center',
    justifyContent: 'center',
  },
  editButton: {
    padding: Spacing.sm,
  },
  mainContent: {
    marginBottom: Spacing.md,
  },
  codeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  iataBadge: {
    paddingHorizontal: Spacing.sm,
    paddingVertical: 2,
    borderRadius: BorderRadius.sm,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: Spacing.md,
    borderTopWidth: 1,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
});

export function AirportCard({ airport, onEdit, showEdit = true }: AirportCardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <Card variant="elevated" style={styles.card}>
      {/* Header - "Home Base" label */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <View style={[styles.iconBadge, { backgroundColor: `${colors.primary}15` }]}>
            <IconSymbol name="house.fill" size={20} color={colors.primary} />
          </View>
          <ThemedText
            style={{
              fontSize: Typography.fontSize.sm,
              fontWeight: Typography.fontWeight.medium,
              color: colors.textSecondary,
              letterSpacing: 0.5,
              textTransform: 'uppercase',
            }}>
            Home Base
          </ThemedText>
        </View>
        {showEdit && onEdit && (
          <Pressable onPress={onEdit} style={styles.editButton} hitSlop={8}>
            <IconSymbol name="pencil" size={18} color={colors.icon} />
          </Pressable>
        )}
      </View>

      {/* Airport Code and Name */}
      <View style={styles.mainContent}>
        <View style={styles.codeContainer}>
          <ThemedText
            style={{
              fontSize: Typography.fontSize.xxxl,
              fontWeight: Typography.fontWeight.bold,
              letterSpacing: -0.5,
              color: colors.text,
            }}>
            {airport.icao}
          </ThemedText>
          {airport.iata && (
            <View style={[styles.iataBadge, { backgroundColor: colors.surface }]}>
              <ThemedText
                style={{
                  fontSize: Typography.fontSize.xs,
                  fontWeight: Typography.fontWeight.semibold,
                  color: colors.textSecondary,
                }}>
                {airport.iata}
              </ThemedText>
            </View>
          )}
        </View>

        <ThemedText
          style={{
            fontSize: Typography.fontSize.lg,
            fontWeight: Typography.fontWeight.medium,
            color: colors.text,
            marginTop: Spacing.xs,
          }}>
          {airport.name}
        </ThemedText>
      </View>

      {/* Location Details */}
      <View style={[styles.footer, { borderTopColor: colors.borderLight }]}>
        <View style={styles.locationRow}>
          <IconSymbol name="location.fill" size={14} color={colors.textTertiary} />
          <ThemedText
            style={{
              fontSize: Typography.fontSize.sm,
              color: colors.textSecondary,
              marginLeft: Spacing.xs,
            }}>
            {airport.city}, {airport.state || airport.country}
          </ThemedText>
        </View>
        {airport.elevation !== undefined && (
          <ThemedText
            style={{
              fontSize: Typography.fontSize.xs,
              color: colors.textTertiary,
            }}>
            {airport.elevation} ft
          </ThemedText>
        )}
      </View>
    </Card>
  );
}
