import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { StatusBar } from 'react-native';
import 'react-native-reanimated';

import { useColorScheme } from '@/hooks/use-color-scheme';

export const unstable_settings = {
  anchor: '(tabs)',
};

/**
 * Provides the app's root layout, supplying theme and top-level navigation.
 *
 * The ThemeProvider uses the current color scheme to select between DarkTheme and DefaultTheme.
 * The navigation stack includes a "(tabs)" screen (header hidden) and a "modal" screen presented as a modal with title "Modal". A StatusBar is rendered with automatic style.
 *
 * @returns The root React element containing the themed navigation stack and status bar.
 */
export default function RootLayout() {
  const colorScheme = useColorScheme();

  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <Stack>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="modal" options={{ presentation: 'modal', title: 'Modal' }} />
      </Stack>
      <StatusBar barStyle={colorScheme === 'dark' ? 'light-content' : 'dark-content'} />
    </ThemeProvider>
  );
}