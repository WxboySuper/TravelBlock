import { AirportCard } from '@/components/airport/AirportCard';
import type { Airport } from '@/types/airport';
import { StyleSheet, View } from 'react-native';

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
});

interface HomeAirportDisplayProps {
  airport: Airport;
  onEdit: () => void;
}

export function HomeAirportDisplay({ airport, onEdit }: HomeAirportDisplayProps) {
  return (
    <View style={styles.container} testID="home-airport-display">
      <AirportCard airport={airport} onEdit={onEdit} showEdit />
    </View>
  );
}
