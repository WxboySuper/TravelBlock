import { HomeAirportWizard } from '@/components/home-airport/HomeAirportWizard';
import { StyleSheet, View } from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default function FirstRunScreen() {
  return (
    <View style={styles.container}>
      <HomeAirportWizard />
    </View>
  );
}
