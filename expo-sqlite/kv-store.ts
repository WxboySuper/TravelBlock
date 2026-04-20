/* eslint-disable @typescript-eslint/no-require-imports */

type KvStoreLike = {
  getItem: (key: string) => Promise<string | null>;
  setItem: (key: string, value: string) => Promise<void>;
  removeItem: (key: string) => Promise<void>;
  mergeItem?: (key: string, value: string) => Promise<void>;
  getItemSync?: (key: string) => string | null;
  setItemSync?: (key: string, value: string) => void;
};

const memoryCache = new Map<string, string>();

function loadExpoKvStore(): KvStoreLike | null {
  try {
    const mod = require('expo-sqlite/kv-store') as { AsyncStorage?: KvStoreLike } & KvStoreLike;
    return mod.AsyncStorage ?? mod;
  } catch (error) {
    console.warn('expo-sqlite/kv-store unavailable, falling back to memory cache', error);
    return null;
  }
}

const persistentStore = loadExpoKvStore();

export async function setItem(opts: { key: string; value: string }): Promise<void> {
  const { key, value } = opts;
  if (persistentStore?.setItem) {
    await persistentStore.setItem(key, value);
  }
  memoryCache.set(key, value);
}

export async function getItem(arg: string | { key: string }): Promise<string | null> {
  const key = typeof arg === 'string' ? arg : arg.key;
  if (persistentStore?.getItem) {
    const persisted = await persistentStore.getItem(key);
    if (persisted !== null) {
      memoryCache.set(key, persisted);
      return persisted;
    }
  }

  return memoryCache.get(key) ?? null;
}

export async function removeItem(opts: { key: string }): Promise<void> {
  const { key } = opts;
  if (persistentStore?.removeItem) {
    await persistentStore.removeItem(key);
  }
  memoryCache.delete(key);
}

export function setItemSync(opts: { key: string; value: string }): void {
  const { key, value } = opts;
  if (persistentStore?.setItemSync) {
    persistentStore.setItemSync(key, value);
  }
  memoryCache.set(key, value);
}

export function getItemSync(opts: { key: string }): string | null {
  if (persistentStore?.getItemSync) {
    const persisted = persistentStore.getItemSync(opts.key);
    if (persisted !== null) {
      memoryCache.set(opts.key, persisted);
      return persisted;
    }
  }
  return memoryCache.get(opts.key) ?? null;
}

export async function mergeItem(arg: string | { key: string; value: string }, value?: string): Promise<void> {
  const key = typeof arg === 'string' ? arg : arg.key;
  const incoming = typeof arg === 'string' ? value : arg.value;
  if (typeof incoming !== 'string') {
    throw new TypeError('mergeItem requires a key and a string value');
  }

  if (persistentStore?.mergeItem) {
    await persistentStore.mergeItem(key, incoming);
    const merged = await persistentStore.getItem(key);
    if (merged !== null) {
      memoryCache.set(key, merged);
    }
    return;
  }

  await setItem({ key, value: incoming });
}

export async function initStore(): Promise<void> {
  // The official Expo kv-store initializes itself on first use.
}

export function getActiveBackend(): 'sqlite' | 'memory' {
  return persistentStore ? 'sqlite' : 'memory';
}

export function getLastOperationBackend(): 'sqlite' | 'memory' {
  return persistentStore ? 'sqlite' : 'memory';
}

export default {
  setItem,
  getItem,
  removeItem,
  setItemSync,
  getItemSync,
  mergeItem,
  initStore,
  getActiveBackend,
  getLastOperationBackend,
};
