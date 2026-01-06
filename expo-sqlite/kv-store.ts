// Simple in-memory KV store used during development and testing. Persistence
// is intentionally ephemeral in this branch (v0.2.0-alpha).
// This file uses strict typing and always stores string values in the cache.
const cache = new Map<string, string>();

export function setItem(key: string, value: string): Promise<void> {
  cache.set(key, value);
  return Promise.resolve();
}

export function getItem(key: string): Promise<string | null> {
  const value = cache.get(key);
  return Promise.resolve(value === undefined ? null : value);
}

export function removeItem(key: string): Promise<void> {
  cache.delete(key);
  return Promise.resolve();
}

export function setItemSync(key: string, value: string): void {
  cache.set(key, value);
}

export function getItemSync(key: string): string | null {
  const value = cache.get(key);
  return value === undefined ? null : value;
}

export default {
  setItem,
  getItem,
  removeItem,
  setItemSync,
  getItemSync,
};
