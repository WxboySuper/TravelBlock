import React from 'react';
import TestRenderer, { act } from 'react-test-renderer';

import { useDestinations } from '@/hooks/useDestinations';
import { loadAirports } from '@/services/airportService';
import { getDestinationsByFlightTime, getDestinationsInTimeRange } from '@/services/radiusService';
import type { Airport } from '@/types/airport';
import type { AirportWithFlightTime } from '@/types/radius';

jest.mock('@/services/airportService', () => ({
  loadAirports: jest.fn(),
}));

jest.mock('@/services/radiusService', () => ({
  getDestinationsByFlightTime: jest.fn(),
  getDestinationsInTimeRange: jest.fn(),
}));

const mockOrigin: Airport = {
  icao: 'KOKC',
  iata: 'OKC',
  name: 'Will Rogers World Airport',
  city: 'Oklahoma City',
  country: 'US',
  state: 'OK',
  lat: 35.3931,
  lon: -97.6007,
  elevation: 1295,
  tz: 'America/Chicago',
};

const mockDestinations: AirportWithFlightTime[] = [
  {
    icao: 'KDFW',
    iata: 'DFW',
    name: 'Dallas/Fort Worth International Airport',
    city: 'Dallas-Fort Worth',
    country: 'US',
    state: 'TX',
    lat: 32.8998,
    lon: -97.0403,
    elevation: 607,
    tz: 'America/Chicago',
    flightTime: 7200,
    distance: 180,
  },
];

type UseDestinationsProps = Parameters<typeof useDestinations>[0];
type TestRendererInstance = ReturnType<typeof TestRenderer.create>;

function renderUseDestinations({
  origin,
  flightTimeInSeconds,
  useTimeRange,
  debounceMs,
}: UseDestinationsProps) {
  let latestResult: ReturnType<typeof useDestinations> | null = null;
  let renderer: TestRendererInstance | null = null;

  function Harness(props: UseDestinationsProps) {
    latestResult = useDestinations(props);
    return null;
  }

  act(() => {
    renderer = TestRenderer.create(
      React.createElement(Harness, {
        origin,
        flightTimeInSeconds,
        useTimeRange,
        debounceMs,
      })
    );
  });

  return {
    get current() {
      if (!latestResult) {
        throw new Error('Hook result not ready');
      }
      return latestResult;
    },
    rerender(nextProps: UseDestinationsProps) {
      act(() => {
        if (!renderer) {
          throw new Error('Hook renderer not initialized');
        }

        renderer.update(React.createElement(Harness, nextProps));
      });
    },
    unmount() {
      act(() => {
        if (!renderer) {
          throw new Error('Hook renderer not initialized');
        }

        renderer.unmount();
      });
    },
  };
}

describe('useDestinations', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.clearAllMocks();
    (loadAirports as jest.Mock).mockImplementation(() => Promise.resolve());
    (getDestinationsByFlightTime as jest.Mock).mockReturnValue(mockDestinations);
    (getDestinationsInTimeRange as jest.Mock).mockReturnValue([]);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('loads the airport cache before calculating destinations', async () => {
    let resolveLoadAirports: (() => void) | null = null;
    (loadAirports as jest.Mock).mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveLoadAirports = resolve;
        })
    );

    const hook = renderUseDestinations({
      origin: mockOrigin,
      flightTimeInSeconds: 7200,
      useTimeRange: false,
      debounceMs: 5,
    });

    expect(hook.current.isLoading).toBe(false);
    expect(hook.current.destinations).toEqual([]);

    act(() => {
      jest.advanceTimersByTime(5);
    });

    expect(loadAirports).toHaveBeenCalledTimes(1);
    expect(hook.current.isLoading).toBe(true);
    expect(getDestinationsByFlightTime).not.toHaveBeenCalled();

    act(() => {
      resolveLoadAirports?.();
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(getDestinationsByFlightTime).toHaveBeenCalledTimes(1);
    expect(getDestinationsByFlightTime).toHaveBeenCalledWith(
      { lat: mockOrigin.lat, lon: mockOrigin.lon },
      7200
    );
    expect(hook.current.destinations).toEqual(mockDestinations);
    expect(hook.current.isLoading).toBe(false);

    hook.unmount();
  });

  it('ignores in-flight destination work after unmount', async () => {
    let resolveLoadAirports: (() => void) | null = null;
    (loadAirports as jest.Mock).mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveLoadAirports = resolve;
        })
    );

    const hook = renderUseDestinations({
      origin: mockOrigin,
      flightTimeInSeconds: 7200,
      useTimeRange: false,
      debounceMs: 5,
    });

    act(() => {
      jest.advanceTimersByTime(5);
    });

    hook.unmount();

    act(() => {
      resolveLoadAirports?.();
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(getDestinationsByFlightTime).not.toHaveBeenCalled();
  });
});
