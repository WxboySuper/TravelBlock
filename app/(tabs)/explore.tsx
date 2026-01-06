import { StyleSheet, View } from 'react-native';

import { EmptyLogbook } from '@/components/logbook/EmptyLogbook';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    padding: Spacing.lg,
  },
  header: {
    paddingTop: Spacing.xxl,
    paddingBottom: Spacing.lg,
  },
  subtitle: {
    marginTop: Spacing.xs,
  },
});

export default function ExploreScreen() {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <ThemedView style={styles.container}>
      <View style={styles.contentContainer}>
        <View style={styles.header}>
          <ThemedText
            style={{
              fontSize: Typography.fontSize.xxxl,
              fontWeight: Typography.fontWeight.bold,
              color: colors.text,
              letterSpacing: -1,
            }}>
            Logbook
          </ThemedText>
          <ThemedText
            style={[
              styles.subtitle,
              {
                fontSize: Typography.fontSize.base,
                color: colors.textSecondary,
              },
            ]}>
            Your Flight History
          </ThemedText>
        </View>

        <EmptyLogbook />
      </View>
    </ThemedView>
  );
}
