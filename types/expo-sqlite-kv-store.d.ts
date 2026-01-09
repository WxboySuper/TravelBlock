declare module 'expo-sqlite/kv-store' {
  /**
   * A storage object compatible with @react-native-async-storage/async-storage
   * style API with additional synchronous helpers.
   */
  export interface KVStorage {
    // Async API
    // setItem supports both options-object and positional overloads
    setItem(key: string, value: string): Promise<void>;
    setItem(opts: { key: string; value: string }): Promise<void>;
    getItem(arg: string | { key: string }): Promise<string | null>;
    removeItem(opts: { key: string }): Promise<void>;
    multiGet(keys: string[]): Promise<Array<[string, string | null]>>;
    multiSet(keyValuePairs: Array<[string, string]>): Promise<void>;
    multiRemove(keys: string[]): Promise<void>;
    clear(): Promise<void>;
    getAllKeys(): Promise<string[]>;
    // mergeItem supports both positional and options-object overloads
    mergeItem(key: string, value: string): Promise<void>;
    mergeItem(opts: { key: string; value: string }): Promise<void>;

    // Sync API (added by expo-sqlite/kv-store as convenience)
    // Accept either positional args or an opts object for compatibility.
    setItemSync(key: string, value: string): void;
    setItemSync(opts: { key: string; value: string }): void;
    getItemSync(key: string): string | null;
    getItemSync(opts: { key: string }): string | null;
  }
  // Named exports for convenience and compatibility with different import styles
  // setItem supports both options-object and positional overloads
  export function setItem(key: string, value: string): Promise<void>;
  export function setItem(opts: { key: string; value: string }): Promise<void>;
  // Implementation signature compatible with both overloads
  export function setItem(arg: any, value?: any): Promise<void>;
  export function getItem(arg: string | { key: string }): Promise<string | null>;
  export function removeItem(opts: { key: string }): Promise<void>;
  export function multiGet(keys: string[]): Promise<Array<[string, string | null]>>;
  export function multiSet(keyValuePairs: Array<[string, string]>): Promise<void>;
  export function multiRemove(keys: string[]): Promise<void>;
  export function clear(): Promise<void>;
  export function getAllKeys(): Promise<string[]>;
  // mergeItem supports both positional and options-object overloads
  export function mergeItem(key: string, value: string): Promise<void>;
  export function mergeItem(opts: { key: string; value: string }): Promise<void>;
  // Implementation signature compatible with both overloads
  export function mergeItem(arg: any, value?: any): Promise<void>;

  // Sync methods accept either positional args or an options-object
  export function setItemSync(key: string, value: string): void;
  export function setItemSync(opts: { key: string; value: string }): void;
  export function setItemSync(arg: any, value?: any): void;

  export function getItemSync(key: string): string | null;
  export function getItemSync(opts: { key: string }): string | null;
  export function getItemSync(arg: any): string | null;

  declare const Storage: KVStorage;
  export default Storage;
}
