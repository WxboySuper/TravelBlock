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
  emptyContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingHorizontal: 20 },
  emptyText: { fontSize: 16, marginBottom: 8 },
  emptySubtext: { fontSize: 14, color: '#666' },
});

function ResultsCount({ count }: { count: number }) {
  return (
    <ThemedText style={styles.resultCountText}>
      {count} result{count !== 1 ? 's' : ''}
    </ThemedText>
  );
}

function LoadingView() {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" testID="loading-indicator" />
      <ThemedText style={{ marginTop: 12 }}>Loading airports...</ThemedText>
    </View>
  );
}

function ErrorView({ message }: { message: string }) {
  return (
    <View style={styles.emptyContainer}>
      <ThemedText type="subtitle" style={styles.emptyText}>
        ⚠️ Error
      </ThemedText>
      <ThemedText style={styles.emptySubtext}>{message}</ThemedText>
    </View>
  );
}

function EmptyView({ searchQuery }: { searchQuery: string }) {
  return (
    <View style={styles.emptyContainer}>
      <ThemedText type="subtitle" style={styles.emptyText}>
        No airports found
      </ThemedText>
      <ThemedText style={styles.emptySubtext}>{searchQuery.length > 0 ? 'Try a different search term' : 'Start typing to search'}</ThemedText>
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
      scrollEnabled
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
            <ResultsCount count={airports.length} />
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
}: SelectAirportModalProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const backgroundColor = useThemeColor({ light: '#fff', dark: '#1a1a1a' }, 'background');

  const handleClose = useCallback(() => {
    setSearchQuery('');
    onClose();
  }, [onClose]);

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
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          renderAirportItem={renderAirportItem}
          keyExtractor={keyExtractor}
        />
      </SafeAreaView>
    </Modal>
  );
}
