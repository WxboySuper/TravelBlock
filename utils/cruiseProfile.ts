export type CruiseAircraftType = 'E175' | 'B738' | 'B789';

export interface CruiseProfile {
  formattedAltitude: string;
  mach: string;
  trueAirspeed: number;
}

const AIRCRAFT_PERFORMANCE: Record<
  CruiseAircraftType,
  { ceilingFeet: number; mach: string; trueAirspeed: number }
> = {
  E175: {
    ceilingFeet: 41000,
    mach: 'M0.75',
    trueAirspeed: 430,
  },
  B738: {
    ceilingFeet: 41000,
    mach: 'M0.78',
    trueAirspeed: 450,
  },
  B789: {
    ceilingFeet: 43000,
    mach: 'M0.85',
    trueAirspeed: 490,
  },
};

export function resolveCruiseAircraftType(aircraftName: string): CruiseAircraftType {
  const normalized = aircraftName.toUpperCase();

  if (normalized.includes('175')) {
    return 'E175';
  }

  if (normalized.includes('737')) {
    return 'B738';
  }

  return 'B789';
}

function clampHeading(heading: number): number {
  const normalized = Math.round(heading) % 360;
  return normalized >= 0 ? normalized : normalized + 360;
}

function roundToRvsmThousand(altitudeFeet: number, heading: number): number {
  const roundedThousand = Math.round(altitudeFeet / 1000);
  const usesOddThousands = clampHeading(heading) <= 179;
  const preferredParity = usesOddThousands ? 1 : 0;

  if (roundedThousand % 2 === preferredParity) {
    return roundedThousand * 1000;
  }

  const lowerCandidate = roundedThousand - 1;
  const upperCandidate = roundedThousand + 1;

  const safeLowerCandidate = lowerCandidate >= 0 ? lowerCandidate : upperCandidate;
  const lowerDistance = Math.abs(altitudeFeet - safeLowerCandidate * 1000);
  const upperDistance = Math.abs(altitudeFeet - upperCandidate * 1000);

  return (lowerDistance <= upperDistance ? safeLowerCandidate : upperCandidate) * 1000;
}

export function calculateCruiseProfile(
  aircraftType: CruiseAircraftType,
  distanceNm: number,
  heading: number
): CruiseProfile {
  const performance = AIRCRAFT_PERFORMANCE[aircraftType];
  const safeDistanceNm = Math.max(0, distanceNm);

  const baseAltitudeFeet = safeDistanceNm * 100;
  const climbLimitedAltitudeFeet = (safeDistanceNm / 6) * 1000;
  const cappedAltitudeFeet = Math.min(
    baseAltitudeFeet,
    climbLimitedAltitudeFeet,
    performance.ceilingFeet
  );
  const rvsmAltitudeFeet = roundToRvsmThousand(cappedAltitudeFeet, heading);

  return {
    formattedAltitude: `FL${Math.round(rvsmAltitudeFeet / 100)
      .toString()
      .padStart(3, '0')}`,
    mach: performance.mach,
    trueAirspeed: performance.trueAirspeed,
  };
}
