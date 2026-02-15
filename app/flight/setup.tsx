"use client";

import FlightSetupScreen from '@/screens/FlightSetupScreen';
import { Stack } from 'expo-router';

export default function FlightSetupRoute() {
  return (
    <>
      <Stack.Screen
        options={{
          headerShown: false,
          presentation: 'modal',
        }}
      />
      <FlightSetupScreen />
    </>
  );
}
