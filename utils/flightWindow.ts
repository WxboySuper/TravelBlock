import { getFlightTimeBucket } from '@/services/radiusService';
import { formatTimeValue, TIME_SLIDER_CONFIG } from '@/utils/timeSlider';

export function formatFlightWindowLabel(flightTimeInSeconds: number): string {
  const bucket = getFlightTimeBucket({
    timeInSeconds: flightTimeInSeconds,
    bucketSizeInSeconds: TIME_SLIDER_CONFIG.SNAP_INTERVAL,
    initialBucketMaxTime: TIME_SLIDER_CONFIG.MIN_TIME,
  });
  const maxLabel = formatTimeValue(bucket.maxTimeInSeconds).formatted;

  if (bucket.minTimeInSeconds === 0) {
    return `Up to ${maxLabel}`;
  }

  const minLabel = formatTimeValue(bucket.minTimeInSeconds).formatted;
  return `${minLabel} - ${maxLabel}`;
}
