jest.mock('react-native', () => ({
  Alert: {
    alert: jest.fn(),
  },
}));

import {
  getHomeAirportSnapshot,
  loadHomeAirport,
  resetHomeAirportStoreForTests,
  subscribeToHomeAirportState,
} from '@/hooks/useHomeAirport';
import { storageService } from '@/services/storageService';
import type { Airport } from '@/types/airport';

jest.mock('@/services/storageService', () => ({
  storageService: {
    getHomeAirport: jest.fn(),
    saveHomeAirport: jest.fn(),
    clearHomeAirport: jest.fn(),
  },
}));

const airportA: Airport = {
  icao: 'KORD',
  iata: 'ORD',
  name: "O'Hare International Airport",
  city: 'Chicago',
  state: 'IL',
  country: 'US',
  elevation: 672,
  lat: 41.9742,
  lon: -87.9073,
  tz: 'America/Chicago',
};

const airportB: Airport = {
  icao: 'KSEA',
  iata: 'SEA',
  name: 'Seattle-Tacoma International Airport',
  city: 'Seattle',
  state: 'WA',
  country: 'US',
  elevation: 433,
  lat: 47.4502,
  lon: -122.3088,
  tz: 'America/Los_Angeles',
};

describe('homeAirport shared store', () => {
  beforeEach(() => {
    resetHomeAirportStoreForTests();
    jest.clearAllMocks();
  });

  it('loads the home airport once and shares the result with all subscribers', async () => {
    jest.mocked(storageService.getHomeAirport).mockResolvedValue(airportA);
    const snapshots: Airport[] = [];

    const unsubscribeA = subscribeToHomeAirportState((state) => {
      if (state.homeAirport) {
        snapshots.push(state.homeAirport);
      }
    });
    const secondaryListener = jest.fn();
    const unsubscribeB = subscribeToHomeAirportState(secondaryListener);

    await Promise.all([loadHomeAirport(), loadHomeAirport()]);

    expect(storageService.getHomeAirport).toHaveBeenCalledTimes(1);
    expect(getHomeAirportSnapshot()).toEqual({
      homeAirport: airportA,
      isLoading: false,
    });
    expect(snapshots).toEqual([airportA]);

    unsubscribeA();
    unsubscribeB();
  });

  it('replaces the cached airport when a refresh is requested', async () => {
    jest.mocked(storageService.getHomeAirport)
      .mockResolvedValueOnce(airportA)
      .mockResolvedValueOnce(airportB);

    await loadHomeAirport();
    expect(getHomeAirportSnapshot().homeAirport?.icao).toBe('KORD');

    await loadHomeAirport(true);
    expect(getHomeAirportSnapshot().homeAirport?.icao).toBe('KSEA');
    expect(storageService.getHomeAirport).toHaveBeenCalledTimes(2);
  });
});
