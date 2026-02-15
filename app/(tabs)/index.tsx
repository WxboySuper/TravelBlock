"use client";

import { useRouter } from "expo-router";
import { useCallback, useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";

import { AirportCard } from "@/components/airport/AirportCard";
import { SelectAirportModal } from "@/components/airport/SelectAirportModal";
import { EmptyHomeBase } from "@/components/home/EmptyHomeBase";
import { SettingsButton, TopBar } from "@/components/navigation/TopBar";
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
    flexGrow: 1,
    padding: Spacing.lg,
    paddingBottom: 120, // Space for FloatingDock
  },
  airportSection: {
    marginTop: Spacing.md,
  },
  actionContainer: {
    paddingTop: Spacing.xl,
  },
  statusContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  statusLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: Spacing.sm,
  },
  statusText: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium,
  },
});

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good Morning';
  if (hour < 17) return 'Good Afternoon';
  return 'Good Evening';
}

function StatusIndicator({ color }: { color: string }) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={styles.statusContainer}>
      <View style={styles.statusLeft}>
        <View style={[styles.statusDot, { backgroundColor: color }]} />
        <ThemedText style={[styles.statusText, { color: colors.textSecondary }]}>
          Ready for Departure
        </ThemedText>
      </View>
    </View>
  );
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
        <StatusIndicator color={colors.success} />

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
