import { FlightPhase } from '@/types/flight';
import {
  FLIGHT_CONSTANTS,
  calculateAltitude,
  calculateVerticalSpeed,
  getDescentStartPercent,
  getFlightPhase,
  getMaxAltitude,
} from '@/utils/flightMetrics';

describe('getMaxAltitude', () => {
  it('keeps very short hops low instead of climbing into the mid-20s', () => {
    expect(getMaxAltitude(20, 'Embraer E-175', 180)).toBe(3000);
  });

  it('uses the aircraft-aware cruise profile for longer sectors', () => {
    expect(getMaxAltitude(926, 'Boeing 787-9 Dreamliner', 90)).toBe(43000);
  });
});

describe('descent profile', () => {
  it('computes top of descent from route distance and cruise profile', () => {
    const shortHopTod = getDescentStartPercent(20, 'Embraer E-175', 180);
    const longHaulTod = getDescentStartPercent(926, 'Boeing 787-9 Dreamliner', 90);

    expect(shortHopTod).toBe(15);
    expect(longHaulTod).toBeGreaterThan(68);
    expect(longHaulTod).toBeLessThan(80);
  });

  it('keeps more altitude early in descent for a gentler profile', () => {
    const descentStartPercent = getDescentStartPercent(926, 'Boeing 787-9 Dreamliner', 90);
    expect(
      calculateAltitude(
        descentStartPercent + 8,
        FlightPhase.Descending,
        43000,
        { descentStartPercent }
      )
    ).toBeGreaterThan(24000);
  });

  it('uses a softer nominal descent rate', () => {
    expect(calculateVerticalSpeed(FlightPhase.Descending, 12000, 0)).toBe(-1100);
  });

  it('switches from cruise to descent at the computed top of descent', () => {
    const descentStartPercent = getDescentStartPercent(926, 'Boeing 787-9 Dreamliner', 90);

    expect(getFlightPhase(descentStartPercent - 0.1, { descentStartPercent })).toBe(FlightPhase.Cruising);
    expect(getFlightPhase(descentStartPercent, { descentStartPercent })).toBe(FlightPhase.Descending);
    expect(FLIGHT_CONSTANTS.CLIMB_END_PERCENT).toBe(10);
  });
});
