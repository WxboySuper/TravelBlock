/**
 * CockpitTabs component - segmented control for cockpit views
 * 
 * Horizontal tab switcher with three views: Map, Metrics, Info.
 * Includes animated underline indicator and haptic feedback.
 * 
 * @module components/cockpit/CockpitTabs
 */

import { AppIcon, type AppIconName } from '@/components/ui/AppIcon';
import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useEffect, useMemo, useRef, useState } from 'react';
import { Animated, LayoutChangeEvent, Pressable, StyleSheet, View } from 'react-native';

export type CockpitTab = 'map' | 'metrics' | 'info';

export interface CockpitTabsProps {
  /** Currently selected tab */
  selectedTab: CockpitTab;
  /** Callback when tab changes */
  onTabChange: (tab: CockpitTab) => void;
}

const TABS: { id: CockpitTab; label: string; icon: AppIconName }[] = [
  { id: 'map', label: 'Map', icon: 'map' },
  { id: 'metrics', label: 'Metrics', icon: 'metrics' },
  { id: 'info', label: 'Info', icon: 'info' },
];
const TAB_GAP = Spacing.sm;

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: Spacing.md,
    paddingBottom: Spacing.sm,
  },
  tabsRow: {
    flexDirection: 'row',
    gap: Spacing.sm,
    position: 'relative',
  },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.md,
    gap: Spacing.xs,
    borderRadius: 999,
  },
  tabLabel: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as any,
    letterSpacing: 0.3,
  },
  indicatorContainer: {
    height: 0,
  },
  indicator: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    borderRadius: 999,
  },
});

/**
 * CockpitTabs component
 * 
 * Segmented control for switching between cockpit views.
 * 
 * @example
 * ```tsx
 * const [selectedTab, setSelectedTab] = useState<CockpitTab>('map');
 * 
 * <CockpitTabs
 *   selectedTab={selectedTab}
 *   onTabChange={setSelectedTab}
 * />
 * ```
 */
export function CockpitTabs({ selectedTab, onTabChange }: CockpitTabsProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];
  const [tabsWidth, setTabsWidth] = useState(0);
  const indicatorPosition = useRef(new Animated.Value(0)).current;
  const selectedTabIndex = useMemo(() => {
    return Math.max(
      TABS.findIndex((tab) => tab.id === selectedTab),
      0
    );
  }, [selectedTab]);
  const indicatorWidth = useMemo(() => {
    if (tabsWidth === 0) {
      return 0;
    }

    return (tabsWidth - TAB_GAP * (TABS.length - 1)) / TABS.length;
  }, [tabsWidth]);

  useEffect(() => {
    Animated.spring(indicatorPosition, {
      toValue: selectedTabIndex * (indicatorWidth + TAB_GAP),
      friction: 8,
      tension: 40,
      useNativeDriver: true,
    }).start();
  }, [indicatorPosition, indicatorWidth, selectedTabIndex]);

  const handleTabsLayout = (event: LayoutChangeEvent) => {
    setTabsWidth(event.nativeEvent.layout.width);
  };

  const handleTabPress = (tab: CockpitTab) => {
    if (tab !== selectedTab) {
      impactAsync(ImpactFeedbackStyle.Light).catch(() => {});
      onTabChange(tab);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.tabsRow} onLayout={handleTabsLayout}>
        {indicatorWidth > 0 ? (
          <Animated.View
            pointerEvents="none"
            style={[
              styles.indicator,
              {
                backgroundColor: colors.cockpitAccentSoft,
                borderColor: colors.cockpitBorder,
                borderWidth: 1,
                width: indicatorWidth,
                transform: [{ translateX: indicatorPosition }],
              },
            ]}
          />
        ) : null}
        {TABS.map((tab) => {
          const isSelected = tab.id === selectedTab;
          const textColor = isSelected ? colors.cockpitAccent : colors.cockpitTextSecondary;

          return (
            <Pressable
              key={tab.id}
              style={styles.tab}
              onPress={() => handleTabPress(tab.id)}
              accessibilityRole="tab"
              accessibilityState={{ selected: isSelected }}
            >
              <AppIcon color={textColor} name={tab.icon} size={18} />
              <ThemedText style={[styles.tabLabel, { color: textColor }]}>
                {tab.label}
              </ThemedText>
            </Pressable>
          );
        })}
      </View>
      <View style={styles.indicatorContainer} />
    </View>
  );
}
