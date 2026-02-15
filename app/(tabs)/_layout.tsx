"use client";

import { FloatingDock } from "@/components/navigation/FloatingDock";
import { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import { Tabs } from "expo-router";

const TabBar = (props: BottomTabBarProps) => <FloatingDock {...props} />;

/**
 * Renders the app's bottom tab navigator with Home and Logbook tabs.
 * Uses a custom floating dock for navigation.
 */
export default function TabLayout() {
  return (
    <Tabs
      tabBar={TabBar}
      screenOptions={{
        headerShown: false,
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "Home",
        }}
      />
      <Tabs.Screen
        name="explore"
        options={{
          title: "Logbook",
        }}
      />
    </Tabs>
  );
}
