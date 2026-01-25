import { FirstRunStep, useFirstRunFlow } from '@/hooks/useFirstRunFlow';
import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { CompletionStep } from './steps/CompletionStep';
import { HomeAirportSetupStep } from './steps/HomeAirportSetupStep';
import { LocationPermissionStep } from './steps/LocationPermissionStep';
import { WelcomeStep } from './steps/WelcomeStep';
import { useThemeColor } from '@/hooks/use-theme-color';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export function HomeAirportWizard() {
  const {
    currentStep,
    selectedAirport,
    isCompleting,
    goToNextStep,
    goToPreviousStep,
    selectAirport,
    completeOnboarding,
  } = useFirstRunFlow();

  const backgroundColor = useThemeColor({ light: '#fff', dark: '#1E293B' }, 'background');

  const renderStep = () => {
    switch (currentStep) {
      case FirstRunStep.WELCOME:
        return <WelcomeStep onNext={goToNextStep} />;
      case FirstRunStep.LOCATION_PERMISSION:
        return <LocationPermissionStep onNext={goToNextStep} />;
      case FirstRunStep.HOME_AIRPORT:
        return (
          <HomeAirportSetupStep
            selectedAirport={selectedAirport}
            onSelectAirport={selectAirport}
            onNext={goToNextStep}
            onBack={goToPreviousStep}
          />
        );
      case FirstRunStep.COMPLETED:
        return (
          <CompletionStep
            onComplete={completeOnboarding}
            isCompleting={isCompleting}
          />
        );
      default:
        return null;
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor }]}>
      {renderStep()}
    </SafeAreaView>
  );
}
