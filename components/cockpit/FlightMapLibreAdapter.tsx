import { ThemedText } from "@/components/themed-text";
import { BorderRadius, Colors, Spacing, Typography } from "@/constants/theme";
import { StyleSheet, View } from "react-native";

import type { FlightMapAdapterProps } from "./mapTypes";

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: Spacing.xl,
  },
  card: {
    width: "100%",
    borderRadius: BorderRadius.xl,
    borderWidth: 1,
    padding: Spacing.xl,
    gap: Spacing.sm,
  },
  title: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold as any,
  },
  body: {
    fontSize: Typography.fontSize.base,
    lineHeight: 24,
  },
});

export function FlightMapLibreAdapter({ theme }: FlightMapAdapterProps) {
  const colors = Colors[theme];

  return (
    <View style={[styles.container, { backgroundColor: colors.cockpitBackground }]}>
      <View style={[styles.card, { backgroundColor: colors.cockpitSurface, borderColor: colors.cockpitBorder }]}>
        <ThemedText style={[styles.title, { color: colors.text }]}>MapLibre Adapter Ready</ThemedText>
        <ThemedText style={[styles.body, { color: colors.cockpitTextSecondary }]}>
          The cockpit now renders through a provider seam. MapLibre can replace Google here once the native package
          and Expo plugin are added in a follow-up build-focused change.
        </ThemedText>
      </View>
    </View>
  );
}
