import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: Spacing.xl,
    paddingBottom: 100,
  },
  iconContainer: {
    marginBottom: Spacing.lg,
  },
  textContainer: {
    alignItems: 'center',
  },
  title: {
    textAlign: 'center',
    marginBottom: Spacing.sm,
  },
  description: {
    textAlign: 'center',
  },
});

export function EmptyLogbook() {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.container}>
      <View style={styles.iconContainer}>
        <IconSymbol name="book.fill" size={72} color={colors.textTertiary} />
      </View>
      <View style={styles.textContainer}>
        <ThemedText
          style={[
            styles.title,
            {
              fontSize: Typography.fontSize.xxl,
              fontWeight: Typography.fontWeight.bold,
              color: colors.text,
            },
          ]}>
          No Flights Logged
        </ThemedText>
        <ThemedText
          style={[
            styles.description,
            {
              fontSize: Typography.fontSize.base,
              color: colors.textSecondary,
              lineHeight: Typography.lineHeight.relaxed * Typography.fontSize.base,
            },
          ]}>
          Complete focus sessions to see your flight history here
        </ThemedText>
      </View>
    </View>
  );
}
