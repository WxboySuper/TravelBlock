import {
  DarkTheme,
  DefaultTheme,
  ThemeProvider,
} from "@react-navigation/native";
import { Stack, useRouter } from "expo-router";
import { StatusBar } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import "react-native-reanimated";

import { initStore } from "@/expo-sqlite/kv-store";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { storageService } from "@/services/storageService";
import { useEffect } from "react";

export const unstable_settings = {
  anchor: "(tabs)",
};

/**
 * Provides the app's root layout, supplying theme and top-level navigation.
 */
export default function RootLayout() {
  const colorScheme = useColorScheme();
  const router = useRouter();

  useEffect(() => {
    async function prepare() {
      try {
        // Initialize store in background - don't wait
        initStore().catch((e) => console.warn("Store init failed:", e));

        // Check onboarding and navigate if needed
        const hasCompleted = await storageService.getOnboardingCompleted();
        if (!hasCompleted) {
          router.replace("/onboarding");
        }
      } catch (e) {
        console.warn("Initialization error:", e);
      }
    }

    prepare();
  }, [router]);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen
            name="modal"
            options={{ presentation: "modal", title: "Modal" }}
          />
          <Stack.Screen
            name="flight/setup"
            options={{ presentation: "modal", headerShown: false }}
          />
          <Stack.Screen name="onboarding" options={{ headerShown: false }} />
        </Stack>
        <StatusBar
          barStyle={colorScheme === "dark" ? "light-content" : "dark-content"}
        />
      </ThemeProvider>
    </GestureHandlerRootView>
  );
}
