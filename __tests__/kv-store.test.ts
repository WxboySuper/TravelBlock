jest.mock('expo-sqlite/kv-store', () => ({
  AsyncStorage: {
    getItem: jest.fn(),
    setItem: jest.fn(),
    removeItem: jest.fn(),
    mergeItem: jest.fn(),
    getItemSync: jest.fn(),
    setItemSync: jest.fn(),
  },
}));

type MockExpoKvStoreModule = {
  AsyncStorage: {
    getItem: jest.Mock;
    setItem: jest.Mock;
    removeItem: jest.Mock;
    mergeItem: jest.Mock;
    getItemSync: jest.Mock;
    setItemSync: jest.Mock;
  };
};

describe('kv-store wrapper', () => {
  beforeEach(() => {
    jest.resetModules();
    jest.clearAllMocks();
  });

  it('delegates async set/get/remove to expo kv store', async () => {
    const expoKvStore = (await import('expo-sqlite/kv-store')) as unknown as MockExpoKvStoreModule;
    expoKvStore.AsyncStorage.getItem.mockResolvedValue('value-1');

    const kvStore = await import('../expo-sqlite/kv-store');

    await kvStore.setItem({ key: 'key-1', value: 'value-1' });
    const value = await kvStore.getItem({ key: 'key-1' });
    await kvStore.removeItem({ key: 'key-1' });

    expect(expoKvStore.AsyncStorage.setItem).toHaveBeenCalledWith('key-1', 'value-1');
    expect(expoKvStore.AsyncStorage.getItem).toHaveBeenCalledWith('key-1');
    expect(expoKvStore.AsyncStorage.removeItem).toHaveBeenCalledWith('key-1');
    expect(value).toBe('value-1');
  });

  it('falls back to memory when expo kv store returns null', async () => {
    const expoKvStore = (await import('expo-sqlite/kv-store')) as unknown as MockExpoKvStoreModule;
    expoKvStore.AsyncStorage.getItem.mockResolvedValue(null);

    const kvStore = await import('../expo-sqlite/kv-store');

    await kvStore.setItem({ key: 'key-2', value: 'value-2' });
    const value = await kvStore.getItem({ key: 'key-2' });

    expect(value).toBe('value-2');
  });
});
