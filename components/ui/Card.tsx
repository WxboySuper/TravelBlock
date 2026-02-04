import { useMemo } from 'react';
import { StyleSheet, View, ViewProps, StyleProp, ViewStyle } from 'react-native';

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

  const combinedStyles = useMemo<StyleProp<ViewStyle>>(() => {
    const base: StyleProp<ViewStyle>[] = [
      styles.card,
      ({ backgroundColor: colors.cardBackground, borderColor: colors.cardBorder } as ViewStyle),
    ];

    if (variant === 'elevated') {
      // The dynamic elevated style uses runtime color tokens; cast to ViewStyle
      // to satisfy TypeScript while avoiding broad `any` usage.
      const elevatedStyle = ({
        shadowColor: colors.cardShadow,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 1,
        shadowRadius: 8,
        elevation: 2,
      } as ViewStyle);
      base.push(elevatedStyle);
    }

    if (style) {
      // style may be a number, object, or array; treat as StyleProp<ViewStyle>
      base.push(style as StyleProp<ViewStyle>);
    }

    return (base as unknown) as StyleProp<ViewStyle>;
  }, [variant, colors, style]);

  return (
    <View style={combinedStyles} testID={testID ?? 'card'} {...props}>
      {children}
    </View>
  );
}
