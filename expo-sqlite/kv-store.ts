// Persistent KV store backed by Expo SQLite with an in-memory fallback.
// Lazily initializes SQLite at runtime and migrates any in-memory cache
// into SQLite once available. Uses a simple async API: setItem/getItem/removeItem.

const DB_NAME = 'travelblock.db';
const TABLE_NAME = 'kv_store';

import { createTableIfNeeded, migrateCacheToDbIfNeeded, performSqliteInit, SqliteDB, SqliteTx, tryQuickSqliteInitSync } from './sqlite-helpers';

let db: SqliteDB | null = null;
let initPromise: Promise<void> | null = null;
// Track which backend is currently being used (for diagnostics).
let backend: 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' = 'unknown';

// In-memory fallback cache used until SQLite is initialized or when unavailable.
const cache = new Map<string, string>();

type AsyncStorageLike = {
  getItem?: (key: string) => Promise<string | null>;
  setItem?: (key: string, value: string) => Promise<void>;
  removeItem?: (key: string) => Promise<void>;
};

// runtime reference to AsyncStorage (may be null)
let asyncStorage: AsyncStorageLike | null = null;

function quickSqliteInit(): void {
  try {
    const opened = tryQuickSqliteInitSync(DB_NAME);
    if (opened) db = opened;
  } catch {
    // ignore
  }
}

function loadAsyncStorageOnce(): AsyncStorageLike | null {
  if (asyncStorage) return asyncStorage;
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const mod: unknown = require('@react-native-async-storage/async-storage');
    asyncStorage = ((mod as { default?: AsyncStorageLike })?.default ?? (mod as AsyncStorageLike)) as AsyncStorageLike;
    return asyncStorage;
  } catch {
    return null;
  }
}

// cache migration and table creation are handled by helpers in sqlite-helpers.ts

function execSql<T = unknown>(sql: string, params: unknown[] = []): Promise<T> {
  if (!db || typeof db.transaction !== 'function') {
    if (db && typeof db.transaction !== 'function') {
      // eslint-disable-next-line no-console
      console.warn('expo-sqlite: opened DB does not support transactions; falling back to in-memory cache');
      db = null;
    }
    return Promise.reject(new Error('SQLite not initialized'));
  }

  // Narrow db for TypeScript within this scope
  const dbLocal = db as SqliteDB;

  return new Promise((resolve, reject) => {
    try {
      dbLocal.transaction((tx: SqliteTx) => {
        tx.executeSql(
          sql,
          params as unknown[],
          (_tx: unknown, result: unknown) => resolve(result as T),
          (_tx: unknown, err: unknown) => {
            reject(err);
            return false;
          }
        );
      }, (txErr: unknown) => reject(txErr));
    } catch (e) {
      reject(e);
    }
  });
}

function initSqliteIfNeeded(): Promise<void> {
  if (db) return Promise.resolve();
  if (initPromise) return initPromise;

  initPromise = (async () => {
      // Lazily require to avoid Metro/route-analysis errors when native modules
      // are not available during bundling.
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      const required: unknown = require('expo-sqlite');
      try {
        const opened = performSqliteInit(required as import('./sqlite-helpers').ExpoSqliteModule, DB_NAME);
        if (opened) {
          db = opened;
          await createTableIfNeeded(db, TABLE_NAME);
          await migrateCacheToDbIfNeeded(db, Array.from(cache.entries()), TABLE_NAME);
        }
      } finally {
        initPromise = null;
      }
  })();

  return initPromise;
}

// performSqliteInit is provided by sqlite-helpers and imported above

export async function setItem(opts: { key: string; value: string }): Promise<void> {
  const { key, value } = opts;
  // Try a quick init path so we attempt to use SQLite immediately when possible
  quickSqliteInit();
  await initSqliteIfNeeded();
  if (db) {
    backend = 'sqlite';
    await execSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [key, value]);
    return;
  }

  // Try AsyncStorage as a persistent fallback
  const asModule = loadAsyncStorageOnce();
  if (asModule?.setItem) {
    backend = 'asyncstorage';
    await asModule.setItem(key, value);
    return;
  }

  backend = 'memory';
  cache.set(key, value);
}

export async function getItem(arg: string | { key: string }): Promise<string | null> {
  const key = typeof arg === 'string' ? arg : arg.key;
  const sqliteVal = await getFromSqlite({ key });
  if (sqliteVal !== null) {
    backend = 'sqlite';
    return sqliteVal;
  }

  const asVal = await getFromAsyncStorage({ key });
  if (asVal !== null) {
    backend = 'asyncstorage';
    return asVal;
  }

  return getFromMemory({ key });
}

async function getFromSqlite(opts: { key: string }): Promise<string | null> {
  const { key } = opts;
  quickSqliteInit();
  await initSqliteIfNeeded();
  if (!db) return null;
  type SqlResult = { rows?: { length: number; _array?: Array<Record<string, unknown>> } };
  const res = await execSql<SqlResult>(`SELECT value FROM ${TABLE_NAME} WHERE key = ?`, [key]);
  const rows = res.rows;
  if (!rows || rows.length === 0) return null;
  return rows._array?.[0]?.value as string | null ?? null;
}

async function getFromAsyncStorage(opts: { key: string }): Promise<string | null> {
  const { key } = opts;
  const asModule = loadAsyncStorageOnce();
  if (!asModule?.getItem) return null;
  return await asModule.getItem(key);
}

function getFromMemory(opts: { key: string }): string | null {
  const { key } = opts;
  backend = 'memory';
  const value = cache.get(key);
  return value === undefined ? null : value;
}

export async function removeItem(opts: { key: string }): Promise<void> {
  const { key } = opts;
  quickSqliteInit();
  await initSqliteIfNeeded();
  if (db) {
    backend = 'sqlite';
    await execSql(`DELETE FROM ${TABLE_NAME} WHERE key = ?`, [key]);
    return;
  }

  const asModule = loadAsyncStorageOnce();
  if (asModule?.removeItem) {
    backend = 'asyncstorage';
    await asModule.removeItem(key);
    return;
  }

  backend = 'memory';
  cache.delete(key);
}

export function setItemSync(opts: { key: string; value: string }): void {
  cache.set(opts.key, opts.value);
}

export function getItemSync(opts: { key: string }): string | null {
  return cache.get(opts.key) ?? null;
}

/**
 * Initialize the store (attempt to bring up SQLite and migrate cache).
 * Call this early in app startup to prefer persistent storage over the
 * in-memory fallback.
 */
export async function initStore(): Promise<void> {
  await initSqliteIfNeeded();
  // Ensure AsyncStorage is available so it's ready as a fallback
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const mod = require('@react-native-async-storage/async-storage');
    asyncStorage = ((mod as { default?: AsyncStorageLike })?.default ?? (mod as AsyncStorageLike)) as AsyncStorageLike;
    if (asyncStorage) {
      // migrate any existing cache into AsyncStorage to avoid data loss
      for (const [k, v] of Array.from(cache.entries())) {
        try {
          if (asyncStorage.setItem) await asyncStorage.setItem(k, v);
        } catch (_) {
          // ignore individual failures
        }
      }
      if (cache.size > 0) cache.clear();
    }
  } catch (_) {
    // not available
  }
}

// Expose which backend is active for diagnostics or tests.
export function getActiveBackend(): 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' {
  return backend;
}

export default {
  setItem,
  getItem,
  removeItem,
  setItemSync,
  getItemSync,
};
