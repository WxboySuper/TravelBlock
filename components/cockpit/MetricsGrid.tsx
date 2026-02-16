/**
 * MetricsGrid component - displays flight metrics in a grid
 * 
 * Shows 6 key flight metrics in a 2x3 grid layout:
 * - Speed & Altitude (top row)
 * - Heading & Vertical Speed (middle row)
 * - Distance & Time (bottom row)
 * 
 * @module components/cockpit/MetricsGrid
 */

import { Spacing } from '@/constants/theme';
import type { FlightProgress } from '@/types/flight';
import {
    formatAltitude,
    formatHeading,
    formatSpeed,
    formatVerticalSpeed,
} from '@/utils/flightMetrics';
import { StyleSheet, View } from 'react-native';
import { MetricCard } from './MetricCard';

export interface MetricsGridProps {
  /** Current flight progress data */
  progress: FlightProgress;
}

const styles = StyleSheet.create({
  container: {
    padding: Spacing.md,
  },
  row: {
    flexDirection: 'row',
    gap: Spacing.md,
    marginBottom: Spacing.md,
  },
});

/**
 * Format distance for display
 */
function formatDistance(distanceKm: number): string {
  // Convert to miles
  const miles = distanceKm * 0.621371;
  return `${Math.round(miles)} mi`;
}

/**
 * Format time for display
 */
function formatTime(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  
  return `${minutes}m`;
}

/**
 * Calculate vertical speed from progress
 * (This would normally come from the timer service, but we can estimate it)
 */
function estimateVerticalSpeed(progress: FlightProgress): number {
  switch (progress.currentPhase) {
    case 'climbing':
      return 2000; // +2000 fpm
    case 'descending':
      return -1500; // -1500 fpm
    default:
      return 0; // Level flight
  }
}

/**
 * MetricsGrid component
 * 
 * Displays flight metrics in a responsive grid layout.
 * 
 * @example
 * ```tsx
 * <MetricsGrid progress={flightProgress} />
 * ```
 */
export function MetricsGrid({ progress }: MetricsGridProps) {
  const phaseVariant = progress.currentPhase;
  const verticalSpeed = estimateVerticalSpeed(progress);

  return (
    <View style={styles.container}>
      {/* Top row: Speed & Altitude */}
      <View style={styles.row}>
        <MetricCard
          label="Speed"
          value={formatSpeed(progress.currentSpeed)}
          icon="🚀"
          variant={phaseVariant}
        />
        <MetricCard
          label="Altitude"
          value={formatAltitude(progress.currentAltitude)}
          icon="📈"
          variant={phaseVariant}
        />
      </View>

      {/* Middle row: Heading & Vertical Speed */}
      <View style={styles.row}>
        <MetricCard
          label="Heading"
          value={formatHeading(progress.heading)}
          icon="🧭"
        />
        <MetricCard
          label="V/S"
          value={formatVerticalSpeed(verticalSpeed)}
          icon={verticalSpeed > 0 ? '⬆️' : verticalSpeed < 0 ? '⬇️' : '➡️'}
        />
      </View>

      {/* Bottom row: Distance */}
      <View style={styles.row}>
        <MetricCard
          label="Flown"
          value={formatDistance(progress.distanceFlown)}
          icon="✓"
        />
        <MetricCard
          label="Remaining"
          value={formatDistance(progress.distanceRemaining)}
          icon="→"
        />
      </View>
    </View>
  );
}
