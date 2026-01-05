import { useCallback } from 'react';
import {
  StyleSheet,
  TextInput,
  TouchableOpacity,
  type TextInputProps,
} from 'react-native';
import { useThemeColor } from '@/hooks/use-theme-color';
import { ThemedView } from '@/components/themed-view';
import { ThemedText } from '@/components/themed-text';

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
 * Search input bar for airport selection with icon, input field, and optional clear button.
 *
 * Provides a search input with:
 * - Search icon on the left
 * - Debounced text input (debouncing handled by parent)
 * - Clear button (×) that appears when text is present
 * - Theme-aware styling
 *
 * @param value - Current search query
 * @param onChangeText - Callback when text changes
 * @param onClear - Callback when clear button is pressed
 * @param placeholder - Placeholder text (default: "Search airports...")
 * @param props - Additional TextInput props
 * @returns A themed search input component
 *
 * @example
 * ```tsx
 * const [query, setQuery] = useState('');
 * <AirportSearchBar
 *   value={query}
 *   onChangeText={setQuery}
 *   onClear={() => setQuery('')}
 *   placeholder="Find an airport..."
 * />
 * ```
 */
export function AirportSearchBar({
  value,
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

  const handleClearPress = useCallback(() => {
    handleClear();
  }, [handleClear]);

  return (
    <ThemedView style={styles.container}>
      {/* Search icon */}
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
          onPress={handleClearPress}
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
