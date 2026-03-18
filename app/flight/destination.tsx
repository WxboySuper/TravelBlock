"use client";

import FlightDestinationScreen from '@/screens/FlightDestinationScreen';
import { Stack } from 'expo-router';

export default function FlightDestinationRoute() {
  return (
    <>
      <Stack.Screen
        options={{
          headerShown: false,
          presentation: 'modal',
        }}
      />
      <FlightDestinationScreen />
    </>
  );
}
