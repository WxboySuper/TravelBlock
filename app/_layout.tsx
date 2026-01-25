import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { StatusBar } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import 'react-native-reanimated';

import { initStore } from '@/expo-sqlite/kv-store';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { storageService } from '@/services/storageService';
import { useRouter } from 'expo-router';
import { useEffect, useState } from 'react';

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
  const router = useRouter();
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    // initialize persistent store early so native SQLite is ready before
    // UI interactions that may save the home airport.
    async function startup() {
      try {
        await initStore();

        // Check onboarding status
        const hasCompleted = await storageService.getOnboardingCompleted();
        if (!hasCompleted) {
          router.replace('/onboarding');
        }
      } catch (error) {
        console.error('Failed to initialize persistent store or check onboarding', error);
      } finally {
        setIsReady(true);
      }
    }

    startup();
  }, [router]);

  if (!isReady) {
    return null; // or a Splash Screen
  }

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="modal" options={{ presentation: 'modal', title: 'Modal' }} />
          <Stack.Screen name="flight/setup" options={{ presentation: 'modal', headerShown: false }} />
          <Stack.Screen name="onboarding" options={{ headerShown: false }} />
        </Stack>
        <StatusBar barStyle={colorScheme === 'dark' ? 'light-content' : 'dark-content'} />
      </ThemeProvider>
    </GestureHandlerRootView>
  );
}