import { BottomTabBarProps, BottomTabNavigationOptions } from '@react-navigation/bottom-tabs';
import { StyleSheet, View, Platform, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';

import { AppIcon, type AppIconName } from '@/components/ui/AppIcon';
import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, BorderRadius, Elevation, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface TabItemProps {
  route: BottomTabBarProps['state']['routes'][0];
  index: number;
  state: BottomTabBarProps['state'];
  descriptors: BottomTabBarProps['descriptors'];
  navigation: BottomTabBarProps['navigation'];
  colors: typeof Colors['light'];
}

type TabPresentation = {
  iconName: AppIconName;
  label: string;
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: Spacing.lg,
    right: Spacing.lg,
    flexDirection: 'row',
    borderRadius: BorderRadius.xxl,
    minHeight: 72,
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    paddingHorizontal: Spacing.sm,
    paddingVertical: Spacing.sm,
    ...Elevation.floating,
  },
  tabButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 56,
    borderRadius: BorderRadius.xl,
    gap: 2,
  },
  iconShell: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabLabel: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold,
    letterSpacing: 0.3,
  },
});

function TabItem({ route, index, state, descriptors, navigation, colors }: TabItemProps) {
  const isFocused = state.index === index;
  const { options } = descriptors[route.key] as { options: BottomTabNavigationOptions };
  const { iconName, label } = getTabPresentation(route.name, options.title);
  const color = isFocused ? colors.tabIconSelected : colors.tabIconDefault;
  const buttonStyle = isFocused ? { backgroundColor: colors.cockpitAccentSoft } : undefined;
  const iconShellStyle = isFocused ? { backgroundColor: colors.cockpitGlass } : undefined;

  const handlePress = () => {
    onTabPress({ isFocused, navigation, route });
  };

  const handleLongPress = () => {
    navigation.emit({
      type: 'tabLongPress',
      target: route.key,
    });
  };

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={isFocused ? { selected: true } : {}}
      accessibilityLabel={options.tabBarAccessibilityLabel}
      testID={options.tabBarButtonTestID}
      onPress={handlePress}
      onLongPress={handleLongPress}
      style={[styles.tabButton, buttonStyle]}
      android_ripple={{ borderless: true, color: colors.border }}
    >
      <View style={[styles.iconShell, iconShellStyle]}>
        <AppIcon size={22} name={iconName} color={color} />
      </View>
      <ThemedText style={[styles.tabLabel, { color }]}>{label}</ThemedText>
    </Pressable>
  );
}

function getTabPresentation(routeName: string, title?: string): TabPresentation {
  if (routeName === 'index') {
    return { iconName: 'home', label: 'Home' };
  }

  if (routeName === 'explore') {
    return { iconName: 'logbook', label: 'Logbook' };
  }

  return {
    iconName: 'home',
    label: title ?? routeName,
  };
}

function onTabPress({
  isFocused,
  navigation,
  route,
}: {
  isFocused: boolean;
  navigation: BottomTabBarProps['navigation'];
  route: BottomTabBarProps['state']['routes'][0];
}) {
  const event = navigation.emit({
    type: 'tabPress',
    target: route.key,
    canPreventDefault: true,
  });

  if (isFocused || event.defaultPrevented) {
    return;
  }

  if (Platform.OS === 'ios') {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
  }

  navigation.navigate(route.name, route.params);
}

export function FloatingDock({ state, descriptors, navigation }: BottomTabBarProps) {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <View style={[
      styles.container, 
      { 
        bottom: Math.max(Spacing.md, insets.bottom + Spacing.sm),
        backgroundColor: colors.surfaceElevated,
        borderColor: colors.cockpitBorder,
        shadowColor: '#000000',
      }
    ]}>
      {state.routes.map((route, index) => (
        <TabItem
          key={route.key}
          route={route}
          index={index}
          state={state}
          descriptors={descriptors}
          navigation={navigation}
          colors={colors}
        />
      ))}
    </View>
  );
}

