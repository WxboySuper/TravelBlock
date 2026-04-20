"use client";

import {
    DarkTheme,
    DefaultTheme,
    ThemeProvider,
} from "@react-navigation/native";
import { Stack, useRouter } from "expo-router";
import { StatusBar } from "react-native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import "react-native-reanimated";

import { FlightProvider } from "@/context/FlightContext";
import { initStore } from "@/expo-sqlite/kv-store";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { storageService } from "@/services/storageService";
import { useEffect } from "react";

export const unstable_settings = {
  anchor: "(tabs)",
};

function AppProviders({
  children,
  colorScheme,
}: {
  children: React.ReactNode;
  colorScheme: string | null | undefined;
}) {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <FlightProvider>
          <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
            {children}
          </ThemeProvider>
        </FlightProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

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
    <AppProviders colorScheme={colorScheme}>
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
        <Stack.Screen
          name="flight/destination"
          options={{ presentation: "modal", headerShown: false }}
        />
        <Stack.Screen name="flight/review" options={{ headerShown: false }} />
        <Stack.Screen
          name="flight/seat-selection"
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="flight/booking-confirmation"
          options={{ headerShown: false }}
        />
        <Stack.Screen name="flight/check-in" options={{ headerShown: false }} />
        <Stack.Screen
          name="flight/boarding-pass"
          options={{ headerShown: false }}
        />
        <Stack.Screen name="flight/cockpit" options={{ headerShown: false }} />
        <Stack.Screen name="onboarding" options={{ headerShown: false }} />
      </Stack>
      <StatusBar
        barStyle={colorScheme === "dark" ? "light-content" : "dark-content"}
      />
    </AppProviders>
  );
}
