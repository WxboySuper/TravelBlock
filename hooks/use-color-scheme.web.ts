import { useEffect, useState } from "react";
import { useColorScheme as useRNColorScheme } from "react-native";

/**
 * Provides a hydration-aware color scheme for web, defaulting to 'light' until client-side hydration completes.
 *
 * @returns The active color scheme: `'light'`, `'dark'`, or `null`. Returns `'light'` before hydration.
 */
export function useColorScheme() {
  const [hasHydrated, setHasHydrated] = useState(false);

  useEffect(() => {
    setHasHydrated(true);
  }, []);

  const colorScheme = useRNColorScheme();

  if (hasHydrated) {
    return colorScheme;
  }

  return "light";
}
