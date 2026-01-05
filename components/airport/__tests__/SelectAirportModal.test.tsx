// React import is not required with the new JSX runtime
import { act, fireEvent, render, waitFor } from '@testing-library/react-native';
import { loadAirports, searchAirports } from '../../../services/airportService';
import type { Airport } from '../../../types/airport';
import { SelectAirportModal } from '../SelectAirportModal';

jest.mock('../../../services/airportService', () => {
  const mockLoadAirports = jest.fn();
  const mockSearchAirports = jest.fn();
  return {
    loadAirports: mockLoadAirports,
    searchAirports: mockSearchAirports,
  };
});

jest.mock('expo-haptics', () => ({
  impactAsync: jest.fn(() => Promise.resolve()),
  ImpactFeedbackStyle: { Light: 'light' },
}));

function noop(): void {}

describe('SelectAirportModal integration', () => {
  const baseAirports: Airport[] = [
    {
      icao: 'KAAA',
      iata: 'AAA',
      name: 'Alpha Airport',
      city: 'Alpha City',
      state: '',
      country: 'US',
      elevation: 0,
      lat: 0,
      lon: 0,
      tz: 'UTC',
    },
    {
      icao: 'KBBB',
      iata: 'BBB',
      name: 'Bravo Airport',
      city: 'Bravo City',
      state: '',
      country: 'CA',
      elevation: 0,
      lat: 10,
      lon: 10,
      tz: 'UTC',
    },
    {
      icao: 'KCCC',
      iata: 'CCC',
      name: 'Charlie Airport',
      city: 'Charlie City',
      state: '',
      country: 'GB',
      elevation: 0,
      lat: 20,
      lon: 20,
      tz: 'UTC',
    },
  ];

  const airportMap = baseAirports.reduce<Record<string, Airport>>((acc, airport) => {
    acc[airport.icao] = airport;
    return acc;
  }, {});

  beforeEach(() => {
    jest.useFakeTimers();
    (loadAirports as jest.Mock).mockResolvedValue(airportMap);
    (searchAirports as jest.Mock).mockReturnValue(baseAirports);
  });

  afterEach(() => {
    jest.clearAllMocks();
    jest.useRealTimers();
  });

  it('performs debounced internal search and renders results', async () => {
    const onSelectAirport = jest.fn();
    const { getByTestId } = render(
      <SelectAirportModal visible onClose={noop} onSelectAirport={onSelectAirport} />
    );

    const input = getByTestId('airport-search-input');
    fireEvent.changeText(input, 'Alpha');

    act(() => {
      jest.advanceTimersByTime(350);
    });

    await waitFor(() => {
      expect(searchAirports).toHaveBeenCalledWith('Alpha');
      expect(getByTestId('airport-item-KAAA')).toBeTruthy();
    });
  });

  it('sorts results by distance when origin is provided', () => {
    (searchAirports as jest.Mock).mockReturnValue([
      { ...baseAirports[1], lat: 1, lon: 1 },
      { ...baseAirports[2], lat: 5, lon: 5 },
    ]);

    const { getAllByTestId, getByTestId } = render(
      <SelectAirportModal
        visible
        origin={{ lat: 0, lon: 0 }}
        onClose={noop}
        onSelectAirport={noop}
      />
    );

    fireEvent.changeText(getByTestId('airport-search-input'), 'B');

    act(() => {
      jest.advanceTimersByTime(350);
    });

    const items = getAllByTestId(/airport-item-/);
    expect(items[0].props.testID).toBe('airport-item-KBBB');
    expect(items[1].props.testID).toBe('airport-item-KCCC');
  });

  it('shows distances in kilometers when distanceInKm is true', async () => {
    (searchAirports as jest.Mock).mockReturnValue([
      { ...baseAirports[0], lat: 0, lon: 1 },
    ]);

    const { getByTestId, getByText } = render(
      <SelectAirportModal
        visible
        distanceInKm
        origin={{ lat: 0, lon: 0 }}
        onClose={noop}
        onSelectAirport={noop}
      />
    );

    fireEvent.changeText(getByTestId('airport-search-input'), 'Alpha');

    act(() => {
      jest.advanceTimersByTime(350);
    });

    await waitFor(() => {
      expect(getByText(/km/)).toBeTruthy();
    });
  });

  it('surfaces load errors gracefully', async () => {
    (loadAirports as jest.Mock).mockRejectedValueOnce(new Error('load failed'));

    const { getByTestId } = render(
      <SelectAirportModal visible onClose={noop} onSelectAirport={noop} />
    );

    await waitFor(() => {
      expect(getByTestId('select-airport-error-view')).toBeTruthy();
    });
  });

  it('clears search results when modal is closed', async () => {
    const onClose = jest.fn();
    const { getByTestId, queryByTestId } = render(
      <SelectAirportModal visible onClose={onClose} onSelectAirport={noop} />
    );

    fireEvent.changeText(getByTestId('airport-search-input'), 'Alpha');

    act(() => {
      jest.advanceTimersByTime(350);
    });

    expect(getByTestId('airport-item-KAAA')).toBeTruthy();

    fireEvent.press(getByTestId('modal-close-button'));

    await waitFor(() => {
      expect(queryByTestId('airport-item-KAAA')).toBeNull();
      expect(onClose).toHaveBeenCalled();
    });
  });

  it('defers search to external handler when provided', () => {
    const handleSearchChange = jest.fn();
    const airportsProp = [baseAirports[0]];

    const { getByTestId } = render(
      <SelectAirportModal
        visible
        airports={airportsProp}
        onClose={noop}
        onSelectAirport={noop}
        onSearchChange={handleSearchChange}
        searchQuery="Alpha"
      />
    );

    fireEvent.changeText(getByTestId('airport-search-input'), 'Beta');

    act(() => {
      jest.advanceTimersByTime(400);
    });

    expect(handleSearchChange).toHaveBeenCalledWith('Beta');
    expect(searchAirports).not.toHaveBeenCalled();
  });
});
