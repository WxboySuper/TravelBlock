import { ThemedText } from '@/components/themed-text';
import { useThemeColor } from '@/hooks/use-theme-color';
import { loadAirports, searchAirports } from '@/services/airportService';
import type { Airport, AirportWithDistance } from '@/types/airport';
import type { Coordinates } from '@/utils/distance';
import { calculateDistance, calculateDistanceKm } from '@/utils/distance';
import { useCallback, useEffect, useRef, useState, type ReactElement } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Modal,
  StyleSheet,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { AirportListItem } from './AirportListItem';
import { AirportSearchBar } from './AirportSearchBar';

type SelectAirportModalProps = {
  visible: boolean;
  onClose: () => void;
  onSelectAirport: (a: AirportWithDistance) => void;
  origin?: Coordinates | null;
  airports?: AirportWithDistance[];
  distanceInKm?: boolean;
  title?: string;
  isLoading?: boolean;
  errorMessage?: string | null;
  searchQuery?: string;
  onSearchChange?: (q: string) => void;
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  headerContainer: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerTitle: { fontSize: 18 },
  closeButton: { padding: 8 },
  closeButtonText: { fontSize: 18 },
  listContainer: { flex: 1 },
  resultCountText: { paddingHorizontal: 16, paddingVertical: 6 },
  loadingContainer: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  loadingText: { marginTop: 12 },
  emptyContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingHorizontal: 20 },
  emptyText: { fontSize: 16, marginBottom: 8 },
  emptySubtext: { fontSize: 14 },
});

function attachDistance(
  airports: Airport[],
  origin?: Coordinates | null,
  distanceInKm?: boolean
): AirportWithDistance[] {
  if (!origin) {
    return airports.map((a) => ({ ...a, distance: undefined }));
  }

  const compute = distanceInKm ? calculateDistanceKm : calculateDistance;

  return airports
    .map((airport) => {
      const distance = compute(origin, { lat: airport.lat, lon: airport.lon });
      return { ...airport, distance };
    })
    .sort((a, b) => (a.distance ?? Infinity) - (b.distance ?? Infinity));
}

/**
 * Displays a concise results summary like "3 results" with correct singular/plural form.
 *
 * @param count - The number of results to display
 * @param testID - Optional test identifier applied to the rendered text
 * @returns The themed text element showing "`<count> result`" or "`<count> results`" as appropriate
 */
function ResultsCount({ count, testID }: { count: number; testID?: string }) {
  return (
    <ThemedText testID={testID} style={styles.resultCountText}>
      {count} result{count !== 1 ? 's' : ''}
    </ThemedText>
  );
}

/**
 * Displays a centered loading indicator and message used while airport data is being fetched.
 *
 * @returns A React element containing an activity indicator and the "Loading airports..." message.
 */
function LoadingView() {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" testID="loading-indicator" />
      <ThemedText style={styles.loadingText}>Loading airports...</ThemedText>
    </View>
  );
}

/**
 * Renders a standardized error view with a warning title and the provided message.
 *
 * @param message - The error message text to display in the view
 * @returns A React element displaying the warning title and `message`
 */
function ErrorView({ message }: { message: string }) {
  const subTextColor = useThemeColor({ light: '#666', dark: '#9a9a9a' }, 'text');
  return (
    <MessageView
      viewTestID="select-airport-error-view"
      title="⚠️ Error"
      titleTestID="select-airport-error-title"
      message={message}
      messageTestID="select-airport-error-message"
      messageColor={subTextColor}
    />
  );
}

/**
 * Displays an empty-state message for the airport list based on the current search query.
 *
 * @param searchQuery - Current search text; when non-empty shows a suggestion to try a different term, otherwise prompts the user to start typing
 * @returns A React element rendering a `MessageView` with the title "No airports found" and a context-specific subtext
 */
function EmptyView({ searchQuery }: { searchQuery: string }) {
  const subTextColor = useThemeColor({ light: '#666', dark: '#9a9a9a' }, 'text');
  return (
    <MessageView
      viewTestID="select-airport-empty-view"
      title="No airports found"
      titleTestID="select-airport-empty-title"
      message={searchQuery.length > 0 ? 'Try a different search term' : 'Start typing to search'}
      messageTestID="select-airport-empty-subtext"
      messageColor={subTextColor}
    />
  );
}

/**
 * Renders a titled message view with a subtitle message and optional message color.
 *
 * @param viewTestID - Test identifier applied to the container view
 * @param title - Title text displayed above the message
 * @param titleTestID - Test identifier applied to the title text
 * @param message - Subtext message displayed below the title
 * @param messageTestID - Test identifier applied to the message text
 * @param messageColor - Optional color to apply to the message text
 * @returns A React element containing the titled message layout with the optional message color applied
 */
function MessageView({
  viewTestID,
  title,
  titleTestID,
  message,
  messageTestID,
  messageColor,
}: {
  viewTestID: string;
  title: string;
  titleTestID: string;
  message: string;
  messageTestID: string;
  messageColor?: string;
}) {
  return (
    <View style={styles.emptyContainer} testID={viewTestID}>
      <ThemedText type="subtitle" style={styles.emptyText} testID={titleTestID}>
        {title}
      </ThemedText>
      <ThemedText style={[styles.emptySubtext, messageColor ? { color: messageColor } : undefined]} testID={messageTestID}>
        {message}
      </ThemedText>
    </View>
  );
}

/**
 * Renders a scrollable list of airports with optimized performance settings.
 *
 * @param airports - Array of airport objects that include distance information.
 * @param renderItem - Function that renders a list item; receives `{ item: AirportWithDistance }`.
 * @param keyExtractor - Function that returns a unique string key for each airport item.
 * @returns A FlatList React element displaying the provided airports.
 */
function AirportListComponent({
  airports,
  renderItem,
  keyExtractor,
}: {
  airports: AirportWithDistance[];
  renderItem: (info: { item: AirportWithDistance }) => ReactElement | null;
  keyExtractor: (item: AirportWithDistance) => string;
}) {
  return (
    <FlatList
      data={airports}
      renderItem={renderItem}
      keyExtractor={keyExtractor}
      style={styles.listContainer}
      removeClippedSubviews
      maxToRenderPerBatch={20}
      updateCellsBatchingPeriod={50}
      testID="airport-list"
    />
  );
}

type ModalHeaderProps = {
  title: string;
  onClose: () => void;
  closeTestID?: string;
};

/**
 * Renders a modal header with a title and a close button.
 *
 * @param title - Header title text
 * @param onClose - Callback invoked when the close button is pressed
 * @param closeTestID - Test identifier for the close button (default 'modal-close-button')
 * @returns The header React element containing the title and close button
 */
export function ModalHeader({ title, onClose, closeTestID = 'modal-close-button' }: ModalHeaderProps): ReactElement {
  return (
    <View style={styles.headerContainer}>
      <ThemedText type="title" style={styles.headerTitle}>
        {title}
      </ThemedText>
      <TouchableOpacity
        onPress={onClose}
        style={styles.closeButton}
        testID={closeTestID}
        accessibilityLabel="Close modal"
        accessibilityRole="button"
      >
        <ThemedText style={styles.closeButtonText}>✕</ThemedText>
      </TouchableOpacity>
    </View>
  );
}

type ModalBodyProps = {
  airports: AirportWithDistance[];
  isLoading: boolean;
  errorMessage?: string | null;
  searchQuery: string;
  setSearchQuery: (s: string) => void;
  renderAirportItem: (info: { item: AirportWithDistance }) => ReactElement | null;
  keyExtractor: (item: AirportWithDistance) => string;
};

/**
 * Render the modal body consisting of a search bar and a state-driven content area for airports.
 *
 * @param airports - Array of airports (with distance) to display in the list.
 * @param isLoading - When `true`, shows a loading view instead of content.
 * @param errorMessage - When provided, shows an error view with this message.
 * @param searchQuery - Current search string bound to the search bar.
 * @param setSearchQuery - Callback invoked when the search string changes.
 * @param renderAirportItem - Function that renders a single airport item for the list.
 * @param keyExtractor - Function that returns a stable key for each airport item.
 * @returns The composed modal body: an AirportSearchBar followed by either a loading indicator, an error message, an empty message, or the results count and airport list.
 */
export function ModalBody({
  airports,
  isLoading,
  errorMessage,
  searchQuery,
  setSearchQuery,
  renderAirportItem,
  keyExtractor,
}: ModalBodyProps): ReactElement | null {
  const isEmpty = airports.length === 0;

  const bodyState: 'loading' | 'error' | 'empty' | 'list' = isLoading
    ? 'loading'
    : errorMessage
    ? 'error'
    : isEmpty
    ? 'empty'
    : 'list';

  const renderBody = () => {
    switch (bodyState) {
      case 'loading':
        return <LoadingView />;
      case 'error':
        return errorMessage ? <ErrorView message={errorMessage} /> : null;
      case 'empty':
        return <EmptyView searchQuery={searchQuery} />;
      case 'list':
        return (
          <>
            <ResultsCount count={airports.length} testID="select-airport-results-count" />
            <AirportListComponent airports={airports} renderItem={renderAirportItem} keyExtractor={keyExtractor} />
          </>
        );
      default:
        return null;
    }
  };

  return (
    <>
      <AirportSearchBar value={searchQuery} onChangeText={setSearchQuery} placeholder="Find an airport..." />
      {renderBody()}
    </>
  );
}

/**
 * Displays a full-screen modal that lets the user search for and select an airport.
 *
 * @param visible - Whether the modal is visible
 * @param onClose - Callback invoked when the modal is closed
 * @param onSelectAirport - Callback invoked with the selected airport
 * @param origin - Optional coordinates used to calculate and show distances for airports
 * @param airports - List of airports (with distance) to display in the list
 * @param distanceInKm - If true, distances are shown in kilometers; otherwise in miles
 * @param title - Modal header title
 * @param isLoading - When true, shows a loading state instead of the list
 * @param errorMessage - Optional error message to display instead of the list
 * @param searchQueryProp - Optional externally controlled search query; when omitted, component manages local search state
 * @param onSearchChange - Optional callback invoked when the search query changes; if provided, it receives the new query
 * @returns The rendered Select Airport modal element
 */
export function SelectAirportModal({
  visible,
  onClose,
  onSelectAirport,
  origin,
  airports: airportsProp,
  distanceInKm = false,
  title = 'Select Airport',
  isLoading = false,
  errorMessage,
  searchQuery: searchQueryProp,
  onSearchChange,
}: SelectAirportModalProps) {
  const backgroundColor = useThemeColor({ light: '#fff', dark: '#1a1a1a' }, 'background');

  // Hook: load airports lazily when modal is visible
  const { hasLoaded, loaderLoading, loaderError } = useAirportLoader(visible);

  // Hook: manage search (debounced) when not externally controlled
  const {
    airports: searchedAirports,
    loading: searchLoading,
    error: searchError,
    query,
    setQuery,
  } = useAirportSearch({
    visible,
    hasLoaded,
    onSearchChange,
    externalQuery: searchQueryProp,
    origin,
    distanceInKm,
  });

  const distanceUnit = distanceInKm ? 'km' : 'mi';

  const handleSelectAirport = useCallback(
    (airport: AirportWithDistance) => {
      onSelectAirport(airport);
      onClose();
    },
    [onSelectAirport, onClose]
  );

  const keyExtractor = useCallback((item: AirportWithDistance) => item.icao, []);

  const renderAirportItem = useCallback(
    ({ item }: { item: AirportWithDistance }) => (
      <AirportListItem
        airport={item}
        onPress={handleSelectAirport}
        showDistance={Boolean(origin)}
        distanceUnit={distanceUnit}
      />
    ),
    [handleSelectAirport, origin, distanceUnit]
  );

  const resolvedAirports = airportsProp ? attachDistance(airportsProp, origin, distanceInKm) : searchedAirports;
  const resolvedLoading = isLoading || loaderLoading || searchLoading;
  const resolvedError = errorMessage ?? loaderError ?? searchError;

  return (
    <Modal
      visible={visible}
      onRequestClose={onClose}
      animationType="slide"
      presentationStyle="fullScreen"
      testID="select-airport-modal"
      accessibilityViewIsModal
    >
      <SafeAreaView style={[styles.container, { backgroundColor }]}>
        <ModalHeader title={title} onClose={onClose} />
        <ModalBody
          airports={resolvedAirports}
          isLoading={resolvedLoading}
          errorMessage={resolvedError}
          searchQuery={searchQueryProp ?? query}
          setSearchQuery={setQuery}
          renderAirportItem={renderAirportItem}
          keyExtractor={keyExtractor}
        />
      </SafeAreaView>
    </Modal>
  );
}

// Hook: lazy-load airports when modal opens
function useAirportLoader(visible: boolean) {
  const [hasLoaded, setHasLoaded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const mounted = useRef(true);

  useEffect(() => {
    mounted.current = true;
    if (!visible) return () => {
      mounted.current = false;
    };

    setLoading(true);
    setError(null);

    let cancelled = false;
    loadAirports()
      .then(() => {
        if (!cancelled && mounted.current) setHasLoaded(true);
      })
      .catch(() => {
        if (!cancelled && mounted.current) setError('Unable to load airports. Please try again.');
      })
      .finally(() => {
        if (!cancelled && mounted.current) setLoading(false);
      });

    return () => {
      cancelled = true;
      mounted.current = false;
    };
  }, [visible]);

  return { hasLoaded, loaderLoading: loading, loaderError: error };
}

type UseAirportSearchArgs = {
  visible: boolean;
  hasLoaded: boolean;
  onSearchChange?: (q: string) => void;
  externalQuery?: string | undefined;
  origin?: Coordinates | null;
  distanceInKm?: boolean;
};

// Hook: perform debounced search using service when modal is visible and not externally controlled
function useAirportSearch({ visible, hasLoaded, onSearchChange, externalQuery, origin, distanceInKm }: UseAirportSearchArgs) {
  const [airports, setAirports] = useState<AirportWithDistance[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const mounted = useRef(true);
  const [query, setQueryState] = useState<string>('');

  useEffect(() => {
    return () => {
      mounted.current = false;
    };
  }, []);

  const setQuery = useCallback(
    (q: string) => {
      if (onSearchChange) {
        try {
          onSearchChange(q);
        } catch (e) {
          console.warn('onSearchChange threw', e);
        }
        return;
      }
      setQueryState(q);
    },
    [onSearchChange]
  );

  useEffect(() => {
    if (!visible) return;
    if (onSearchChange) return;
    if (!hasLoaded) return;

    const q = (externalQuery ?? query ?? '').trim();
    if (!q) {
      setAirports([]);
      setError(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
      const timer = setTimeout(() => {
        runSearch(q, {
          origin,
          distanceInKm,
          mounted,
          setAirports,
          setError,
          setLoading,
        });
      }, 300);

    return () => {
      clearTimeout(timer);
    };
  }, [visible, hasLoaded, externalQuery, onSearchChange, query, origin, distanceInKm]);

  return { airports, loading, error, query, setQuery };
}

function runSearch(
  q: string,
  opts: {
    origin?: Coordinates | null | undefined;
    distanceInKm?: boolean | undefined;
    mounted: React.MutableRefObject<boolean>;
    setAirports: (a: AirportWithDistance[]) => void;
    setError: (s: string | null) => void;
    setLoading: (b: boolean) => void;
  }
) {
  const { origin, distanceInKm, mounted, setAirports, setError, setLoading } = opts;
  try {
    const results = searchAirports(q);
    const withDistance = attachDistance(results, origin, distanceInKm);
    if (mounted.current) setAirports(withDistance);
  } catch (e) {
    console.error('searchAirports failed', e);
    if (mounted.current) setError('Unable to search airports. Please try again.');
  } finally {
    if (mounted.current) setLoading(false);
  }
}