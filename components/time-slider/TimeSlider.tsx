import { Colors, Spacing } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { getDefaultTimeRange, getTimeInRange, snapToInterval } from "@/utils/timeSlider";
import { impactAsync, ImpactFeedbackStyle } from "expo-haptics";
import { useCallback, useEffect } from "react";
import { LayoutChangeEvent, StyleSheet, View } from "react-native";
import { Gesture, GestureDetector } from "react-native-gesture-handler";
import Animated, {
  runOnJS,
  SharedValue,
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
const SPRING_CONFIG = { damping: 20, stiffness: 200 };

const styles = StyleSheet.create({
  container: {
    width: "100%",
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.lg,
    position: "relative",
  },
  sliderSurface: {
    height: THUMB_SIZE,
    justifyContent: "center",
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

interface SliderRange {
  minValue: number;
  maxValue: number;
  snapValue: number;
}

interface SliderSharedState {
  gestureStartX: SharedValue<number>;
  isGestureActive: SharedValue<boolean>;
  lastEmittedValue: SharedValue<number>;
  trackWidth: SharedValue<number>;
  translateX: SharedValue<number>;
}

function useSliderSharedState(value: number): SliderSharedState {
  return {
    trackWidth: useSharedValue(0),
    translateX: useSharedValue(0),
    gestureStartX: useSharedValue(0),
    isGestureActive: useSharedValue(false),
    lastEmittedValue: useSharedValue(value),
  };
}

function clampPosition(trackWidth: SharedValue<number>, position: number) {
  "worklet";
  return Math.max(0, Math.min(trackWidth.value, position));
}

function valueToPosition(trackWidth: SharedValue<number>, range: SliderRange, value: number) {
  "worklet";
  if (trackWidth.value === 0) return 0;
  const normalizedValue = (value - range.minValue) / (range.maxValue - range.minValue);
  return normalizedValue * trackWidth.value;
}

function positionToValue(trackWidth: SharedValue<number>, range: SliderRange, position: number) {
  "worklet";
  if (trackWidth.value === 0) return range.minValue;
  const normalizedPosition = position / trackWidth.value;
  const rawValue = normalizedPosition * (range.maxValue - range.minValue) + range.minValue;
  const constrained = getTimeInRange(rawValue, range.minValue, range.maxValue);
  return snapToInterval(constrained, range.snapValue);
}

function createSliderGestures(
  range: SliderRange,
  shared: SliderSharedState,
  onGestureStart: (() => void) | undefined,
  onGestureEnd: (() => void) | undefined,
  onValueChange: (value: number) => void,
  triggerHaptic: () => void
) {
  const panGesture = Gesture.Pan()
    .onBegin(() => {
      shared.isGestureActive.value = true;
      shared.gestureStartX.value = shared.translateX.value;
      if (onGestureStart) {
        runOnJS(onGestureStart)();
      }
      runOnJS(triggerHaptic)();
    })
    .onUpdate((event) => {
      const nextPosition = clampPosition(shared.trackWidth, shared.gestureStartX.value + event.translationX);
      shared.translateX.value = nextPosition;

      const nextValue = positionToValue(shared.trackWidth, range, nextPosition);
      if (nextValue !== shared.lastEmittedValue.value) {
        shared.lastEmittedValue.value = nextValue;
        runOnJS(onValueChange)(nextValue);
        runOnJS(triggerHaptic)();
      }
    })
    .onEnd(() => {
      const finalValue = positionToValue(shared.trackWidth, range, shared.translateX.value);
      shared.lastEmittedValue.value = finalValue;
      shared.translateX.value = withSpring(
        valueToPosition(shared.trackWidth, range, finalValue),
        SPRING_CONFIG
      );
      shared.isGestureActive.value = false;

      if (onGestureEnd) {
        runOnJS(onGestureEnd)();
      }
    })
    .hitSlop(HIT_SLOP);

  const tapGesture = Gesture.Tap().onEnd((event) => {
    const nextValue = positionToValue(
      shared.trackWidth,
      range,
      clampPosition(shared.trackWidth, event.x)
    );

    shared.isGestureActive.value = false;
    shared.lastEmittedValue.value = nextValue;
    shared.translateX.value = withSpring(
      valueToPosition(shared.trackWidth, range, nextValue),
      SPRING_CONFIG
    );

    runOnJS(onValueChange)(nextValue);
    runOnJS(triggerHaptic)();
  });

  return Gesture.Simultaneous(panGesture, tapGesture);
}

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
  const range = {
    minValue: min ?? defaultRange.min,
    maxValue: max ?? defaultRange.max,
    snapValue: interval ?? defaultRange.interval,
  };
  const shared = useSliderSharedState(value);

  const triggerHaptic = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
  }, []);

  useEffect(() => {
    if (!shared.isGestureActive.value) {
      shared.lastEmittedValue.value = value;
      shared.translateX.value = withSpring(
        valueToPosition(shared.trackWidth, range, value),
        SPRING_CONFIG
      );
    }
  }, [range, shared, value]);

  const handleTrackLayout = useCallback(
    (event: LayoutChangeEvent) => {
      const { width } = event.nativeEvent.layout;
      shared.trackWidth.value = width;
      shared.translateX.value = valueToPosition(shared.trackWidth, range, value);
    },
    [range, shared, value]
  );

  const sliderGesture = createSliderGestures(
    range,
    shared,
    onGestureStart,
    onGestureEnd,
    onValueChange,
    triggerHaptic
  );

  const thumbStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: shared.translateX.value }],
  }));

  const activeTrackStyle = useAnimatedStyle(() => ({
    width: shared.translateX.value,
  }));

  return (
    <View style={styles.container}>
      <GestureDetector gesture={sliderGesture}>
        <View style={styles.sliderSurface}>
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

          <Animated.View style={[styles.thumbContainer, thumbStyle]}>
            <View style={[styles.thumb, { borderColor: colors.tint }]} />
          </Animated.View>
        </View>
      </GestureDetector>
    </View>
  );
}
