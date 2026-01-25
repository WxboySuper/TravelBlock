import { SymbolView, SymbolViewProps, SymbolWeight } from "expo-symbols";
import { OpaqueColorValue, StyleProp, ViewStyle } from "react-native";

/**
 * Render an Expo SymbolView icon configured by the provided props.
 *
 * @param name - The symbol name to display (from `SymbolViewProps.name`).
 * @param size - Icon width and height in pixels; defaults to `24`.
 * @param color - Tint color applied to the symbol.
 * @param style - Additional view style to merge with the size container.
 * @param weight - Symbol weight variant; defaults to `'regular'`.
 * @returns A JSX element rendering a SymbolView with the specified name, size, color, style, and weight.
 */
export function IconSymbol({
  name,
  size = 24,
  color,
  style,
  weight = "regular",
}: {
  name: SymbolViewProps["name"];
  size?: number;
  color: string | OpaqueColorValue;
  style?: StyleProp<ViewStyle>;
  weight?: SymbolWeight;
}) {
  return (
    <SymbolView
      weight={weight}
      tintColor={color}
      resizeMode="scaleAspectFit"
      name={name}
      style={[
        {
          width: size,
          height: size,
        },
        style,
      ]}
    />
  );
}
