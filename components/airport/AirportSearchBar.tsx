import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, BorderRadius, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useCallback } from 'react';
import { StyleSheet, TextInput, TouchableOpacity, type TextInputProps } from 'react-native';

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    gap: Spacing.md,
  },
  input: {
    flex: 1,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.md,
    fontSize: Typography.fontSize.base,
    minHeight: 48,
    borderWidth: 1,
  },
  clearButton: {
    padding: Spacing.sm,
  },
  clearButtonText: {
    fontSize: 24,
    fontWeight: '300',
  },
});

export interface AirportSearchBarProps extends Omit<TextInputProps, 'value'> {
  /** Current search query value */
  value: string;
  /** Called when text changes */
  onChangeText: (text: string) => void;
  /** Called when clear button is pressed */
  onClear?: () => void;
  /** Placeholder text for input (default: "Search airports...") */
  placeholder?: string;
}

/**
 * Render a themed airport search bar with a leading search icon, a text input, and an optional clear button.
 *
 * The clear button appears only when `value` is non-empty.
 *
 * @param value - Current search query shown in the input
 * @param onChangeText - Invoked with the new text when the input changes
 * @param onClear - Optional callback invoked when the clear button is pressed
 * @param placeholder - Placeholder text for the input (default: "Search airports...")
 * @param props - Additional TextInput props forwarded to the underlying TextInput
 * @returns A React element representing the airport search bar
 */
export function AirportSearchBar({
  value,
  onChangeText,
  onClear,
  placeholder = 'Search airports...',
  ...props
}: AirportSearchBarProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const handleClear = useCallback(() => {
    onChangeText('');
    onClear?.();
  }, [onChangeText, onClear]);

  return (
    <ThemedView style={styles.container} testID="airport-search-bar">
      {/* Search icon */}
      <ThemedText style={{ fontSize: Typography.fontSize.lg }}>🔍</ThemedText>

      {/* Search input */}
      <TextInput
        {...props}
        style={[
          styles.input,
          {
            backgroundColor: colors.surface,
            color: colors.text,
            borderColor: colors.border,
          },
        ]}
        placeholder={placeholder}
        placeholderTextColor={colors.textTertiary}
        value={value}
        onChangeText={onChangeText}
        clearButtonMode="never"
        testID="airport-search-input"
      />

      {/* Clear button */}
      {value.length > 0 && (
        <TouchableOpacity
          style={styles.clearButton}
          onPress={handleClear}
          testID="airport-search-clear"
          accessibilityLabel="Clear search"
          accessibilityHint="Clears the search input">
          <ThemedText style={[styles.clearButtonText, { color: colors.textTertiary }]}>×</ThemedText>
        </TouchableOpacity>
      )}
    </ThemedView>
  );
}