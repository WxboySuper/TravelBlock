/**
 * CockpitTabs component - segmented control for cockpit views
 * 
 * Horizontal tab switcher with three views: Map, Metrics, Info.
 * Includes animated underline indicator and haptic feedback.
 * 
 * @module components/cockpit/CockpitTabs
 */

import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';
import { useEffect, useMemo, useRef, useState } from 'react';
import { Animated, LayoutChangeEvent, StyleSheet, TouchableOpacity, View } from 'react-native';

export type CockpitTab = 'map' | 'metrics' | 'info';

export interface CockpitTabsProps {
  /** Currently selected tab */
  selectedTab: CockpitTab;
  /** Callback when tab changes */
  onTabChange: (tab: CockpitTab) => void;
}

const TABS: { id: CockpitTab; label: string; icon: string }[] = [
  { id: 'map', label: 'Map', icon: '🗺️' },
  { id: 'metrics', label: 'Metrics', icon: '📊' },
  { id: 'info', label: 'Info', icon: 'ℹ️' },
];
const TAB_GAP = Spacing.xs;

const styles = StyleSheet.create({
  container: {
    borderBottomWidth: 1,
    paddingHorizontal: Spacing.md,
  },
  tabsRow: {
    flexDirection: 'row',
    gap: Spacing.xs,
  },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.md,
    gap: Spacing.xs,
  },
  tabIcon: {
    fontSize: Typography.fontSize.base,
  },
  tabLabel: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as any,
  },
  indicatorContainer: {
    height: 3,
    marginTop: -3,
  },
  indicator: {
    height: 3,
    borderRadius: 2,
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
    <View style={[styles.container, { borderBottomColor: colors.border }]}>
      <View style={styles.tabsRow} onLayout={handleTabsLayout}>
        {TABS.map((tab) => {
          const isSelected = tab.id === selectedTab;
          const textColor = isSelected ? colors.tint : colors.textSecondary;

          return (
            <TouchableOpacity
              key={tab.id}
              style={styles.tab}
              onPress={() => handleTabPress(tab.id)}
              accessibilityRole="tab"
              accessibilityState={{ selected: isSelected }}
            >
              <ThemedText style={[styles.tabIcon, { color: textColor }]}>
                {tab.icon}
              </ThemedText>
              <ThemedText style={[styles.tabLabel, { color: textColor }]}>
                {tab.label}
              </ThemedText>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Animated indicator */}
      <View style={styles.indicatorContainer}>
        <Animated.View
          style={[
            styles.indicator,
            {
              backgroundColor: colors.tint,
              width: indicatorWidth,
              transform: [{ translateX: indicatorPosition }],
            },
          ]}
        />
      </View>
    </View>
  );
}
