import { useState, useCallback } from 'react';
import { storageService } from '@/services/storageService';
import { Airport, AirportWithDistance } from '@/types/airport';
import { useRouter } from 'expo-router';

export enum FirstRunStep {
  WELCOME = 0,
  LOCATION_PERMISSION = 1,
  HOME_AIRPORT = 2,
  COMPLETED = 3,
}

export function useFirstRunFlow() {
  const [currentStep, setCurrentStep] = useState<FirstRunStep>(FirstRunStep.WELCOME);
  const [selectedAirport, setSelectedAirport] = useState<Airport | null>(null);
  const [isCompleting, setIsCompleting] = useState(false);
  const router = useRouter();

  const goToNextStep = useCallback(() => {
    setCurrentStep((prev) => Math.min(prev + 1, FirstRunStep.COMPLETED));
  }, []);

  const goToPreviousStep = useCallback(() => {
    setCurrentStep((prev) => Math.max(prev - 1, FirstRunStep.WELCOME));
  }, []);

  const selectAirport = useCallback((airport: Airport | AirportWithDistance) => {
    // Strip distance if present to match Airport type
    const { lat, lon, name, city, country, iata, icao, elevation } = airport;
    const cleanAirport: Airport = { lat, lon, name, city, country, iata, icao, elevation };
    setSelectedAirport(cleanAirport);
  }, []);

  const completeOnboarding = useCallback(async () => {
    if (isCompleting) return;
    setIsCompleting(true);
    try {
      if (selectedAirport) {
        await storageService.saveHomeAirport(selectedAirport);
      }
      await storageService.setOnboardingCompleted(true);

      // Navigate to the main app
      router.replace('/(tabs)');
    } catch (error) {
      console.error('Failed to complete onboarding:', error);
      setIsCompleting(false);
    }
  }, [isCompleting, selectedAirport, router]);

  return {
    currentStep,
    selectedAirport,
    isCompleting,
    goToNextStep,
    goToPreviousStep,
    selectAirport,
    completeOnboarding,
  };
}
