import { StyleSheet, View, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface TopBarProps {
  title: string;
  subtitle?: string;
  rightAction?: React.ReactNode;
}

export function TopBar({ title, subtitle, rightAction }: TopBarProps) {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const styles = StyleSheet.create({
    container: {
      paddingTop: insets.top + Spacing.md,
      paddingHorizontal: Spacing.lg,
      paddingBottom: Spacing.lg,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      backgroundColor: colors.background,
    },
    textContainer: {
      flex: 1,
    },
    title: {
      fontSize: Typography.fontSize.xxxl,
      fontWeight: Typography.fontWeight.bold,
      color: colors.text,
      letterSpacing: -1,
    },
    subtitle: {
      fontSize: Typography.fontSize.sm,
      color: colors.textSecondary,
      fontWeight: Typography.fontWeight.medium,
      textTransform: 'uppercase',
      letterSpacing: 0.5,
      marginTop: Spacing.xs,
    },
    actionContainer: {
      marginLeft: Spacing.md,
    }
  });

  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        {subtitle && <ThemedText style={styles.subtitle}>{subtitle}</ThemedText>}
        <ThemedText style={styles.title}>{title}</ThemedText>
      </View>
      {rightAction && (
        <View style={styles.actionContainer}>
          {rightAction}
        </View>
      )}
    </View>
  );
}

export function SettingsButton() {
  const router = useRouter();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <Pressable
      onPress={() => router.push('/settings')}
      style={({ pressed }) => ({
        opacity: pressed ? 0.7 : 1,
        padding: Spacing.xs,
      })}
      accessibilityRole="button"
      accessibilityLabel="Settings"
    >
      <IconSymbol size={28} name="gearshape.fill" color={colors.text} />
    </Pressable>
  );
}
