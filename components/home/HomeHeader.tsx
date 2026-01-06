import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  container: {
    paddingTop: Spacing.xxl,
    paddingBottom: Spacing.lg,
  },
  greeting: {
    marginBottom: Spacing.xs,
  },
});

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good Morning';
  if (hour < 17) return 'Good Afternoon';
  return 'Good Evening';
}

export function HomeHeader() {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const greeting = getGreeting();

  return (
    <View style={styles.container} testID="home-header-container">
      <ThemedText
        testID="home-header-greeting"
        style={[
          styles.greeting,
          {
            fontSize: Typography.fontSize.sm,
            color: colors.textSecondary,
            fontWeight: Typography.fontWeight.medium,
            textTransform: 'uppercase',
            letterSpacing: 0.5,
          },
        ]}>
        {greeting}
      </ThemedText>
      <ThemedText
        testID="home-header-title"
        style={{
          fontSize: Typography.fontSize.xxxl,
          fontWeight: Typography.fontWeight.bold,
          color: colors.text,
          letterSpacing: -1,
        }}>
        TravelBlock
      </ThemedText>
    </View>
  );
}
