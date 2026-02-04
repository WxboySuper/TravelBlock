
/* eslint-disable @typescript-eslint/no-require-imports */
import { describe, it, expect, jest, beforeEach } from '@jest/globals';

// Mock expo-sqlite to avoid loading the real module which is ESM
jest.mock('expo-sqlite', () => ({}), { virtual: true });

// Mock sqlite-helpers to prevent SQLite usage
jest.mock('../expo-sqlite/sqlite-helpers', () => ({
  tryQuickSqliteInitSync: jest.fn(() => null),
  performSqliteInit: jest.fn(() => null), // Returns null so db stays null
  createTableIfNeeded: jest.fn(),
  migrateCacheToDbIfNeeded: jest.fn(),
}));

// We need to manage the mock for async-storage
const mockSetItem = jest.fn<(key: string, value: string) => Promise<void>>();
const mockMultiSet = jest.fn<(keyValuePairs: string[][]) => Promise<void>>();
const mockGetItem = jest.fn<(key: string) => Promise<string | null>>();
const mockRemoveItem = jest.fn<(key: string) => Promise<void>>();

describe('KV Store Migration', () => {
  let kvStore: typeof import('../expo-sqlite/kv-store');

  beforeEach(() => {
    jest.resetModules();
    jest.clearAllMocks();
    mockSetItem.mockReset();
    mockMultiSet.mockReset();
    mockGetItem.mockReset();
    mockRemoveItem.mockReset();

    // Default implementations
    mockSetItem.mockResolvedValue(undefined);
    mockMultiSet.mockResolvedValue(undefined);
    mockGetItem.mockResolvedValue(null);
    mockRemoveItem.mockResolvedValue(undefined);
  });

  it('uses multiSet for migration when available', async () => {
    // 1. Mock async-storage to fail loading initially
    jest.doMock('@react-native-async-storage/async-storage', () => {
      throw new Error('AsyncStorage not found');
    }, { virtual: true });

    // 2. Import kv-store (fresh module)
    kvStore = require('../expo-sqlite/kv-store');

    // 3. Populate cache
    await kvStore.setItem({ key: 'k1', value: 'v1' });
    await kvStore.setItem({ key: 'k2', value: 'v2' });

    // 4. Mock async-storage to be available with multiSet
    jest.doMock('@react-native-async-storage/async-storage', () => ({
      default: {
        setItem: mockSetItem,
        multiSet: mockMultiSet,
        getItem: mockGetItem,
        removeItem: mockRemoveItem,
      },
    }), { virtual: true });

    // 5. Run initStore
    await kvStore.initStore();

    // 6. Verify
    // setItem called 0 times (batch used)
    expect(mockSetItem).toHaveBeenCalledTimes(0);
    // multiSet called 1 times
    expect(mockMultiSet).toHaveBeenCalledTimes(1);

    // Verify arguments passed to multiSet
    // The order of entries depends on Map iteration order which matches insertion order
    expect(mockMultiSet).toHaveBeenCalledWith(expect.arrayContaining([
      ['k1', 'v1'],
      ['k2', 'v2']
    ]));
  });

  it('falls back to setItem if multiSet fails', async () => {
    // 1. Mock async-storage to fail loading initially
    jest.doMock('@react-native-async-storage/async-storage', () => {
      throw new Error('AsyncStorage not found');
    }, { virtual: true });

    // 2. Import kv-store
    kvStore = require('../expo-sqlite/kv-store');

    // 3. Populate cache
    await kvStore.setItem({ key: 'k1', value: 'v1' });
    await kvStore.setItem({ key: 'k2', value: 'v2' });

    // 4. Mock async-storage to be available but multiSet fails
    mockMultiSet.mockRejectedValueOnce(new Error('Batch failed'));
    jest.doMock('@react-native-async-storage/async-storage', () => ({
      default: {
        setItem: mockSetItem,
        multiSet: mockMultiSet,
        getItem: mockGetItem,
        removeItem: mockRemoveItem,
      },
    }), { virtual: true });

    // 5. Run initStore
    await kvStore.initStore();

    // 6. Verify
    expect(mockMultiSet).toHaveBeenCalledTimes(1);
    // setItem called 2 times (fallback)
    expect(mockSetItem).toHaveBeenCalledTimes(2);
    expect(mockSetItem).toHaveBeenCalledWith('k1', 'v1');
    expect(mockSetItem).toHaveBeenCalledWith('k2', 'v2');
  });

  it('uses setItem if multiSet is not available', async () => {
    // 1. Mock async-storage to fail loading initially
    jest.doMock('@react-native-async-storage/async-storage', () => {
      throw new Error('AsyncStorage not found');
    }, { virtual: true });

    // 2. Import kv-store
    kvStore = require('../expo-sqlite/kv-store');

    // 3. Populate cache
    await kvStore.setItem({ key: 'k1', value: 'v1' });

    // 4. Mock async-storage without multiSet
    jest.doMock('@react-native-async-storage/async-storage', () => ({
      default: {
        setItem: mockSetItem,
        // multiSet is undefined
        getItem: mockGetItem,
        removeItem: mockRemoveItem,
      },
    }), { virtual: true });

    // 5. Run initStore
    await kvStore.initStore();

    // 6. Verify
    expect(mockMultiSet).toHaveBeenCalledTimes(0);
    expect(mockSetItem).toHaveBeenCalledTimes(1);
    expect(mockSetItem).toHaveBeenCalledWith('k1', 'v1');
  });
});
