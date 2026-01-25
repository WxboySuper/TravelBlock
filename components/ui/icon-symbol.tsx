// Fallback for using MaterialIcons on Android and web.

import MaterialIcons from "@expo/vector-icons/MaterialIcons";
import { SymbolViewProps, SymbolWeight } from "expo-symbols";
import { ComponentProps } from "react";
import { OpaqueColorValue, type StyleProp, type TextStyle } from "react-native";

type IconMapping = Record<
  SymbolViewProps["name"],
  ComponentProps<typeof MaterialIcons>["name"]
>;
type IconSymbolName = keyof typeof MAPPING;

/**
 * Add your SF Symbols to Material Icons mappings here.
 * - see Material Icons in the [Icons Directory](https://icons.expo.fyi).
 * - see SF Symbols in the [SF Symbols](https://developer.apple.com/sf-symbols/) app.
 */
const MAPPING = {
  "house.fill": "home",
  "paperplane.fill": "send",
  "chevron.left.forwardslash.chevron.right": "code",
  "chevron.right": "chevron-right",
  "airplane.departure": "flight-takeoff",
  pencil: "edit",
  "book.fill": "menu-book",
} as IconMapping;

/**
 * Renders a cross-platform icon using an SF Symbol name mapped to a MaterialIcons glyph.
 *
 * @param name - SF Symbol name; must be a key in the component's internal mapping and is translated to a MaterialIcons name
 * @param size - Icon size in pixels (default 24)
 * @param color - Icon color
 * @param style - Text-style overrides applied to the icon
 * @param weight - Optional SF Symbol weight; currently ignored by this implementation
 * @returns The MaterialIcons element rendered with the mapped icon name, specified size, color, and style
 */
export function IconSymbol({
  name,
  size = 24,
  color,
  style,
}: {
  name: IconSymbolName;
  size?: number;
  color: string | OpaqueColorValue;
  style?: StyleProp<TextStyle>;
  weight?: SymbolWeight;
}) {
  return (
    <MaterialIcons
      color={color}
      size={size}
      name={MAPPING[name]}
      style={style}
    />
  );
}
