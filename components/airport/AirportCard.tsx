import { useCallback } from 'react';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';

import { Card } from '@/components/ui/Card';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { Airport } from '@/types/airport';

interface AirportCardProps {
  airport: Airport;
  onEdit?: () => void;
  onClear?: () => void;
  showEdit?: boolean;
}

const styles = StyleSheet.create({
  card: {
    padding: Spacing.lg,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 6,
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
    marginTop: Spacing.md,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  clearButton: {
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.sm,
    borderWidth: 1,
  },
  clearButtonText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
  },
});

function AirportCardHeader({ onEdit, showEdit }: { onEdit?: () => void; showEdit: boolean }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.header}>
      <View style={styles.headerLeft}>
        <View style={[styles.iconBadge, { backgroundColor: `${colors.primary}1A` }]}>
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
        <TouchableOpacity
          onPress={onEdit}
          style={styles.editButton}
          hitSlop={8}
          activeOpacity={0.6}
          accessibilityLabel="Edit airport"
          testID="airport-card-edit-button">
          <IconSymbol name="pencil" size={18} color={colors.icon} />
        </TouchableOpacity>
      )}
    </View>
  );
}

function AirportCardFooter({
  city,
  country,
  state,
  elevation,
  onClear,
}: {
  city: string;
  country: string;
  state?: string;
  elevation?: number;
  onClear?: () => void;
}) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={[styles.footer, { borderTopColor: colors.borderLight }]}>
      <View style={styles.locationRow}>
        <IconSymbol name="location.fill" size={14} color={colors.textTertiary} />
        <ThemedText
          style={{
            fontSize: Typography.fontSize.sm,
            color: colors.textSecondary,
            marginLeft: Spacing.xs,
            flex: 1,
          }}>
          {city}, {state || country}
        </ThemedText>
        {elevation !== undefined && (
          <ThemedText
            style={{
              fontSize: Typography.fontSize.xs,
              color: colors.textTertiary,
              marginRight: Spacing.md,
            }}>
            {elevation} ft
          </ThemedText>
        )}
      </View>
      {onClear && (
        <TouchableOpacity
          onPress={onClear}
          style={[styles.clearButton, { borderColor: colors.border, backgroundColor: colors.surface }]}
          hitSlop={8}
          activeOpacity={0.6}
          accessibilityLabel="Clear home base"
          testID="airport-card-clear-button">
          <ThemedText style={[styles.clearButtonText, { color: colors.error }]}>Clear</ThemedText>
        </TouchableOpacity>
      )}
    </View>
  );
}

export function AirportCard({ airport, onEdit, onClear, showEdit = true }: AirportCardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const handleEdit = useCallback(() => {
    if (!onEdit) return;
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {
      // Ignore if haptics are not available
    });
    onEdit();
  }, [onEdit]);

  const handleClear = useCallback(() => {
    if (!onClear) return;
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {
      // Ignore if haptics are not available
    });
    onClear();
  }, [onClear]);

  return (
    <Card variant="elevated" style={styles.card}>
      <AirportCardHeader onEdit={onEdit ? handleEdit : undefined} showEdit={showEdit} />

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

      <AirportCardFooter
        city={airport.city}
        country={airport.country}
        state={airport.state}
        elevation={airport.elevation}
        onClear={onClear ? handleClear : undefined}
      />
    </Card>
  );
}
