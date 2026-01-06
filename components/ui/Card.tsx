import { StyleSheet, View, ViewProps } from 'react-native';

import { BorderRadius, Colors, Spacing } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface CardProps extends ViewProps {
  variant?: 'default' | 'elevated';
}

const styles = StyleSheet.create({
  card: {
    borderRadius: BorderRadius.md,
    borderWidth: 1,
    padding: Spacing.md,
  },
});

export function Card({ style, variant = 'default', children, ...props }: CardProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor: colors.cardBackground,
          borderColor: colors.cardBorder,
        },
        variant === 'elevated' && {
          shadowColor: colors.cardShadow,
          shadowOffset: { width: 0, height: 2 },
          shadowOpacity: 1,
          shadowRadius: 8,
          elevation: 2,
        },
        style,
      ]}
      {...props}>
      {children}
    </View>
  );
}
