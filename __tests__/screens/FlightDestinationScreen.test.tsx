import React from 'react';
import TestRenderer, { act } from 'react-test-renderer';

import FlightDestinationScreen from '@/screens/FlightDestinationScreen';

const mockPush = jest.fn();
const mockBack = jest.fn();
const mockSetDestination = jest.fn();

const mockOrigin = {
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
];

let mockFlightState = {
  origin: mockOrigin,
  destination: null as (typeof mockDestinations)[number] | null,
  flightDuration: 3600,
};

jest.mock('react-native', () => {
  type HostProps = React.PropsWithChildren<Record<string, unknown>>;
  const createHostComponent = (name: string) => {
    return ({ children, ...props }: HostProps) => React.createElement(name, props, children);
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

jest.mock('@/components/destinations/DestinationsList', () => ({
  DestinationsList: ({
    onSelectDestination,
  }: {
    onSelectDestination: (airport: (typeof mockDestinations)[number]) => void;
  }) => {
    return React.createElement(
      'Pressable',
      {
        testID: 'mock-destination-list',
        onPress: () => onSelectDestination(mockDestinations[0]),
      },
      React.createElement('Text', { testID: 'destinations-state' }, mockDestinations[0].city)
    );
  },
}));

jest.mock('@/components/themed-text', () => ({
  ThemedText: ({ children, testID, style }: { children: React.ReactNode; testID?: string; style?: unknown }) => {
    return React.createElement('Text', { testID, style }, children);
  },
}));

jest.mock('@/components/themed-view', () => ({
  ThemedView: ({ children }: { children: React.ReactNode }) => {
    return React.createElement('View', null, children);
  },
}));

jest.mock('@/components/ui/Button', () => ({
  Button: ({
    title,
    onPress,
    testID,
    disabled,
  }: {
    title: string;
    onPress: () => void;
    testID?: string;
    disabled?: boolean;
  }) => {
    return React.createElement(
      'Pressable',
      {
        testID,
        onPress,
        disabled,
      },
      React.createElement('Text', null, title)
    );
  },
}));

jest.mock('@/components/ui/icon-symbol', () => ({
  IconSymbol: () => React.createElement('Text', { testID: 'icon-symbol' }, 'icon'),
}));

jest.mock('expo-haptics', () => ({
  impactAsync: jest.fn().mockImplementation(() => Promise.resolve()),
  ImpactFeedbackStyle: {
    Light: 'light',
    Heavy: 'heavy',
  },
}));

jest.mock('react-native-safe-area-context', () => ({
  SafeAreaView: ({ children, ...props }: { children: React.ReactNode }) => {
    return React.createElement('SafeAreaView', props, children);
  },
  useSafeAreaInsets: () => ({
    top: 0,
    right: 0,
    bottom: 20,
    left: 0,
  }),
}));

jest.mock('@/hooks/use-color-scheme', () => ({
  useColorScheme: () => 'light',
}));

jest.mock('@/hooks/useDestinations', () => ({
  useDestinations: () => ({
    destinations: mockDestinations,
    isLoading: false,
    error: null,
    refresh: jest.fn(),
  }),
}));

jest.mock('@/context/FlightContext', () => ({
  useFlight: () => ({
    ...mockFlightState,
    setDestination: mockSetDestination,
  }),
}));

jest.mock('expo-router', () => ({
  useRouter: () => ({
    back: mockBack,
    push: mockPush,
  }),
}));

type TestRendererInstance = ReturnType<typeof TestRenderer.create>;

function hasText(renderer: TestRendererInstance, expectedText: string) {
  return renderer.root.findAllByType('Text').some((node: { children: Array<string | number> }) => {
    return node.children.join('') === expectedText;
  });
}

describe('FlightDestinationScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockFlightState = {
      origin: mockOrigin,
      destination: null,
      flightDuration: 3600,
    };
    mockSetDestination.mockResolvedValue(undefined);
  });

  it('shows the destination summary and keeps review disabled until a destination is selected', () => {
    let renderer: TestRendererInstance | null = null;

    act(() => {
      renderer = TestRenderer.create(<FlightDestinationScreen />);
    });

    if (!renderer) {
      throw new Error('Screen renderer not initialized');
    }

    expect(hasText(renderer, 'KOKC')).toBe(true);
    expect(hasText(renderer, '50m - 1h 0m')).toBe(true);
    expect(renderer.root.findByProps({ testID: 'review-flight-button' }).props.disabled).toBe(true);
  });

  it('selects a destination and enables review when one is already selected', async () => {
    let renderer: TestRendererInstance | null = null;

    act(() => {
      renderer = TestRenderer.create(<FlightDestinationScreen />);
    });

    if (!renderer) {
      throw new Error('Screen renderer not initialized');
    }

    act(() => {
      renderer.root.findByProps({ testID: 'mock-destination-list' }).props.onPress();
    });

    expect(mockSetDestination).toHaveBeenCalledWith(mockDestinations[0]);

    mockFlightState = {
      origin: mockOrigin,
      destination: mockDestinations[0],
      flightDuration: 3600,
    };

    act(() => {
      renderer.update(<FlightDestinationScreen />);
    });

    expect(renderer.root.findByProps({ testID: 'review-flight-button' }).props.disabled).toBe(false);

    await act(async () => {
      renderer.root.findByProps({ testID: 'review-flight-button' }).props.onPress();
      await Promise.resolve();
    });

    expect(mockPush).toHaveBeenCalledWith('/flight/review');
  });
});
