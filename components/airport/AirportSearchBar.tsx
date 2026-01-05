import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { useThemeColor } from '@/hooks/use-theme-color';
import { useCallback } from 'react';
import {
  StyleSheet,
  TextInput,
  TouchableOpacity,
  type TextInputProps,
} from 'react-native';

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
    gap: 8,
  },
  input: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 8,
    borderRadius: 6,
    fontSize: 16,
    minHeight: 40,
  },
  clearButton: {
    padding: 4,
  },
  clearButtonText: {
    fontSize: 20,
    fontWeight: 'bold',
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
export function AirportSearchBar({  value,
  onChangeText,
  onClear,
  placeholder = 'Search airports...',
  ...props
}: AirportSearchBarProps) {
  const inputBackgroundColor = useThemeColor(
    { light: '#f5f5f5', dark: '#333' },
    'background'
  );
  const textColor = useThemeColor(
    { light: '#000', dark: '#fff' },
    'text'
  );
  const placeholderColor = useThemeColor(
    { light: '#999', dark: '#666' },
    'text'
  );

  const handleClear = useCallback(() => {
    onChangeText('');
    onClear?.();
  }, [onChangeText, onClear]);

  return (
    <ThemedView style={styles.container} testID="airport-search-bar">      {/* Search icon */}
      <ThemedText type="defaultSemiBold">🔍</ThemedText>

      {/* Search input */}
      <TextInput
        {...props}
        style={[
          styles.input,
          {
            backgroundColor: inputBackgroundColor,
            color: textColor,
          },
        ]}
        placeholder={placeholder}
        placeholderTextColor={placeholderColor}
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
          accessibilityHint="Clears the search input"
        >
          <ThemedText style={styles.clearButtonText}>×</ThemedText>
        </TouchableOpacity>
      )}
    </ThemedView>
  );
}