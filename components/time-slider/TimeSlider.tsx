import { Colors, Spacing } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { getDefaultTimeRange, getTimeInRange, snapToInterval } from "@/utils/timeSlider";
import { impactAsync, ImpactFeedbackStyle } from "expo-haptics";
import { useCallback, useEffect, useMemo, useRef } from "react";
import {
  Animated,
  LayoutChangeEvent,
  PanResponder,
  Pressable,
  StyleSheet,
  View,
} from "react-native";

interface TimeSliderProps {
  value: number;
  onValueChange: (value: number) => void;
  onGestureStart?: () => void;
  onGestureEnd?: () => void;
  min?: number;
  max?: number;
  interval?: number;
}

const TRACK_HEIGHT = 8;
const THUMB_SIZE = 32;
const SPRING_CONFIG = { damping: 20, stiffness: 200, useNativeDriver: true };

const styles = StyleSheet.create({
  container: {
    width: "100%",
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.lg,
  },
  sliderSurface: {
    height: THUMB_SIZE,
    justifyContent: "center",
  },
  trackContainer: {
    height: TRACK_HEIGHT,
    borderRadius: TRACK_HEIGHT / 2,
    overflow: "hidden",
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

function clampPosition(trackWidth: number, position: number) {
  return Math.max(0, Math.min(trackWidth, position));
}

function valueToPosition(trackWidth: number, minValue: number, maxValue: number, value: number) {
  if (trackWidth === 0) return 0;
  const normalizedValue = (value - minValue) / (maxValue - minValue);
  return normalizedValue * trackWidth;
}

function positionToValue(
  trackWidth: number,
  minValue: number,
  maxValue: number,
  snapValue: number,
  position: number
) {
  if (trackWidth === 0) return minValue;
  const normalizedPosition = position / trackWidth;
  const rawValue = normalizedPosition * (maxValue - minValue) + minValue;
  const constrained = getTimeInRange(rawValue, minValue, maxValue);
  return snapToInterval(constrained, snapValue);
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
  const range = useMemo(
    () => ({
      minValue: min ?? defaultRange.min,
      maxValue: max ?? defaultRange.max,
      snapValue: interval ?? defaultRange.interval,
    }),
    [defaultRange.interval, defaultRange.max, defaultRange.min, interval, max, min]
  );

  const translateX = useRef(new Animated.Value(0)).current;
  const trackWidthRef = useRef(0);
  const currentPositionRef = useRef(0);
  const gestureStartRef = useRef(0);
  const isDraggingRef = useRef(false);
  const lastEmittedValueRef = useRef(value);

  const triggerHaptic = useCallback(() => {
    impactAsync(ImpactFeedbackStyle.Light).catch(() => undefined);
  }, []);

  const setPosition = useCallback((position: number) => {
    currentPositionRef.current = position;
    translateX.setValue(position);
  }, [translateX]);

  const animateToPosition = useCallback((position: number) => {
    currentPositionRef.current = position;
    Animated.spring(translateX, {
      toValue: position,
      ...SPRING_CONFIG,
    }).start();
  }, [translateX]);

  const commitValue = useCallback((nextValue: number, withHaptic: boolean) => {
    if (nextValue === lastEmittedValueRef.current) {
      return;
    }

    lastEmittedValueRef.current = nextValue;
    animateToPosition(
      valueToPosition(trackWidthRef.current, range.minValue, range.maxValue, nextValue)
    );
    onValueChange(nextValue);

    if (withHaptic) {
      triggerHaptic();
    }
  }, [animateToPosition, onValueChange, range.maxValue, range.minValue, triggerHaptic]);

  useEffect(() => {
    if (isDraggingRef.current) {
      return;
    }

    lastEmittedValueRef.current = value;
    animateToPosition(
      valueToPosition(trackWidthRef.current, range.minValue, range.maxValue, value)
    );
  }, [animateToPosition, range.maxValue, range.minValue, value]);

  const handleTrackLayout = useCallback((event: LayoutChangeEvent) => {
    trackWidthRef.current = event.nativeEvent.layout.width;
    animateToPosition(
      valueToPosition(trackWidthRef.current, range.minValue, range.maxValue, lastEmittedValueRef.current)
    );
  }, [animateToPosition, range.maxValue, range.minValue]);

  const handleTrackPress = useCallback((locationX: number) => {
    const nextValue = positionToValue(
      trackWidthRef.current,
      range.minValue,
      range.maxValue,
      range.snapValue,
      clampPosition(trackWidthRef.current, locationX)
    );
    commitValue(nextValue, true);
  }, [commitValue, range.maxValue, range.minValue, range.snapValue]);

  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onMoveShouldSetPanResponder: () => true,
        onStartShouldSetPanResponder: () => true,
        onPanResponderGrant: () => {
          isDraggingRef.current = true;
          gestureStartRef.current = currentPositionRef.current;
          onGestureStart?.();
          triggerHaptic();
        },
        onPanResponderMove: (_, gestureState) => {
          const nextPosition = clampPosition(
            trackWidthRef.current,
            gestureStartRef.current + gestureState.dx
          );
          setPosition(nextPosition);

          const nextValue = positionToValue(
            trackWidthRef.current,
            range.minValue,
            range.maxValue,
            range.snapValue,
            nextPosition
          );

          if (nextValue !== lastEmittedValueRef.current) {
            lastEmittedValueRef.current = nextValue;
            onValueChange(nextValue);
            triggerHaptic();
          }
        },
        onPanResponderRelease: () => {
          const finalValue = positionToValue(
            trackWidthRef.current,
            range.minValue,
            range.maxValue,
            range.snapValue,
            currentPositionRef.current
          );
          isDraggingRef.current = false;
          animateToPosition(
            valueToPosition(trackWidthRef.current, range.minValue, range.maxValue, finalValue)
          );
          lastEmittedValueRef.current = finalValue;
          onGestureEnd?.();
        },
        onPanResponderTerminate: () => {
          isDraggingRef.current = false;
          onGestureEnd?.();
        },
      }),
    [
      animateToPosition,
      onGestureEnd,
      onGestureStart,
      onValueChange,
      range.maxValue,
      range.minValue,
      range.snapValue,
      setPosition,
      triggerHaptic,
    ]
  );

  return (
    <View style={styles.container}>
      <View style={styles.sliderSurface}>
        <Pressable
          onPress={(event) => handleTrackPress(event.nativeEvent.locationX)}
          style={[styles.trackContainer, { backgroundColor: colors.border }]}
          onLayout={handleTrackLayout}
        >
          <View style={[styles.track, { backgroundColor: colors.border }]} />
          <Animated.View
            style={[
              styles.activeTrack,
              { backgroundColor: colors.tint, width: translateX },
            ]}
          />
        </Pressable>

        <Animated.View
          style={[styles.thumbContainer, { transform: [{ translateX }] }]}
          {...panResponder.panHandlers}
        >
          <View style={[styles.thumb, { borderColor: colors.tint }]} />
        </Animated.View>
      </View>
    </View>
  );
}
