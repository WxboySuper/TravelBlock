/** @type {import('jest').Config} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>'],
  testMatch: ['**/__tests__/utils/**/*.test.ts', '**/__tests__/services/**/*.test.ts', '**/__tests__/**/*.test.tsx'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json', 'node'],
  testPathIgnorePatterns: ['/node_modules/', '/.expo/', '/.worktrees/'],
  collectCoverageFrom: [
    'utils/**/*.ts',
    'services/**/*.ts',
    '!**/*.test.ts',
    '!**/*.spec.ts',
  ],
  coveragePathIgnorePatterns: ['/node_modules/', '/.expo/', '/.worktrees/'],
  coverageThreshold: {
    global: {
      branches: 90,
      functions: 90,
      lines: 90,
      statements: 90,
    },
  },
  // Map @/ alias to root directory for ts-jest
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',
  },
  // Ensure shared test setup is loaded for all Jest environments (ts-jest and jest-expo)
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
};
