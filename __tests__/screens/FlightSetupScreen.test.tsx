import React from 'react';
import TestRenderer, { act } from 'react-test-renderer';

import FlightSetupScreen from '@/screens/FlightSetupScreen';
import { loadAirports } from '@/services/airportService';
import { getDestinationsByFlightTime, getDestinationsInTimeRange } from '@/services/radiusService';

jest.mock('react-native', () => {
  const React = require('react');

  const createHostComponent = (name: string) => {
    return ({ children, ...props }: any) => React.createElement(name, props, children);
  };

  return {
    Platform: {
      OS: 'ios',
      select: (mapping: Record<string, unknown>) => mapping.ios ?? mapping.default,
    },
    View: createHostComponent('View'),
    Text: createHostComponent('Text'),
    Pressable: createHostComponent('Pressable'),
    TouchableOpacity: createHostComponent('TouchableOpacity'),
    SafeAreaView: createHostComponent('SafeAreaView'),
    StyleSheet: {
      create: (styles: Record<string, unknown>) => styles,
    },
  };
});

jest.mock('@/components/airport/AirportCard', () => ({
  AirportCard: ({ airport }: { airport: { icao: string } }) => {
    const React = require('react');
    return React.createElement('Text', { testID: 'home-airport' }, airport.icao);
  },
}));

jest.mock('@/components/destinations/DestinationsList', () => ({
  DestinationsList: ({
    destinations,
    isLoading,
    error,
  }: {
    destinations: Array<{ city: string }>;
    isLoading: boolean;
    error: string | null;
  }) => {
    const React = require('react');
    if (isLoading) {
      return React.createElement('Text', { testID: 'destinations-state' }, 'loading');
    }
    if (error) {
      return React.createElement('Text', { testID: 'destinations-state' }, error);
    }

    return React.createElement(
      'Text',
      { testID: 'destinations-state' },
      destinations.map((destination) => destination.city).join(',')
    );
  },
}));

jest.mock('@/components/time-slider/TimeSlider', () => ({
  TimeSlider: ({ onValueChange }: { onValueChange: (value: number) => void }) => {
    const React = require('react');
    return React.createElement(
      'Pressable',
      {
        testID: 'mock-time-slider',
        onPress: () => onValueChange(10800),
      },
      React.createElement('Text', null, 'Mock Slider')
    );
  },
}));

jest.mock('@/components/time-slider/TimeValue', () => ({
  TimeValue: ({ seconds }: { seconds: number }) => {
    const React = require('react');
    return React.createElement('Text', { testID: 'time-value' }, String(seconds));
  },
}));

jest.mock('@/components/themed-text', () => ({
  ThemedText: ({ children }: { children: React.ReactNode }) => {
    const React = require('react');
    return React.createElement('Text', null, children);
  },
}));

jest.mock('@/components/themed-view', () => ({
  ThemedView: ({ children }: { children: React.ReactNode }) => {
    const React = require('react');
    return React.createElement('View', null, children);
  },
}));

jest.mock('@/components/ui/Button', () => ({
  Button: ({ title }: { title: string }) => {
    const React = require('react');
    return React.createElement('Text', { testID: 'review-flight-button' }, title);
  },
}));

jest.mock('@/components/ui/icon-symbol', () => ({
  IconSymbol: () => {
    const React = require('react');
    return React.createElement('Text', { testID: 'icon-symbol' }, 'icon');
  },
}));

jest.mock('expo-haptics', () => ({
  impactAsync: jest.fn().mockResolvedValue(undefined),
  ImpactFeedbackStyle: {
    Light: 'light',
    Heavy: 'heavy',
  },
}));

jest.mock('react-native-safe-area-context', () => ({
  SafeAreaView: ({ children, ...props }: { children: React.ReactNode }) => {
    const React = require('react');
    return React.createElement('SafeAreaView', props, children);
  },
}));

jest.mock('@/hooks/use-color-scheme', () => ({
  useColorScheme: () => 'light',
}));

jest.mock('@/hooks/useHomeAirport', () => ({
  useHomeAirport: () => ({
    homeAirport: {
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
    },
    isLoading: false,
    handleSelectAirport: jest.fn(),
    handleClearHomeBase: jest.fn(),
    refreshHomeAirport: jest.fn(),
  }),
}));

jest.mock('@/context/FlightContext', () => ({
  useFlight: () => ({
    origin: null,
    destination: null,
    flightDuration: 3600,
    setOrigin: jest.fn(),
    setDestination: jest.fn(),
    setFlightDuration: jest.fn(),
  }),
}));

jest.mock('expo-router', () => ({
  useRouter: () => ({
    back: jest.fn(),
    push: jest.fn(),
  }),
}));

jest.mock('@/services/airportService', () => ({
  loadAirports: jest.fn(),
}));

jest.mock('@/services/radiusService', () => ({
  getDestinationsByFlightTime: jest.fn(),
  getDestinationsInTimeRange: jest.fn(),
}));

const mockDestinations = [
  {
    icao: 'KOUN',
    iata: 'OUN',
    name: 'University of Oklahoma Westheimer Airport',
    city: 'Norman',
    country: 'US',
    state: 'OK',
    lat: 35.2456,
    lon: -97.4721,
    elevation: 1162,
    tz: 'America/Chicago',
    distance: 20,
    flightTime: 2400,
  },
  {
    icao: 'KSWO',
    iata: 'SWO',
    name: 'Stillwater Regional Airport',
    city: 'Stillwater',
    country: 'US',
    state: 'OK',
    lat: 36.1612,
    lon: -97.0857,
    elevation: 1000,
    tz: 'America/Chicago',
    distance: 45,
    flightTime: 4800,
  },
];

function getText(renderer: any, testID: string) {
  return renderer.root.findByProps({ testID }).children.join('');
}

describe('FlightSetupScreen', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.clearAllMocks();
    (loadAirports as jest.Mock).mockResolvedValue(undefined);
    (getDestinationsInTimeRange as jest.Mock).mockReturnValue([]);
    (getDestinationsByFlightTime as jest.Mock).mockImplementation((_origin, flightTime) => {
      return flightTime >= 10800 ? mockDestinations : mockDestinations.slice(0, 1);
    });
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('updates the available destinations after the slider changes', async () => {
    let renderer: any;

    act(() => {
      renderer = TestRenderer.create(<FlightSetupScreen />);
    });

    act(() => {
      jest.advanceTimersByTime(300);
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(getText(renderer, 'destinations-state')).toBe('Norman');

    act(() => {
      renderer.root.findByProps({ testID: 'mock-time-slider' }).props.onPress();
    });

    act(() => {
      jest.advanceTimersByTime(300);
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(getText(renderer, 'destinations-state')).toBe('Norman,Stillwater');
    expect(getDestinationsByFlightTime).toHaveBeenCalledWith(
      { lat: 35.3931, lon: -97.6007 },
      10800
    );
  });
});
