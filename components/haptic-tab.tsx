import { BottomTabBarButtonProps } from '@react-navigation/bottom-tabs';
import { PlatformPressable } from '@react-navigation/elements';
import { Platform } from 'react-native';
import * as Haptics from 'expo-haptics';

/**
 * Render a bottom-tab pressable that triggers a light iOS haptic on press-in.
 *
 * The component forwards all received BottomTabBarButtonProps to PlatformPressable.
 *
 * @param props - Props forwarded to the underlying PlatformPressable; if an `onPressIn` handler
 *                is provided it will be called after the haptic feedback (on iOS).
 * @returns A PlatformPressable configured for use as a bottom-tab button with optional iOS haptic feedback.
 */
export function HapticTab(props: BottomTabBarButtonProps) {
  return (
    <PlatformPressable
      {...props}
      onPressIn={(ev) => {
        if (Platform.OS === 'ios') {
          // Add a soft haptic feedback when pressing down on the tabs.
          Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        }
        props.onPressIn?.(ev);
      }}
    />
  );
}