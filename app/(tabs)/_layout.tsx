import { Tabs } from "expo-router";
import { FloatingDock } from "@/components/navigation/FloatingDock";

/**
 * Renders the app's bottom tab navigator with Home and Logbook tabs.
 * Uses a custom floating dock for navigation.
 */
export default function TabLayout() {
  return (
    <Tabs
      tabBar={(props) => <FloatingDock {...props} />}
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
