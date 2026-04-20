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

function TabItem({ route, index, state, descriptors, navigation, colors }: TabItemProps) {
  const isFocused = state.index === index;
  const { options } = descriptors[route.key] as { options: BottomTabNavigationOptions };

  const handlePress = () => {
    const event = navigation.emit({
      type: 'tabPress',
      target: route.key,
      canPreventDefault: true,
    });

    if (!isFocused && !event.defaultPrevented) {
      if (Platform.OS === 'ios') {
        void impactAsync(ImpactFeedbackStyle.Light);
      }
      navigation.navigate(route.name, route.params);
    }
  };

  const handleLongPress = () => {
    navigation.emit({
      type: 'tabLongPress',
      target: route.key,
    });
  };

  // Map route names to icons
  let iconName: AppIconName = 'home';
  let label = options.title ?? route.name;
  if (route.name === 'index') {
    iconName = 'home';
    label = 'Home';
  }
  if (route.name === 'explore') {
    iconName = 'logbook';
    label = 'Logbook';
  }

  const color = isFocused ? colors.tabIconSelected : colors.tabIconDefault;

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={isFocused ? { selected: true } : {}}
      accessibilityLabel={options.tabBarAccessibilityLabel}
      testID={options.tabBarButtonTestID}
      onPress={handlePress}
      onLongPress={handleLongPress}
      style={[styles.tabButton, isFocused ? { backgroundColor: colors.cockpitAccentSoft } : undefined]}
      android_ripple={{ borderless: true, color: colors.border }}
    >
      <View style={[styles.iconShell, isFocused ? { backgroundColor: colors.cockpitGlass } : undefined]}>
        <AppIcon size={22} name={iconName} color={color} />
      </View>
      <ThemedText style={[styles.tabLabel, { color }]}>{label}</ThemedText>
    </Pressable>
  );
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
    fontWeight: Typography.fontWeight.semibold as any,
    letterSpacing: 0.3,
  },
});
