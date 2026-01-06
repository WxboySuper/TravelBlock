declare module 'expo-sqlite/kv-store' {
  /**
   * A storage object compatible with @react-native-async-storage/async-storage
   * style API with additional synchronous helpers.
   */
  export interface KVStorage {
    // Async API
    setItem(opts: { key: string; value: string }): Promise<void>;
    getItem(opts: { key: string }): Promise<string | null>;
    removeItem(opts: { key: string }): Promise<void>;
    multiGet(keys: string[]): Promise<Array<[string, string | null]>>;
    multiSet(keyValuePairs: Array<[string, string]>): Promise<void>;
    multiRemove(keys: string[]): Promise<void>;
    clear(): Promise<void>;
    getAllKeys(): Promise<string[]>;
    mergeItem(opts: { key: string; value: string }): Promise<void>;

    // Sync API (added by expo-sqlite/kv-store as convenience)
    setItemSync(key: string, value: string): void;
    getItemSync(key: string): string | null;
  }
  // Named exports for convenience and compatibility with different import styles
  export function setItem(opts: { key: string; value: string }): Promise<void>;
  export function getItem(opts: { key: string }): Promise<string | null>;
  export function removeItem(opts: { key: string }): Promise<void>;
  export function multiGet(keys: string[]): Promise<Array<[string, string | null]>>;
  export function multiSet(keyValuePairs: Array<[string, string]>): Promise<void>;
  export function multiRemove(keys: string[]): Promise<void>;
  export function clear(): Promise<void>;
  export function getAllKeys(): Promise<string[]>;
  export function mergeItem(key: string, value: string): Promise<void>;

  // Sync methods support both positional and options-object forms
  export function setItemSync(opts: { key: string; value: string }): void;
  export function setItemSync(key: string, value: string): void;
  export function getItemSync(opts: { key: string }): string | null;
  export function getItemSync(key: string): string | null;

  declare const Storage: KVStorage;
  export default Storage;
}
