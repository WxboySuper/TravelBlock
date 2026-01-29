import { useRouter } from "expo-router";
import { useCallback, useState } from "react";
import { StyleSheet, View, ScrollView } from "react-native";

import { AirportCard } from "@/components/airport/AirportCard";
import { SelectAirportModal } from "@/components/airport/SelectAirportModal";
import { EmptyHomeBase } from "@/components/home/EmptyHomeBase";
import { TopBar, SettingsButton } from "@/components/navigation/TopBar";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Button } from "@/components/ui/Button";
import { Colors, Spacing, Typography } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { useHomeAirport } from "@/hooks/useHomeAirport";
import { getCurrentLocation, hasLocationPermission } from "@/services/locationService";
import type { Airport } from "@/types/airport";
import type { Coordinates } from "@/types/location";

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    padding: Spacing.lg,
    paddingBottom: 120, // Space for FloatingDock
  },
  airportSection: {
    marginTop: Spacing.md,
  },
  actionContainer: {
    paddingTop: Spacing.xl,
  },
});

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good Morning';
  if (hour < 17) return 'Good Afternoon';
  return 'Good Evening';
}

export default function HomeScreen() {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];
  const { homeAirport, isLoading, handleSelectAirport, handleClearHomeBase } =
    useHomeAirport();
  const router = useRouter();

  const handleNewJourney = useCallback(() => {
    router.push("/flight/setup");
  }, [router]);

  const handleSelect = useCallback(
    async (airport: Airport) => {
      try {
        await handleSelectAirport(airport);
        setIsModalVisible(false);
      } catch {
        // Error already handled in hook
      }
    },
    [handleSelectAirport],
  );

  const openModal = useCallback(async () => {
    const hasPermission = await hasLocationPermission();
    if (hasPermission) {
      const location = await getCurrentLocation();
      if (location) {
        setUserLocation(location);
      }
    }
    setIsModalVisible(true);
  }, []);

  const closeModal = useCallback(() => setIsModalVisible(false), []);

  if (isLoading) {
    return (
      <ThemedView style={styles.container}>
        <TopBar title="TravelBlock" rightAction={<SettingsButton />} />
        <View style={styles.contentContainer}>
          <ThemedText
            style={{
              fontSize: Typography.fontSize.sm,
              color: colors.textSecondary,
              fontWeight: Typography.fontWeight.medium,
            }}
          >
            Loading...
          </ThemedText>
        </View>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <TopBar
        title="TravelBlock"
        subtitle={getGreeting()}
        rightAction={<SettingsButton />}
      />

      <ScrollView contentContainerStyle={styles.contentContainer}>
        {/* Status Text (Inline replacement for StatusBar) */}
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.md }}>
           <View style={{ flexDirection: 'row', alignItems: 'center' }}>
             <View style={{ width: 8, height: 8, borderRadius: 4, backgroundColor: colors.success, marginRight: Spacing.sm }} />
             <ThemedText style={{ fontSize: Typography.fontSize.xs, color: colors.textSecondary, fontWeight: Typography.fontWeight.medium }}>
               Ready for Departure
             </ThemedText>
           </View>
        </View>

        <View style={styles.airportSection}>
          {homeAirport ? (
            <>
              <AirportCard
                airport={homeAirport}
                onEdit={openModal}
                onClear={handleClearHomeBase}
              />
              <View style={styles.actionContainer}>
                <Button
                  title="New Journey"
                  onPress={handleNewJourney}
                  size="lg"
                  variant="primary"
                />
              </View>
            </>
          ) : (
            <EmptyHomeBase onSelectAirport={openModal} />
          )}
        </View>
      </ScrollView>

      <SelectAirportModal
        visible={isModalVisible}
        onClose={closeModal}
        onSelectAirport={handleSelect}
        title="Select Home Base"
        origin={userLocation}
      />
    </ThemedView>
  );
}
