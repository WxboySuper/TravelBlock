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
    // mergeItem supports both options-object and positional forms
    mergeItem(arg: { key: string; value: string } | string, value?: string): Promise<void>;

    // Sync API (added by expo-sqlite/kv-store as convenience)
    setItemSync(arg: { key: string; value: string } | string, value?: string): void;
    getItemSync(arg: { key: string } | string): string | null;
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
  // mergeItem supports both options-object and positional forms
  export function mergeItem(arg: { key: string; value: string } | string, value?: string): Promise<void>;

  // Sync methods accept either an options-object or positional args
  export function setItemSync(arg: { key: string; value: string } | string, value?: string): void;
  export function getItemSync(arg: { key: string } | string): string | null;

  declare const Storage: KVStorage;
  export default Storage;
}
