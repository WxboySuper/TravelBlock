import { StyleSheet, Text, type TextProps } from "react-native";

import { useThemeColor } from "@/hooks/use-theme-color";

const styles = StyleSheet.create({
  default: {
    fontSize: 16,
    lineHeight: 24,
  },
  defaultSemiBold: {
    fontSize: 16,
    lineHeight: 24,
    fontWeight: "600",
  },
  title: {
    fontSize: 32,
    fontWeight: "bold",
    lineHeight: 32,
  },
  subtitle: {
    fontSize: 20,
    fontWeight: "bold",
  },
  link: {
    lineHeight: 30,
    fontSize: 16,
  },
  small: {
    fontSize: 12,
    lineHeight: 16,
  },
});

export type ThemedTextProps = TextProps & {
  lightColor?: string;
  darkColor?: string;
  type?:
    | "default"
    | "title"
    | "defaultSemiBold"
    | "subtitle"
    | "link"
    | "small";
};

/**
 * Render text that adapts color to the active theme and applies a predefined typography variant.
 *
 * @param style - Additional Text style overrides to merge with the component's variant styles
 * @param lightColor - Optional color to use when the light theme is active
 * @param darkColor - Optional color to use when the dark theme is active
 * @param type - Typography variant to apply: 'default', 'title', 'defaultSemiBold', 'subtitle', or 'link'
 * @returns A React Native Text element with the resolved theme color and the selected typography styles
 */
export function ThemedText({
  style,
  lightColor,
  darkColor,
  type = "default",
  ...rest
}: ThemedTextProps) {
  const color = useThemeColor({ light: lightColor, dark: darkColor }, "text");
  const linkColor = useThemeColor(
    { light: lightColor, dark: darkColor },
    "link",
  );

  const textProps = rest as TextProps;

  return (
    <Text
      {...textProps}
      style={[
        { color: type === "link" ? linkColor : color },
        type === "default" ? styles.default : undefined,
        type === "title" ? styles.title : undefined,
        type === "defaultSemiBold" ? styles.defaultSemiBold : undefined,
        type === "subtitle" ? styles.subtitle : undefined,
        type === "link" ? styles.link : undefined,
        type === "small" ? styles.small : undefined,
        style,
      ]}
    />
  );
}
