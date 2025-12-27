import { PropsWithChildren, useState } from 'react';
import { StyleSheet, TouchableOpacity } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

const styles = StyleSheet.create({
  heading: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  content: {
    marginTop: 6,
    marginLeft: 24,
  },
});

/**
 * Renders a collapsible section with a tappable header that shows or hides its children.
 *
 * The header displays a chevron icon and the given `title`; tapping the header toggles visibility.
 * The chevron rotates 90° when open and its color adapts to the current color scheme.
 *
 * @param title - Text displayed in the header
 * @returns A React element containing the header and, when open, the section content
 */
export function Collapsible({ children, title }: PropsWithChildren & { title: string }) {
  const [isOpen, setIsOpen] = useState(false);
  const theme = useColorScheme() ?? 'light';
  const handleToggle = () => setIsOpen((value) => !value);
  const rotateStyle = { transform: [{ rotate: isOpen ? '90deg' : '0deg' }] };

  return (
    <ThemedView>
      <TouchableOpacity style={styles.heading} onPress={handleToggle} activeOpacity={0.8}>
        <IconSymbol
          name="chevron.right"
          size={18}
          weight="medium"
          color={theme === 'light' ? Colors.light.icon : Colors.dark.icon}
          style={rotateStyle}
        />

        <ThemedText type="defaultSemiBold">{title}</ThemedText>
      </TouchableOpacity>
      {isOpen && <ThemedView style={styles.content}>{children}</ThemedView>}
    </ThemedView>
  );
}