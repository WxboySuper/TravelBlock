/**
 * Learn more about light and dark modes:
 * https://docs.expo.dev/guides/color-schemes/
 */

import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";

/**
 * Selects a color value for the current color scheme.
 *
 * @param props - Optional theme-specific color overrides; may include `light` and/or `dark`.
 * @param colorName - The color key to use from the `Colors` palette (must exist for both light and dark).
 * @returns The resolved color string for the active theme: the theme-specific value from `props` if present, otherwise `Colors[theme][colorName]`.
 */
export function useThemeColor(
  props: { light?: string; dark?: string },
  colorName: keyof typeof Colors.light & keyof typeof Colors.dark,
) {
  const theme = useColorScheme() ?? "light";
  const colorFromProps = props[theme];

  if (colorFromProps) {
    return colorFromProps;
  } else {
    return Colors[theme][colorName];
  }
}
