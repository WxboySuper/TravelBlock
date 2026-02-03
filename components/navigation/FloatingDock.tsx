import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { StyleSheet, View, Platform, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { impactAsync, ImpactFeedbackStyle } from 'expo-haptics';

import { IconSymbol, IconSymbolName } from '@/components/ui/icon-symbol';
import { Colors, Spacing, BorderRadius } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface TabItemProps {
  route: any;
  index: number;
  state: any;
  descriptors: any;
  navigation: any;
  colors: any;
}

function TabItem({ route, index, state, descriptors, navigation, colors }: TabItemProps) {
  const isFocused = state.index === index;
  const { options } = descriptors[route.key];

  const handlePress = () => {
    const event = navigation.emit({
      type: 'tabPress',
      target: route.key,
      canPreventDefault: true,
    });

    if (!isFocused && !event.defaultPrevented) {
      if (Platform.OS === 'ios') {
        impactAsync(ImpactFeedbackStyle.Light);
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
  let iconName: IconSymbolName = 'house.fill'; // Default
  if (route.name === 'index') iconName = 'house.fill';
  if (route.name === 'explore') iconName = 'book.fill';

  const color = isFocused ? colors.tabIconSelected : colors.tabIconDefault;

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={isFocused ? { selected: true } : {}}
      accessibilityLabel={options.tabBarAccessibilityLabel}
      testID={options.tabBarButtonTestID}
      onPress={handlePress}
      onLongPress={handleLongPress}
      style={styles.tabButton}
      android_ripple={{ borderless: true, color: colors.border }}
    >
      {isFocused && <View style={[styles.activeIndicator, { backgroundColor: colors.tint }]} />}
      <IconSymbol size={28} name={iconName} color={color} />
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
        bottom: Spacing.lg + insets.bottom, 
        backgroundColor: colors.surfaceElevated,
        borderColor: colors.borderLight,
        shadowColor: colors.cardShadow,
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
    left: Spacing.xl,
    right: Spacing.xl,
    flexDirection: 'row',
    borderRadius: BorderRadius.full,
    height: 64,
    alignItems: 'center',
    justifyContent: 'space-around',
    // Shadow for depth
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
    borderWidth: 1,
  },
  tabButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
    borderRadius: BorderRadius.full,
  },
  activeIndicator: {
    position: 'absolute',
    width: 40,
    height: 40,
    borderRadius: 20,
    opacity: 0.1,
  }
});
