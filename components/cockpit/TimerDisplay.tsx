/**
 * TimerDisplay component - countdown timer with phase indicator
 * 
 * Large, prominent display showing flight time remaining.
 * Changes color as time runs out (white → yellow → red).
 * 
 * @module components/cockpit/TimerDisplay
 */

import { ThemedText } from '@/components/themed-text';
import { Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { FlightPhase } from '@/types/flight';
import { StyleSheet, View } from 'react-native';

export interface TimerDisplayProps {
  /** Remaining time in seconds */
  remainingSeconds: number;
  /** Current flight phase */
  phase: FlightPhase;
  /** Progress percentage (0-100) */
  progressPercent: number;
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    paddingVertical: Spacing.lg,
  },
  timerText: {
    fontSize: 64,
    fontWeight: Typography.fontWeight.bold as any,
    letterSpacing: -2,
    fontVariant: ['tabular-nums'] as any,
  },
  phaseContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: Spacing.sm,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
    borderRadius: 16,
  },
  phaseIcon: {
    fontSize: Typography.fontSize.base,
    marginRight: Spacing.xs,
  },
  phaseText: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as any,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});

/**
 * Format seconds to MM:SS or HH:MM:SS
 */
function formatTime(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
  
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

/**
 * Get timer color based on remaining time percentage
 */
function getTimerColor(progressPercent: number, colors: any): string {
  if (progressPercent >= 95) {
    // < 5% remaining - red
    return '#EF4444';
  }
  if (progressPercent >= 90) {
    // < 10% remaining - yellow
    return '#F59E0B';
  }
  // Normal - white/text color
  return colors.text;
}

/**
 * Get phase display info (icon and color)
 */
function getPhaseInfo(phase: FlightPhase): { icon: string; color: string; label: string } {
  switch (phase) {
    case 'climbing':
      return { icon: '✈️', color: '#3B82F6', label: 'Climbing' };
    case 'cruising':
      return { icon: '➡️', color: '#14B8A6', label: 'Cruising' };
    case 'descending':
      return { icon: '🔽', color: '#10B981', label: 'Descending' };
    default:
      // This should never happen
      return { icon: '➡️', color: '#14B8A6', label: 'Cruising' };
  }
}

/**
 * TimerDisplay component
 * 
 * Shows flight countdown timer with current phase indicator.
 * Timer changes color as time runs low.
 * 
 * @example
 * ```tsx
 * <TimerDisplay
 *   remainingSeconds={3600}
 *   phase="cruising"
 *   progressPercent={50}
 * />
 * ```
 */
export function TimerDisplay({ remainingSeconds, phase, progressPercent }: TimerDisplayProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  const formattedTime = formatTime(Math.max(remainingSeconds, 0));
  const timerColor = getTimerColor(progressPercent, colors);
  const phaseInfo = getPhaseInfo(phase);

  return (
    <View style={styles.container}>
      <ThemedText style={[styles.timerText, { color: timerColor }]}>
        {formattedTime}
      </ThemedText>
      
      <View style={[styles.phaseContainer, { backgroundColor: phaseInfo.color }]}>
        <ThemedText style={[styles.phaseIcon, { color: '#FFFFFF' }]}>
          {phaseInfo.icon}
        </ThemedText>
        <ThemedText style={[styles.phaseText, { color: '#FFFFFF' }]}>
          {phaseInfo.label}
        </ThemedText>
      </View>
    </View>
  );
}
