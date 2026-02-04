/* global jest */
/* eslint-env jest */
/**
 * Jest setup file for Expo projects.
 * 
 * This file is used by both ts-jest (for Node.js utilities) and jest-expo (for React Native).
 * Setup code that applies to both environments can be placed here.
 */

// Mock expo-constants for all tests
jest.mock('expo-constants', () => ({
  __esModule: true,
  default: {
    expoVersion: '54.0.0',
    nativeAppVersion: '1.0.0',
    nativeAppBuildVersion: '1',
  },
}));
 


