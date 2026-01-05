/** @type {import('jest').Config} */
/**
 * Jest configuration for React Native component testing with Expo.
 * 
 * This configuration uses the jest-expo preset and is designed for testing
 * React Native components and Expo modules.
 * 
 * Usage: npm test -- --config jest.expo.config.js
 */
module.exports = {
  displayName: 'expo',
  preset: 'jest-expo',
  testEnvironment: 'node',
  roots: ['<rootDir>'],
  testMatch: [
    '**/components/**/__tests__/**/*.test.ts',
    '**/components/**/__tests__/**/*.test.tsx',
    '**/hooks/**/__tests__/**/*.test.ts',
    '**/hooks/**/__tests__/**/*.test.tsx',
  ],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  testPathIgnorePatterns: ['/node_modules/', '/.expo/'],
  collectCoverageFrom: [
    'components/**/*.ts',
    'components/**/*.tsx',
    'hooks/**/*.ts',
    'hooks/**/*.tsx',
    '!**/*.test.ts',
    '!**/*.test.tsx',
    '!**/*.spec.ts',
    '!**/*.spec.tsx',
  ],
  coveragePathIgnorePatterns: ['/node_modules/', '/.expo/'],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
  },
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  transformIgnorePatterns: [
    'node_modules/(?!(expo|@react-native|@react-navigation|@react-native-community|react-native-gesture-handler|react-native-reanimated|react-native-screens)/)',
  ],
};
