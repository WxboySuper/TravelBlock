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

  return (
    <View style={[
      styles.container, 
      { 
        paddingTop: insets.top + Spacing.md, 
        backgroundColor: colors.background 
      }
    ]}>
      <View style={styles.textContainer}>
        <ThemedText style={[styles.title, { color: colors.text }]}>{title}</ThemedText>
        {subtitle && <ThemedText style={[styles.subtitle, { color: colors.textSecondary }]}>{subtitle}</ThemedText>}
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

  const handlePress = () => {
    router.push('/settings');
  };

  return (
    <Pressable
      onPress={handlePress}
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

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  textContainer: {
    flex: 1,
  },
  title: {
    fontSize: Typography.fontSize.xxxl,
    fontWeight: Typography.fontWeight.bold,
    letterSpacing: -1,
  },
  subtitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginTop: Spacing.xs,
  },
  actionContainer: {
    marginLeft: Spacing.md,
  }
});
