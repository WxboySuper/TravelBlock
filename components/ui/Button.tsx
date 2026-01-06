import { useCallback } from 'react';
import { Pressable, PressableProps, StyleSheet, ViewStyle } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface ButtonProps extends Omit<PressableProps, 'style'> {
  title: string;
  variant?: 'primary' | 'secondary' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  style?: ViewStyle;
}

const styles = StyleSheet.create({
  button: {
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.7,
  },
  disabled: {
    opacity: 0.5,
  },
});

export function Button({ title, variant = 'primary', size = 'md', style, disabled, ...props }: ButtonProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const sizeStyles = {
    sm: { paddingVertical: Spacing.sm, paddingHorizontal: Spacing.md },
    md: { paddingVertical: Spacing.md, paddingHorizontal: Spacing.lg },
    lg: { paddingVertical: Spacing.lg, paddingHorizontal: Spacing.xl },
  };

  const variantStyles = {
    primary: {
      backgroundColor: colors.primary,
    },
    secondary: {
      backgroundColor: 'transparent',
      borderWidth: 1,
      borderColor: colors.border,
    },
    ghost: {
      backgroundColor: 'transparent',
    },
  };

  const textColors = {
    primary: '#FFFFFF',
    secondary: colors.text,
    ghost: colors.primary,
  };

  const getButtonStyle = useCallback(
    ({ pressed }: { pressed: boolean }) => [
      styles.button,
      sizeStyles[size],
      variantStyles[variant],
      pressed && styles.pressed,
      disabled && styles.disabled,
      style,
    ],
    [size, variant, disabled, style, colors, sizeStyles, variantStyles]
  );

  return (
    <Pressable style={getButtonStyle} disabled={disabled} {...props}>
      <ThemedText
        style={{
          color: disabled ? colors.textTertiary : textColors[variant],
          fontSize: Typography.fontSize.base,
          fontWeight: Typography.fontWeight.semibold,
          textAlign: 'center',
        }}>
        {title}
      </ThemedText>
    </Pressable>
  );
}
