import { StyleSheet, View } from 'react-native';

import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { ThemedText } from '@/components/themed-text';
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
    marginBottom: Spacing.xl,
  },
  title: {
    textAlign: 'center',
    marginBottom: Spacing.sm,
  },
  description: {
    textAlign: 'center',
  },
});

interface EmptyHomeBaseProps {
  onSelectAirport: () => void;
}

export function EmptyHomeBase({ onSelectAirport }: EmptyHomeBaseProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.container} testID="empty-home-root">
      <View style={styles.iconContainer} testID="empty-home-icon">
        <IconSymbol name="airplane.departure" size={72} color={colors.textTertiary} />
      </View>
      <View style={styles.textContainer}>
        <ThemedText
          testID="empty-home-title"
          style={[
            styles.title,
            {
              fontSize: Typography.fontSize.xxl,
              fontWeight: Typography.fontWeight.bold,
              color: colors.text,
            },
          ]}>
          Set Your Home Base
        </ThemedText>
        <ThemedText
          testID="empty-home-description"
          style={[
            styles.description,
            {
              fontSize: Typography.fontSize.base,
              color: colors.textSecondary,
              lineHeight: Typography.lineHeight.relaxed * Typography.fontSize.base,
            },
          ]}>
          Choose your starting airport to begin your focus journey
        </ThemedText>
      </View>
      <Button title="Select Airport" onPress={onSelectAirport} size="lg" testID="empty-home-select-airport-button" />
    </View>
  );
}
