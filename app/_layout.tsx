import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { StatusBar } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import 'react-native-reanimated';

import { initStore } from '@/expo-sqlite/kv-store';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useEffect } from 'react';

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

  useEffect(() => {
    // initialize persistent store early so native SQLite is ready before
    // UI interactions that may save the home airport.
    async function startup() {
      try {
        await initStore();
      } catch (error) {
        console.error('Failed to initialize persistent store', error);
        // TODO: Consider surfacing a fallback UI if store init fails
      }
    }

    void startup();
  }, []);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="modal" options={{ presentation: 'modal', title: 'Modal' }} />
        </Stack>
        <StatusBar barStyle={colorScheme === 'dark' ? 'light-content' : 'dark-content'} />
      </ThemeProvider>
    </GestureHandlerRootView>
  );
}