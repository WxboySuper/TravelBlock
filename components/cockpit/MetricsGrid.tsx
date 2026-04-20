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
import { FlightPhase, type FlightProgress } from '@/types/flight';
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

function getPhaseBanner(progress: FlightProgress) {
  switch (progress.currentPhase) {
    case FlightPhase.Cruising:
      return {
        detail: 'Cruise altitude reached. Cabin metrics are now in steady-state.',
        eyebrow: 'Flight Phase',
        value: 'Cruise Locked',
        variant: 'cruising' as const,
      };
    case FlightPhase.Descending:
      return {
        detail: 'Descent profile active. Expect altitude and speed to taper down.',
        eyebrow: 'Flight Phase',
        value: 'Descent Active',
        variant: 'descending' as const,
      };
    default:
      return {
        detail: 'Climb profile active. Aircraft is still accelerating toward cruise.',
        eyebrow: 'Flight Phase',
        value: 'Climb Active',
        variant: 'climbing' as const,
      };
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
  const phaseBanner = getPhaseBanner(progress);

  return (
    <View style={styles.container}>
      <MetricCard
        description={phaseBanner.detail}
        emphasisLabel={progress.currentPhase === FlightPhase.Cruising ? 'Stable' : 'Tracking'}
        fullWidth
        icon={progress.currentPhase === FlightPhase.Cruising ? 'check' : progress.currentPhase === FlightPhase.Descending ? 'descend' : 'climb'}
        label={phaseBanner.eyebrow}
        value={phaseBanner.value}
        variant={phaseBanner.variant}
      />

      {/* Top row: Speed & Altitude */}
      <View style={styles.row}>
        <MetricCard
          label="Speed"
          value={formatSpeed(progress.currentSpeed)}
          icon="speed"
          variant={phaseVariant}
        />
        <MetricCard
          label="Altitude"
          value={formatAltitude(progress.currentAltitude)}
          icon="altitude"
          variant={phaseVariant}
        />
      </View>

      {/* Middle row: Heading & Vertical Speed */}
      <View style={styles.row}>
        <MetricCard
          label="Heading"
          value={formatHeading(progress.heading)}
          icon="heading"
        />
        <MetricCard
          label="V/S"
          value={formatVerticalSpeed(verticalSpeed)}
          icon={verticalSpeed > 0 ? 'climb' : verticalSpeed < 0 ? 'descend' : 'cruise'}
        />
      </View>

      {/* Bottom row: Distance */}
      <View style={styles.row}>
        <MetricCard
          label="Flown"
          value={formatDistance(progress.distanceFlown)}
          icon="check"
        />
        <MetricCard
          label="Remaining"
          value={formatDistance(progress.distanceRemaining)}
          icon="distance"
        />
      </View>
    </View>
  );
}
