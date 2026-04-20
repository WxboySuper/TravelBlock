import { calculateCruiseProfile } from '@/utils/cruiseProfile';

describe('calculateCruiseProfile', () => {
  it('returns odd-thousand RVSM altitude for eastbound/westbound headings 0-179', () => {
    expect(calculateCruiseProfile('B738', 312, 90)).toEqual({
      formattedAltitude: 'FL310',
      mach: 'M0.78',
      trueAirspeed: 450,
    });
  });

  it('returns even-thousand RVSM altitude for headings 180-359', () => {
    expect(calculateCruiseProfile('B738', 312, 225)).toEqual({
      formattedAltitude: 'FL320',
      mach: 'M0.78',
      trueAirspeed: 450,
    });
  });

  it('applies aircraft ceiling constraints', () => {
    expect(calculateCruiseProfile('E175', 700, 0)).toEqual({
      formattedAltitude: 'FL410',
      mach: 'M0.75',
      trueAirspeed: 430,
    });

    expect(calculateCruiseProfile('B789', 700, 180)).toEqual({
      formattedAltitude: 'FL420',
      mach: 'M0.85',
      trueAirspeed: 490,
    });
  });

  it('applies the climb constraint before formatting', () => {
    expect(calculateCruiseProfile('E175', 30, 0)).toEqual({
      formattedAltitude: 'FL030',
      mach: 'M0.75',
      trueAirspeed: 430,
    });
  });
});
