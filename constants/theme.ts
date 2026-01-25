/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import { Platform } from "react-native";

// Aviation-inspired minimal color palette
const tintColorLight = "#1E3A8A"; // Deep aviation blue
const tintColorDark = "#60A5FA"; // Sky blue

export const Colors = {
  light: {
    text: "#0F172A", // Slate 900
    textSecondary: "#64748B", // Slate 500
    textTertiary: "#94A3B8", // Slate 400
    background: "#F8FAFC", // Slate 50
    surface: "#FFFFFF",
    surfaceElevated: "#FFFFFF",
    border: "#E2E8F0", // Slate 200
    borderLight: "#F1F5F9", // Slate 100
    tint: tintColorLight,
    link: tintColorLight,
    icon: "#64748B",
    tabIconDefault: "#94A3B8",
    tabIconSelected: tintColorLight,
    // Aviation accent colors
    primary: "#1E3A8A", // Blue 900
    primaryLight: "#3B82F6", // Blue 500
    success: "#10B981", // Emerald 500
    warning: "#F59E0B", // Amber 500
    error: "#EF4444", // Red 500
    // Card variants
    cardBackground: "#FFFFFF",
    cardBorder: "#E2E8F0",
    cardShadow: "rgba(15, 23, 42, 0.04)",
  },
  dark: {
    text: "#F8FAFC", // Slate 50
    textSecondary: "#94A3B8", // Slate 400
    textTertiary: "#64748B", // Slate 500
    background: "#0F172A", // Slate 900
    surface: "#1E293B", // Slate 800
    surfaceElevated: "#334155", // Slate 700
    border: "#334155", // Slate 700
    borderLight: "#1E293B", // Slate 800
    tint: tintColorDark,
    link: tintColorDark,
    icon: "#94A3B8",
    tabIconDefault: "#64748B",
    tabIconSelected: tintColorDark,
    // Aviation accent colors
    primary: "#60A5FA", // Blue 400
    primaryLight: "#93C5FD", // Blue 300
    success: "#34D399", // Emerald 400
    warning: "#FBBF24", // Amber 400
    error: "#F87171", // Red 400
    // Card variants
    cardBackground: "#1E293B",
    cardBorder: "#334155",
    cardShadow: "rgba(0, 0, 0, 0.3)",
  },
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const BorderRadius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  full: 9999,
};

export const Typography = {
  fontSize: {
    xs: 12,
    sm: 14,
    base: 16,
    lg: 18,
    xl: 20,
    xxl: 24,
    xxxl: 32,
  },
  fontWeight: {
    regular: "400" as const,
    medium: "500" as const,
    semibold: "600" as const,
    bold: "700" as const,
  },
  lineHeight: {
    tight: 1.2,
    normal: 1.5,
    relaxed: 1.75,
  },
};

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: "system-ui",
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: "ui-serif",
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: "ui-rounded",
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: "ui-monospace",
  },
  default: {
    sans: "normal",
    serif: "serif",
    rounded: "normal",
    mono: "monospace",
  },
  web: {
    sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    serif: "Georgia, 'Times New Roman', serif",
    rounded:
      "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
    mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
  },
});
