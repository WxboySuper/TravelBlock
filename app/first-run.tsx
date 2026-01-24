import FirstRunScreen from '@/screens/FirstRunScreen';
import { Stack } from 'expo-router';

export default function FirstRunRoute() {
  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <FirstRunScreen />
    </>
  );
}
