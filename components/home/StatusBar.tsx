import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: Spacing.sm,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'center',
  },
});

export function StatusBar() {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={[styles.container, { backgroundColor: colors.surface }]}>
      <View style={styles.left}>
        <View style={[styles.dot, { backgroundColor: colors.success }]} />
        <ThemedText
          style={{
            fontSize: Typography.fontSize.xs,
            color: colors.textSecondary,
            fontWeight: Typography.fontWeight.medium,
          }}>
          Ready for Departure
        </ThemedText>
      </View>
      <ThemedText
        style={{
          fontSize: Typography.fontSize.xs,
          color: colors.textTertiary,
        }}>
        v0.3.0
      </ThemedText>
    </View>
  );
}
