import { useMemo } from 'react';
import { StyleSheet, View, ViewProps } from 'react-native';

import { BorderRadius, Colors, Spacing } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface CardProps extends ViewProps {
  variant?: 'default' | 'elevated';
  testID?: string;
}

const styles = StyleSheet.create({
  card: {
    borderRadius: BorderRadius.md,
    borderWidth: 1,
    padding: Spacing.md,
  },
});

export function Card({ style, variant = 'default', children, testID, ...props }: CardProps): React.JSX.Element {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const combinedStyles = useMemo(() => {
    const base = [styles.card, { backgroundColor: colors.cardBackground, borderColor: colors.cardBorder }];
    if (variant === 'elevated') {
      base.push({
        shadowColor: colors.cardShadow,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 1,
        shadowRadius: 8,
        elevation: 2,
      } as any);
    }
    if (style) base.push(style as any);
    return base;
  }, [variant, colors, style]);

  return (
    <View style={combinedStyles} testID={testID ?? 'card'} {...props}>
      {children}
    </View>
  );
}
