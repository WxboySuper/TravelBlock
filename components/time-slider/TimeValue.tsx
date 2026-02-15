/**
 * TimeValue component displays the formatted time value for the slider.
 *
 * @module components/time-slider/TimeValue
 */

import { ThemedText } from "@/components/themed-text";
import { Colors, Typography } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { formatTimeValue } from "@/utils/timeSlider";
import { StyleSheet, View } from "react-native";

interface TimeValueProps {
  /** Time value in seconds */
  seconds: number;
  /** Optional additional styling */
  style?: any;
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  timeText: {
    fontSize: Typography.fontSize.xl,
    fontWeight: Typography.fontWeight.bold,
    letterSpacing: 0.5,
  },
  label: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.medium,
    textTransform: "uppercase",
    opacity: 0.6,
    marginTop: 2,
  },
});

/**
 * Displays a formatted time value with hours and minutes.
 *
 * @example
 * ```tsx
 * <TimeValue seconds={5400} />  // Displays "1h 30m"
 * <TimeValue seconds={1800} />  // Displays "30m"
 * ```
 */
export function TimeValue({ seconds, style }: TimeValueProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];
  const { formatted } = formatTimeValue(seconds);

  return (
    <View
      style={[
        styles.container,
        { backgroundColor: colors.cardBackground },
        style,
      ]}
    >
      <ThemedText style={styles.timeText}>{formatted}</ThemedText>
      <ThemedText style={styles.label}>Flight Duration</ThemedText>
    </View>
  );
}
