import { useRouter } from "expo-router";
import { useCallback, useState } from "react";
import { StyleSheet, View } from "react-native";

import { AirportCard } from "@/components/airport/AirportCard";
import { SelectAirportModal } from "@/components/airport/SelectAirportModal";
import { EmptyHomeBase } from "@/components/home/EmptyHomeBase";
import { HomeHeader } from "@/components/home/HomeHeader";
import { StatusBar } from "@/components/home/StatusBar";
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
  },
  airportSection: {
    flex: 1,
    paddingTop: Spacing.md,
  },
  actionContainer: {
    paddingTop: Spacing.xl,
  },
});

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
        <StatusBar />
        <View style={styles.contentContainer}>
          <HomeHeader />
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
      <StatusBar />

      <View style={styles.contentContainer}>
        <HomeHeader />

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
      </View>

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
