// Persistent KV store backed by Expo SQLite with an in-memory fallback.
// Lazily initializes SQLite at runtime and migrates any in-memory cache
// into SQLite once available. Uses a simple async API: setItem/getItem/removeItem.

const DB_NAME = 'travelblock.db';
const TABLE_NAME = 'kv_store';

import { createTableIfNeeded, migrateCacheToDbIfNeeded, performSqliteInit, SqliteDB, SqliteTx, tryQuickSqliteInitSync } from './sqlite-helpers';

let db: SqliteDB | null = null;
let initPromise: Promise<void> | null = null;
// Track backend state: `writeBackend` reflects the active persistent
// write backend (used by `getActiveBackend`). `lastOperationBackend`
// reflects the backend used by the most recent operation (reads or writes)
// for per-operation diagnostics. Reads must not mutate `writeBackend`.
let lastOperationBackend: 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' = 'unknown';
let writeBackend: 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' = 'unknown';

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
    // are not available during bundling. Place the require inside the try
    // so synchronous throws from native module loading are caught and
    // cause initPromise to be cleared for retry.
    try {
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      const required: unknown = require('expo-sqlite');
      const opened = performSqliteInit(required as import('./sqlite-helpers').ExpoSqliteModule, DB_NAME);
      if (opened) {
        db = opened;
        await createTableIfNeeded(db, TABLE_NAME);
        await migrateCacheToDbIfNeeded(db, Array.from(cache.entries()), TABLE_NAME);
      }
      // leave initPromise set on success so callers can await the same promise
    } catch (err) {
      // clear initPromise on failure so future callers can retry
      initPromise = null;
      throw err;
    }
  })();

  return initPromise;
}

// performSqliteInit is provided by sqlite-helpers and imported above

export async function setItem(opts: { key: string; value: string }): Promise<void> {
  const { key, value } = opts;
  await initSqliteIfNeeded();
  if (db) {
    writeBackend = 'sqlite';
    lastOperationBackend = 'sqlite';
    await execSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [key, value]);
    // keep in-memory cache in sync with persistent write so synchronous reads succeed
    cache.set(key, value);
    return;
  }

  // Try AsyncStorage as a persistent fallback
  const asModule = loadAsyncStorageOnce();
  if (asModule?.setItem) {
    writeBackend = 'asyncstorage';
    lastOperationBackend = 'asyncstorage';
    await asModule.setItem(key, value);
    // ensure synchronous reads observe the newly-written value
    cache.set(key, value);
    return;
  }

  writeBackend = 'memory';
  lastOperationBackend = 'memory';
  cache.set(key, value);
}

export async function getItem(arg: string | { key: string }): Promise<string | null> {
  const key = typeof arg === 'string' ? arg : arg.key;
  const sqliteVal = await getFromSqlite({ key });
  if (sqliteVal !== null) {
    lastOperationBackend = 'sqlite';
    return sqliteVal;
  }

  const asVal = await getFromAsyncStorage({ key });
  if (asVal !== null) {
    lastOperationBackend = 'asyncstorage';
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
  lastOperationBackend = 'memory';
  const value = cache.get(key);
  return value === undefined ? null : value;
}

export async function removeItem(opts: { key: string }): Promise<void> {
  const { key } = opts;
  quickSqliteInit();
  await initSqliteIfNeeded();
  if (db) {
    writeBackend = 'sqlite';
    lastOperationBackend = 'sqlite';
    await execSql(`DELETE FROM ${TABLE_NAME} WHERE key = ?`, [key]);
    // evict from cache after successful DB deletion
    cache.delete(key);
    return;
  }

  const asModule = loadAsyncStorageOnce();
  if (asModule?.removeItem) {
    writeBackend = 'asyncstorage';
    lastOperationBackend = 'asyncstorage';
    await asModule.removeItem(key);
    // ensure cache cleared even when using AsyncStorage backend
    cache.delete(key);
    return;
  }

  writeBackend = 'memory';
  lastOperationBackend = 'memory';
  cache.delete(key);
}

export function setItemSync(opts: { key: string; value: string }): void {
  // Attempt a quick SQLite init so the sync-path can opportunistically
  // write to SQLite if available. No await here — just trigger the init.
  quickSqliteInit();
  cache.set(opts.key, opts.value);
  // Schedule background write to persistent storage if available.
  // Prefer SQLite when available, otherwise fall back to AsyncStorage.
  const asModule = loadAsyncStorageOnce();
  if (db) {
    writeBackend = 'sqlite';
    lastOperationBackend = 'sqlite';
    execSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [opts.key, opts.value]).catch((err) => {
      // eslint-disable-next-line no-console
      console.warn('setItemSync: background write to SQLite failed', err);
    });
  } else if (asModule?.setItem) {
    writeBackend = 'asyncstorage';
    lastOperationBackend = 'asyncstorage';
    asModule.setItem(opts.key, opts.value).catch((err) => {
      // eslint-disable-next-line no-console
      console.warn('setItemSync: background write to AsyncStorage failed', err);
    });
  } else {
    writeBackend = 'memory';
    lastOperationBackend = 'memory';
  }
}

export function getItemSync(opts: { key: string }): string | null {
  const value = cache.get(opts.key);
  lastOperationBackend = 'memory';
  return value ?? null;
}

// mergeItem: accepts either (key, value) or ({ key, value })
// Helper: normalize arguments into { key, incoming }
function normalizeMergeArgs(arg: string | { key: string; value: string }, value?: string): { key: string; incoming?: string } {
  if (typeof arg === 'string') {
    // Only return incoming when the caller provided a valid second parameter.
    if (typeof value === 'string') {
      return { key: arg, incoming: value };
    }
    return { key: arg };
  }
  return { key: arg.key, incoming: arg.value };
}

// Helper: safely parse JSON returning null on failure
function tryParseJson(s: string | null): unknown {
  if (s === null) return null;
  try {
    return JSON.parse(s);
  } catch {
    return null;
  }
}

// Helper: identify plain (non-null, non-array) objects at runtime
function isPlainObject(v: unknown): v is Record<string, unknown> {
  return v !== null && typeof v === 'object' && !Array.isArray(v);
}

// Helper: merge two JSON-parsable objects shallowly; returns merged JSON string or null if not mergeable
function shallowMergeJsonStrings(existing: string | null, incoming: string): string | null {
  const existingObj = tryParseJson(existing);
  const incomingObj = tryParseJson(incoming);
  if (isPlainObject(existingObj) && isPlainObject(incomingObj)) {
    const merged = Object.assign({}, existingObj, incomingObj);
    return JSON.stringify(merged);
  }
  return null;
}

export async function mergeItem(arg: string | { key: string; value: string }, value?: string): Promise<void> {
  const { key, incoming } = normalizeMergeArgs(arg, value);
  if (!key || typeof incoming !== 'string') {
    throw new TypeError('mergeItem requires a key and a string value');
  }

  // Attempt to merge when possible; otherwise replace the value
  try {
    const existing = await getItem({ key });
    const merged = shallowMergeJsonStrings(existing, incoming);
    const toStore = merged ?? incoming;
    await setItem({ key, value: toStore });
  } catch (err) {
    // On merge error: log the original error, then attempt a fallback
    // replace. If the fallback write fails, log both errors and rethrow
    // so callers can observe the failure.
    // eslint-disable-next-line no-console
    console.error('mergeItem: merge failed, attempting replacement', { key, err });
    try {
      await setItem({ key, value: incoming });
      // replacement succeeded; return normally
      return;
    } catch (fallbackErr) {
      // eslint-disable-next-line no-console
      console.error('mergeItem: fallback setItem failed', { key, err, fallbackErr });
      // Throw an aggregated error so callers are aware of both failures
      throw new Error(
        `mergeItem failed for key ${key}: merge error: ${String(err)}; fallback setItem error: ${String(
          fallbackErr
        )}`
      );
    }
  }
}

/**
 * Initialize the store (attempt to bring up SQLite and migrate cache).
 * Call this early in app startup to prefer persistent storage over the
 * in-memory fallback.
 */
export async function initStore(): Promise<void> {
  await initSqliteIfNeeded();
  // If SQLite is active we've already migrated the in-memory cache into DB
  // inside initSqliteIfNeeded (via migrateCacheToDbIfNeeded). Only attempt
  // to write entries into AsyncStorage if SQLite is NOT active.
  if (db) return;

  // Ensure AsyncStorage is available so it's ready as a fallback
  const asModule = loadAsyncStorageOnce();
  if (asModule?.setItem) {
    // migrate any existing cache into AsyncStorage to avoid data loss
    const successfulKeys: string[] = [];
    for (const [key, value] of cache) {
      try {
        await asModule.setItem(key, value);
        successfulKeys.push(key);
      } catch (err) {
        console.warn(`Failed to migrate key ${key} to AsyncStorage`, err);
      }
    }
    // Only remove successfully migrated keys
    for (const key of successfulKeys) {
      cache.delete(key);
    }
  }
}

// Expose which backend is active for diagnostics or tests.
export function getActiveBackend(): 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' {
  return writeBackend;
}

// Return the backend used by the most recent operation (read or write).
export function getLastOperationBackend(): 'sqlite' | 'asyncstorage' | 'memory' | 'unknown' {
  return lastOperationBackend;
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
