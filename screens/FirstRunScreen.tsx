import { HomeAirportWizard } from '@/components/home-airport/HomeAirportWizard';
import { StyleSheet, View } from 'react-native';

export default function FirstRunScreen() {
  return (
    <View style={styles.container}>
      <HomeAirportWizard />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
