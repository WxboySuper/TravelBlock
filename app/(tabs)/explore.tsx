import { StyleSheet, View } from "react-native";

import { EmptyLogbook } from "@/components/logbook/EmptyLogbook";
import { TopBar, SettingsButton } from "@/components/navigation/TopBar";
import { ThemedView } from "@/components/themed-view";
import { Spacing } from "@/constants/theme";

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    padding: Spacing.lg,
    paddingBottom: 120, // Space for FloatingDock
  },
});

export default function ExploreScreen(): React.JSX.Element {
  return (
    <ThemedView style={styles.container} testID="explore-root">
      <TopBar
        title="Logbook"
        subtitle="Your Flight History"
        rightAction={<SettingsButton />}
      />

      <View style={styles.contentContainer}>
        <EmptyLogbook />
      </View>
    </ThemedView>
  );
}
