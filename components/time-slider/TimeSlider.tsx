/**
 * TimeSlider component provides a custom slider for selecting flight duration.
 *
 * Uses react-native-gesture-handler for smooth gesture handling and
 * react-native-reanimated for high-performance animations.
 *
 * @module components/time-slider/TimeSlider
 */

import { Colors, Spacing } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import {
    getDefaultTimeRange,
    getTimeInRange,
    snapToInterval,
} from "@/utils/timeSlider";
import { impactAsync, ImpactFeedbackStyle } from "expo-haptics";
import { useCallback, useEffect, useRef } from "react";
import { LayoutChangeEvent, StyleSheet, View } from "react-native";
import {
    Gesture,
    GestureDetector,
    GestureHandlerRootView,
} from "react-native-gesture-handler";
import Animated, {
    runOnJS,
    useAnimatedStyle,
    useSharedValue,
    withSpring,
} from "react-native-reanimated";

interface TimeSliderProps {
  /** Current time value in seconds */
  value: number;
  /** Callback when value changes */
  onValueChange: (value: number) => void;
  /** Callback when gesture starts */
  onGestureStart?: () => void;
  /** Callback when gesture ends */
  onGestureEnd?: () => void;
  /** Minimum time in seconds (default: 30min) */
  min?: number;
  /** Maximum time in seconds (default: 5h) */
  max?: number;
  /** Snap interval in seconds (default: 10min) */
  interval?: number;
}

const TRACK_HEIGHT = 8;
const THUMB_SIZE = 32;
const HIT_SLOP = { top: 20, bottom: 20, left: 10, right: 10 };

const styles = StyleSheet.create({
  container: {
    width: "100%",
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.lg,
  },
  trackContainer: {
    height: TRACK_HEIGHT,
    borderRadius: TRACK_HEIGHT / 2,
    overflow: "hidden",
    position: "relative",
  },
  track: {
    flex: 1,
    borderRadius: TRACK_HEIGHT / 2,
  },
  activeTrack: {
    position: "absolute",
    left: 0,
    top: 0,
    height: TRACK_HEIGHT,
    borderRadius: TRACK_HEIGHT / 2,
  },
  thumbContainer: {
    position: "absolute",
    width: THUMB_SIZE,
    height: THUMB_SIZE,
    top: TRACK_HEIGHT / 2 - THUMB_SIZE / 2,
    marginLeft: -THUMB_SIZE / 2,
  },
  thumb: {
    width: THUMB_SIZE,
    height: THUMB_SIZE,
    borderRadius: THUMB_SIZE / 2,
    borderWidth: 3,
    backgroundColor: "white",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 4,
  },
});

/**
 * A custom slider component for selecting flight duration with haptic feedback.
 *
 * Features:
 * - Smooth gesture handling with spring animations
 * - Automatic snapping to 10-minute intervals
 * - Haptic feedback on interaction
 * - Responsive to touch with hit slop for easier interaction
 *
 * @example
 * ```tsx
 * const [flightTime, setFlightTime] = useState(3600); // 1 hour
 *
 * <TimeSlider
 *   value={flightTime}
 *   onValueChange={setFlightTime}
 * />
 * ```
 */
export function TimeSlider({
  value,
  onValueChange,
  onGestureStart,
  onGestureEnd,
  min,
  max,
  interval,
}: TimeSliderProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? "light"];

  const defaultRange = getDefaultTimeRange();
  const minValue = min ?? defaultRange.min;
  const maxValue = max ?? defaultRange.max;
  const snapValue = interval ?? defaultRange.interval;

  const trackWidth = useSharedValue(0);
  const translateX = useSharedValue(0);
  const isGestureActive = useSharedValue(false);
  const lastHapticValue = useRef(value);

  // Calculate position from value
  const valueToPosition = useCallback(
    (val: number) => {
      if (trackWidth.value === 0) return 0;
      const normalizedValue = (val - minValue) / (maxValue - minValue);
      return normalizedValue * trackWidth.value;
    },
    [minValue, maxValue, trackWidth]
  );

  // Calculate value from position
  const positionToValue = useCallback(
    (position: number) => {
      if (trackWidth.value === 0) return minValue;
      const normalizedPosition = position / trackWidth.value;
      const rawValue = normalizedPosition * (maxValue - minValue) + minValue;
      const constrained = getTimeInRange(rawValue, minValue, maxValue);
      return snapToInterval(constrained, snapValue);
    },
    [minValue, maxValue, snapValue, trackWidth]
  );

  // Trigger haptic feedback
  const triggerHaptic = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => {
      // Ignore haptic errors
    });
  }, []);

  // Update position when value changes externally
  useEffect(() => {
    if (!isGestureActive.value) {
      translateX.value = withSpring(valueToPosition(value), {
        damping: 20,
        stiffness: 200,
      });
    }
  }, [value, valueToPosition, isGestureActive, translateX]);

  // Handle track layout
  const handleTrackLayout = useCallback(
    (event: LayoutChangeEvent) => {
      const { width } = event.nativeEvent.layout;
      trackWidth.value = width;
      translateX.value = valueToPosition(value);
    },
    [trackWidth, translateX, valueToPosition, value]
  );

  // Gesture handler using new Gesture API
  const panGesture = Gesture.Pan()
    .onBegin(() => {
      isGestureActive.value = true;
      if (onGestureStart) {
        runOnJS(onGestureStart)();
      }
      runOnJS(triggerHaptic)();
    })
    .onUpdate((event) => {
      const startX = valueToPosition(value);
      const newPosition = startX + event.translationX;
      const constrainedPosition = Math.max(
        0,
        Math.min(trackWidth.value, newPosition)
      );
      translateX.value = constrainedPosition;

      const newValue = positionToValue(constrainedPosition);
      if (newValue !== value) {
        runOnJS(onValueChange)(newValue);
        runOnJS(triggerHaptic)();
      }
    })
    .onEnd(() => {
      const finalValue = positionToValue(translateX.value);
      const finalPosition = valueToPosition(finalValue);

      translateX.value = withSpring(finalPosition, {
        damping: 20,
        stiffness: 200,
      });

      isGestureActive.value = false;

      if (onGestureEnd) {
        runOnJS(onGestureEnd)();
      }
    })
    .hitSlop(HIT_SLOP);

  // Animated thumb style
  const thumbStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }],
  }));

  // Animated active track style
  const activeTrackStyle = useAnimatedStyle(() => ({
    width: translateX.value,
  }));

  return (
    <GestureHandlerRootView style={styles.container}>
      <View
        style={[styles.trackContainer, { backgroundColor: colors.border }]}
        onLayout={handleTrackLayout}
      >
        <View style={[styles.track, { backgroundColor: colors.border }]} />
        <Animated.View
          style={[
            styles.activeTrack,
            { backgroundColor: colors.tint },
            activeTrackStyle,
          ]}
        />
      </View>

      <GestureDetector gesture={panGesture}>
        <Animated.View style={[styles.thumbContainer, thumbStyle]}>
          <View style={[styles.thumb, { borderColor: colors.tint }]} />
        </Animated.View>
      </GestureDetector>
    </GestureHandlerRootView>
  );
}
