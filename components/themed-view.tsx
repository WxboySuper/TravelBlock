import { View, type ViewProps } from "react-native";

import { useThemeColor } from "@/hooks/use-theme-color";

export type ThemedViewProps = ViewProps & {
  lightColor?: string;
  darkColor?: string;
};

/**
 * Render a View whose background color is resolved from the current theme.
 *
 * @param lightColor - Optional background color to use in light theme
 * @param darkColor - Optional background color to use in dark theme
 * @param style - Additional view style(s) to merge with the themed background color
 * @returns A View element with the computed background color and any forwarded props
 */
export function ThemedView({
  style,
  lightColor,
  darkColor,
  ...otherProps
}: ThemedViewProps) {
  const backgroundColor = useThemeColor(
    { light: lightColor, dark: darkColor },
    "background",
  );

  return <View style={[{ backgroundColor }, style]} {...otherProps} />;
}
