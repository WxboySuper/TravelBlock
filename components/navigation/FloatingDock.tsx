import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { StyleSheet, View, Platform, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';

import { IconSymbol, IconSymbolName } from '@/components/ui/icon-symbol';
import { Colors, Spacing, BorderRadius } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

export function FloatingDock({ state, descriptors, navigation }: BottomTabBarProps) {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const styles = StyleSheet.create({
    container: {
      position: 'absolute',
      bottom: Spacing.lg + insets.bottom, // Lifted up
      left: Spacing.xl,
      right: Spacing.xl,
      flexDirection: 'row',
      backgroundColor: colors.surfaceElevated,
      borderRadius: BorderRadius.full,
      height: 64,
      alignItems: 'center',
      justifyContent: 'space-around',
      // Shadow for depth
      shadowColor: colors.cardShadow,
      shadowOffset: {
        width: 0,
        height: 4,
      },
      shadowOpacity: 0.15,
      shadowRadius: 12,
      elevation: 8,
      borderWidth: 1,
      borderColor: colors.borderLight,
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
      backgroundColor: colors.tint,
      opacity: 0.1,
    }
  });

  return (
    <View style={styles.container}>
      {state.routes.map((route, index) => {
        const { options } = descriptors[route.key];
        const isFocused = state.index === index;

        const onPress = () => {
          const event = navigation.emit({
            type: 'tabPress',
            target: route.key,
            canPreventDefault: true,
          });

          if (!isFocused && !event.defaultPrevented) {
            if (Platform.OS === 'ios') {
              Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
            }
            navigation.navigate(route.name, route.params);
          }
        };

        const onLongPress = () => {
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
            key={route.key}
            accessibilityRole="button"
            accessibilityState={isFocused ? { selected: true } : {}}
            accessibilityLabel={options.tabBarAccessibilityLabel}
            testID={options.tabBarButtonTestID}
            onPress={onPress}
            onLongPress={onLongPress}
            style={styles.tabButton}
            android_ripple={{ borderless: true, color: colors.border }}
          >
            {isFocused && <View style={styles.activeIndicator} />}
            <IconSymbol size={28} name={iconName} color={color} />
          </Pressable>
        );
      })}
    </View>
  );
}
