import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  safeArea: {
    paddingTop: 0,
  },
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
    <SafeAreaView edges={['top']} style={[styles.safeArea, { backgroundColor: colors.surface }]} testID="statusbar-safearea">
      <View style={styles.container} testID="statusbar-container">
        <View style={styles.left} testID="statusbar-left">
          <View style={[styles.dot, { backgroundColor: colors.success }]} testID="statusbar-dot" />
          <ThemedText
            testID="statusbar-label"
          style={{
            fontSize: Typography.fontSize.xs,
            color: colors.textSecondary,
            fontWeight: Typography.fontWeight.medium,
          }}>
          Ready for Departure
        </ThemedText>
      </View>
      <ThemedText
        testID="statusbar-version"
        style={{
          fontSize: Typography.fontSize.xs,
          color: colors.textTertiary,
        }}>
        v0.3.0
      </ThemedText>
      </View>
    </SafeAreaView>
  );
}
