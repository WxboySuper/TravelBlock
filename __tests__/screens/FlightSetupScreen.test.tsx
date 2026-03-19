import React from 'react';
import TestRenderer, { act } from 'react-test-renderer';

import FlightSetupScreen from '@/screens/FlightSetupScreen';

const mockPush = jest.fn();
const mockBack = jest.fn();
const mockSetOrigin = jest.fn();
const mockSetDestination = jest.fn();
const mockSetFlightDuration = jest.fn();

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

jest.mock('@/components/time-slider/TimeSlider', () => ({
  TimeSlider: ({ value, onValueChange }: { value: number; onValueChange: (value: number) => void }) => {
    return React.createElement(
      'Pressable',
      {
        testID: 'mock-time-slider',
        onPress: () => onValueChange(value + 600),
      },
      React.createElement('Text', null, 'Mock Slider')
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
}));

jest.mock('@/hooks/use-color-scheme', () => ({
  useColorScheme: () => 'light',
}));

const mockHomeAirport = {
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

jest.mock('@/hooks/useHomeAirport', () => ({
  useHomeAirport: () => ({
    homeAirport: mockHomeAirport,
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
    setOrigin: mockSetOrigin,
    setDestination: mockSetDestination,
    setFlightDuration: mockSetFlightDuration,
  }),
}));

jest.mock('expo-router', () => ({
  useRouter: () => ({
    back: mockBack,
    push: mockPush,
  }),
}));

type TestRendererInstance = ReturnType<typeof TestRenderer.create>;

describe('FlightSetupScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSetOrigin.mockImplementation(() => Promise.resolve());
    mockSetDestination.mockImplementation(() => Promise.resolve());
    mockSetFlightDuration.mockImplementation(() => Promise.resolve());
  });

  it('continues to the destination step after selecting a flight window', async () => {
    let renderer: TestRendererInstance | null = null;

    act(() => {
      renderer = TestRenderer.create(<FlightSetupScreen />);
    });

    if (!renderer) {
      throw new Error('Screen renderer not initialized');
    }

    expect(mockSetOrigin).toHaveBeenCalledWith(mockHomeAirport);

    act(() => {
      renderer.root.findByProps({ testID: 'mock-time-slider' }).props.onPress();
    });

    expect(mockSetFlightDuration).toHaveBeenCalledWith(4200);
    expect(mockSetDestination).toHaveBeenCalledWith(null);

    await act(async () => {
      renderer.root.findByProps({ testID: 'continue-to-destinations' }).props.onPress();
      await Promise.resolve();
    });

    expect(mockPush).toHaveBeenCalledWith('/flight/destination');
  });
});
