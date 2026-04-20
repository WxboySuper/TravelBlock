import MaterialIcons from "@expo/vector-icons/MaterialIcons";
import type { ComponentProps } from "react";
import type { OpaqueColorValue, StyleProp, TextStyle } from "react-native";

export type AppIconName =
  | "home"
  | "logbook"
  | "settings"
  | "map"
  | "metrics"
  | "info"
  | "warning"
  | "aircraft"
  | "aircraftHeading"
  | "altitude"
  | "heading"
  | "climb"
  | "descend"
  | "cruise"
  | "distance"
  | "time"
  | "location"
  | "route"
  | "speed"
  | "check"
  | "airport";

const ICONS: Record<AppIconName, ComponentProps<typeof MaterialIcons>["name"]> = {
  home: "home-filled",
  logbook: "menu-book",
  settings: "settings",
  map: "map",
  metrics: "bar-chart",
  info: "info",
  warning: "warning-amber",
  aircraft: "flight",
  aircraftHeading: "navigation",
  altitude: "flight-takeoff",
  heading: "explore",
  climb: "trending-up",
  descend: "trending-down",
  cruise: "trending-flat",
  distance: "straighten",
  time: "schedule",
  location: "place",
  route: "alt-route",
  speed: "speed",
  check: "check-circle",
  airport: "local-airport",
};

export function AppIcon({
  color,
  name,
  size = 24,
  style,
}: {
  color: string | OpaqueColorValue;
  name: AppIconName;
  size?: number;
  style?: StyleProp<TextStyle>;
}) {
  return <MaterialIcons color={color} name={ICONS[name]} size={size} style={style} />;
}
