import { AppIcon } from "@/components/ui/AppIcon";
import { ThemedText } from "@/components/themed-text";
import { BorderRadius, Colors, Elevation, Spacing, Typography } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import type { FlightBooking, FlightPhase } from "@/types/flight";
import { Pressable, StyleSheet, View, type TextStyle } from "react-native";

const styles = StyleSheet.create({
  shell: {
    paddingHorizontal: Spacing.md,
    paddingTop: Spacing.sm,
    paddingBottom: Spacing.md,
    gap: Spacing.md,
  },
  hero: {
    borderRadius: BorderRadius.xxl,
    borderWidth: 1,
    overflow: "hidden",
  },
  heroContent: {
    padding: Spacing.lg,
    gap: Spacing.md,
  },
  topRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: Spacing.sm,
    minWidth: 0,
  },
  timerBlock: {
    flex: 1,
    minWidth: 0,
  },
  eyebrow: {
    fontSize: Typography.fontSize.xs,
    textTransform: "uppercase",
    letterSpacing: 1.2,
    fontWeight: Typography.fontWeight.semibold as TextStyle["fontWeight"],
  },
  timerText: {
    fontSize: 50,
    fontWeight: Typography.fontWeight.bold as TextStyle["fontWeight"],
    lineHeight: 56,
    letterSpacing: -2,
    fontVariant: ["tabular-nums"] as TextStyle["fontVariant"],
    flexShrink: 1,
  },
  routeRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: Spacing.sm,
  },
  routeCode: {
    fontSize: Typography.fontSize.lg,
    fontWeight: Typography.fontWeight.bold as TextStyle["fontWeight"],
  },
  routeCity: {
    fontSize: Typography.fontSize.sm,
  },
  routeArrowWrap: {
    width: 38,
    height: 38,
    borderRadius: 19,
    alignItems: "center",
    justifyContent: "center",
  },
  routeCodeBlock: {
    flex: 1,
  },
  routeCodeBlockRight: {
    flex: 1,
    alignItems: "flex-end",
  },
  actionButton: {
    minWidth: 0,
    borderRadius: BorderRadius.full,
    paddingHorizontal: Spacing.sm,
    paddingVertical: 10,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
  },
  actionText: {
    color: "#FFFFFF",
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.bold as TextStyle["fontWeight"],
    letterSpacing: 0.4,
  },
  statusPill: {
    alignSelf: "flex-start",
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: BorderRadius.full,
    flexDirection: "row",
    alignItems: "center",
    gap: Spacing.xs,
  },
  statusText: {
    color: "#FFFFFF",
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.bold as TextStyle["fontWeight"],
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  footerMetaLeft: {
    color: "#FFFFFF",
    fontSize: Typography.fontSize.sm,
    flex: 1,
  },
  footerMetaRight: {
    fontSize: Typography.fontSize.sm,
  },
});

function formatTime(seconds: number): string {
  const safe = Math.max(seconds, 0);
  const hours = Math.floor(safe / 3600);
  const minutes = Math.floor((safe % 3600) / 60);
  const secs = safe % 60;

  if (hours > 0) {
    return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  }

  return `${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
}

function getPhaseMeta(phase: FlightPhase, colors: typeof Colors.light) {
  switch (phase) {
    case "climbing":
      return { color: colors.cockpitAccent, icon: "climb" as const, label: "Climbing" };
    case "descending":
      return { color: colors.cockpitSuccess, icon: "descend" as const, label: "Descending" };
    default:
      return { color: "#0EA5E9", icon: "cruise" as const, label: "Cruising" };
  }
}

function RouteBlock({
  alignRight = false,
  city,
  code,
  color,
}: {
  alignRight?: boolean;
  city: string;
  code: string;
  color: string;
}) {
  return (
    <View style={alignRight ? styles.routeCodeBlockRight : styles.routeCodeBlock}>
      <ThemedText darkColor="#F8FAFC" lightColor="#F8FAFC" style={styles.routeCode}>
        {code}
      </ThemedText>
      <ThemedText style={[styles.routeCity, { color }]}>{city}</ThemedText>
    </View>
  );
}

function HeaderFooter({
  aircraftName,
  flightNumber,
  progressLabel,
  secondaryColor,
}: {
  aircraftName: string;
  flightNumber: string;
  progressLabel: string;
  secondaryColor: string;
}) {
  return (
    <View style={{ flexDirection: "row", justifyContent: "space-between", gap: Spacing.sm, minWidth: 0 }}>
      <ThemedText
        numberOfLines={1}
        ellipsizeMode="tail"
        style={[styles.footerMetaLeft, { color: secondaryColor }]}
      >
        {flightNumber} · {aircraftName}
      </ThemedText>
      <ThemedText style={[styles.footerMetaRight, { color: secondaryColor }]}>
        {progressLabel}
      </ThemedText>
    </View>
  );
}

function HeroSurface({
  children,
  colors,
}: {
  children: React.ReactNode;
  colors: typeof Colors.light;
}) {
  return (
    <View
      style={[
        styles.hero,
        Elevation.floating,
        {
          backgroundColor: colors.cockpitSurface,
          borderColor: colors.cockpitBorder,
          shadowColor: "#000000",
        },
      ]}
    >
      <View style={styles.heroContent}>{children}</View>
    </View>
  );
}

function HeroTopRow({
  colors,
  onDivertPress,
  primaryTextColor,
  remainingSeconds,
}: {
  colors: typeof Colors.light;
  onDivertPress: () => void;
  primaryTextColor: string;
  remainingSeconds: number;
}) {
  return (
    <View style={styles.topRow}>
      <View style={styles.timerBlock}>
        <ThemedText style={[styles.eyebrow, { color: colors.cockpitTextSecondary }]}>In Flight</ThemedText>
        <ThemedText
          adjustsFontSizeToFit
          darkColor={primaryTextColor}
          lightColor={primaryTextColor}
          minimumFontScale={0.72}
          numberOfLines={1}
          style={styles.timerText}
        >
          {formatTime(remainingSeconds)}
        </ThemedText>
      </View>

      <Pressable
        accessibilityLabel="Divert flight"
        accessibilityRole="button"
        onPress={onDivertPress}
        style={[styles.actionButton, { backgroundColor: colors.cockpitWarning }]}
      >
        <AppIcon color="#FFFFFF" name="warning" size={16} />
        <ThemedText style={styles.actionText}>Divert</ThemedText>
      </Pressable>
    </View>
  );
}

function RouteSummary({
  booking,
  colors,
}: {
  booking: FlightBooking;
  colors: typeof Colors.light;
}) {
  return (
    <View style={styles.routeRow}>
      <RouteBlock city={booking.origin.city} code={booking.origin.iata} color={colors.cockpitTextSecondary} />

      <View style={[styles.routeArrowWrap, { backgroundColor: colors.cockpitAccentSoft }]}>
        <AppIcon color={colors.cockpitAccent} name="aircraft" size={18} style={{ transform: [{ rotate: "90deg" }] }} />
      </View>

      <RouteBlock alignRight city={booking.destination.city} code={booking.destination.iata} color={colors.cockpitTextSecondary} />
    </View>
  );
}

export function CockpitHeader({
  booking,
  isDiverted,
  onDivertPress,
  phase,
  progressPercent,
  remainingSeconds,
}: {
  booking: FlightBooking;
  isDiverted: boolean;
  onDivertPress: () => void;
  phase: FlightPhase;
  progressPercent: number;
  remainingSeconds: number;
}) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];
  const primaryTextColor = "#F8FAFC";
  const phaseMeta = getPhaseMeta(phase, colors);
  const progressLabel = progressPercent >= 100 ? "Arrived" : `${Math.round(100 - progressPercent)}% remaining`;

  return (
    <View style={styles.shell}>
      <HeroSurface colors={colors}>
        <HeroTopRow
          colors={colors}
          onDivertPress={onDivertPress}
          primaryTextColor={primaryTextColor}
          remainingSeconds={remainingSeconds}
        />

        <View style={[styles.statusPill, { backgroundColor: phaseMeta.color }]}>
          <AppIcon color="#FFFFFF" name={phaseMeta.icon} size={18} />
          <ThemedText style={styles.statusText}>{isDiverted ? "Diverted" : phaseMeta.label}</ThemedText>
        </View>

        <RouteSummary booking={booking} colors={colors} />

        <HeaderFooter
          aircraftName={booking.aircraft.name}
          flightNumber={booking.flightNumber}
          progressLabel={progressLabel}
          secondaryColor={colors.cockpitTextSecondary}
        />
      </HeroSurface>
    </View>
  );
}
