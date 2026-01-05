import { ThemedText } from '@/components/themed-text';
import { useThemeColor } from '@/hooks/use-theme-color';
import type { AirportWithDistance } from '@/types/airport';
import type { Coordinates } from '@/utils/distance';
import { useCallback, useState, type ReactElement } from 'react';
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

function ResultsCount({ count, testID }: { count: number; testID?: string }) {
  return (
    <ThemedText testID={testID} style={styles.resultCountText}>
      {count} result{count !== 1 ? 's' : ''}
    </ThemedText>
  );
}

function LoadingView() {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" testID="loading-indicator" />
      <ThemedText style={styles.loadingText}>Loading airports...</ThemedText>
    </View>
  );
}

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

export function SelectAirportModal({
  visible,
  onClose,
  onSelectAirport,
  origin,
  airports = [],
  distanceInKm = false,
  title = 'Select Airport',
  isLoading = false,
  errorMessage,
  searchQuery: searchQueryProp,
  onSearchChange,
}: SelectAirportModalProps) {
  const [localSearchQuery, setLocalSearchQuery] = useState('');
  const setSearch = useCallback(
    (q: string) => {
      if (onSearchChange) {
        try {
          onSearchChange(q);
        } catch (error) {
          // Log error to avoid breaking modal while still providing visibility
          console.warn('SelectAirportModal: onSearchChange callback threw an error', error);
        }
      } else {
        setLocalSearchQuery(q);
      }
    },
    [onSearchChange]
  );

  const effectiveSearchQuery = typeof searchQueryProp === 'string' ? searchQueryProp : localSearchQuery;
  const backgroundColor = useThemeColor({ light: '#fff', dark: '#1a1a1a' }, 'background');
  const handleClose = useCallback(() => {
    setSearch('');
    onClose();
  }, [onClose, setSearch]);

  const handleSelectAirport = useCallback(
    (airport: AirportWithDistance) => {
      onSelectAirport(airport);
      handleClose();
    },
    [onSelectAirport, handleClose]
  );

  const distanceUnit = distanceInKm ? 'km' : 'mi';

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

  return (
    <Modal
      visible={visible}
      onRequestClose={handleClose}
      animationType="slide"
      presentationStyle="fullScreen"
      testID="select-airport-modal"
      accessibilityViewIsModal
    >
      <SafeAreaView style={[styles.container, { backgroundColor }]}>
        <ModalHeader title={title} onClose={handleClose} />
        <ModalBody
          airports={airports}
          isLoading={isLoading}
          errorMessage={errorMessage}
          searchQuery={effectiveSearchQuery}
          setSearchQuery={setSearch}
          renderAirportItem={renderAirportItem}
          keyExtractor={keyExtractor}
        />
      </SafeAreaView>
    </Modal>
  );
}
