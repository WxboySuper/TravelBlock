declare module 'expo-sqlite/kv-store' {
  /**
   * A storage object compatible with @react-native-async-storage/async-storage
   * style API with additional synchronous helpers.
   */
  export interface KVStorage {
    // Async API
    setItem(key: string, value: string): Promise<void>;
    getItem(key: string): Promise<string | null>;
    removeItem(key: string): Promise<void>;
    multiGet(keys: string[]): Promise<Array<[string, string | null]>>;
    multiSet(keyValuePairs: Array<[string, string]>): Promise<void>;
    multiRemove(keys: string[]): Promise<void>;
    clear(): Promise<void>;
    getAllKeys(): Promise<string[]>;
    mergeItem(key: string, value: string): Promise<void>;

    // Sync API (added by expo-sqlite/kv-store as convenience)
    setItemSync(key: string, value: string): void;
    getItemSync(key: string): string | null;
  }

  declare const Storage: KVStorage;
  export default Storage;
}
