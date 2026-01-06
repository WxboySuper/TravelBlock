// Persistent KV store backed by Expo SQLite with an in-memory fallback.
// Lazily initializes SQLite at runtime and migrates any in-memory cache
// into SQLite once available. Uses a simple async API: setItem/getItem/removeItem.

const DB_NAME = 'travelblock.db';
const TABLE_NAME = 'kv_store';

type SqliteTx = {
  executeSql: (
    sql: string,
    params?: unknown[],
    success?: (tx: unknown, result: unknown) => void,
    error?: (tx: unknown, err: unknown) => boolean | undefined
  ) => void;
};
type SqliteDB = {
  transaction: (fn: (tx: SqliteTx) => void, error?: (err: unknown) => void, success?: () => void) => void;
};
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

type ExpoSqliteModule = {
  openDatabase?: (name: string) => SqliteDB;
  openDatabaseSync?: (name: string) => SqliteDB;
};

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
    try {
      // Lazily require to avoid Metro/route-analysis errors when native modules
      // are not available during bundling.
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      const required: unknown = require('expo-sqlite');
      const sqliteMod = required as ExpoSqliteModule;
      if (sqliteMod && typeof sqliteMod.openDatabase === 'function') {
        const openFn = sqliteMod.openDatabase;
        const opened = typeof openFn === 'function' ? openFn(DB_NAME) : null;
        if (opened && typeof opened.transaction === 'function') {
          db = opened as SqliteDB;
          // attempt to create table but don't block on failure
          try {
            // eslint-disable-next-line @typescript-eslint/no-floating-promises
            execSql(`CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (key TEXT PRIMARY KEY NOT NULL, value TEXT)`);
          } catch (_) {
            // ignore
          }
          // migrate cache
          if (cache.size > 0 && db) {
            const entries = Array.from(cache.entries());
            await new Promise<void>((resolve, reject) => {
              (db as SqliteDB).transaction((tx: SqliteTx) => {
                for (const [k, v] of entries) {
                  tx.executeSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [k, v]);
                }
              }, (err: unknown) => reject(err), () => resolve());
            });
            cache.clear();
          }
        } else {
          // eslint-disable-next-line no-console
          console.warn('expo-sqlite: openDatabase returned an object without transaction(); skipping SQLite initialization');
          db = null;
        }
      }
    } catch (e) {
      // SQLite not available; keep using in-memory cache
      db = null;
    } finally {
      initPromise = null;
    }
  })();

  return initPromise;
}

export async function setItem(key: string, value: string): Promise<void> {
  // Try a quick init path so we attempt to use SQLite immediately when possible
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const maybeSqlite: unknown = require('expo-sqlite');
    const sqliteMod = maybeSqlite as ExpoSqliteModule;
    if (sqliteMod && !db) {
      const openSync = sqliteMod.openDatabaseSync;
      const open = sqliteMod.openDatabase;
      const opened = typeof openSync === 'function'
        ? openSync(DB_NAME)
        : (typeof open === 'function' ? open(DB_NAME) : null);
      if (opened && typeof opened.transaction === 'function') {
        db = opened as SqliteDB;
      } else {
        // eslint-disable-next-line no-console
        console.warn('expo-sqlite: quick init returned DB without transaction(); will fallback to in-memory');
        db = null;
      }
    }
  } catch (e) {
    // ignore and fall back to init path
  }

  await initSqliteIfNeeded();
  if (db) {
    backend = 'sqlite';
    await execSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [key, value]);
    return;
  }

  // Try AsyncStorage as a persistent fallback
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const mod: unknown = require('@react-native-async-storage/async-storage');
    asyncStorage = ((mod as { default?: AsyncStorageLike })?.default ?? (mod as AsyncStorageLike)) as AsyncStorageLike;
    if (asyncStorage?.setItem) {
      backend = 'asyncstorage';
      await asyncStorage.setItem(key, value);
      return;
    }
  } catch (_) {
    // fall through to memory
  }

  backend = 'memory';
  cache.set(key, value);
}

export async function getItem(key: string): Promise<string | null> {
  try {
    // attempt quick init so we can read from SQLite immediately
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const maybeSqlite: unknown = require('expo-sqlite');
    const sqliteMod = maybeSqlite as ExpoSqliteModule;
    if (sqliteMod && !db) {
      const openSync = sqliteMod.openDatabaseSync;
      const open = sqliteMod.openDatabase;
      const opened = typeof openSync === 'function'
        ? openSync(DB_NAME)
        : (typeof open === 'function' ? open(DB_NAME) : null);
      if (opened && typeof opened.transaction === 'function') db = opened as SqliteDB;
    }
  } catch (e) {
    // ignore
  }

  await initSqliteIfNeeded();
  if (db) {
    backend = 'sqlite';
    type SqlResult = { rows?: { length: number; _array?: Array<Record<string, unknown>> } };
    const res = await execSql<SqlResult>(`SELECT value FROM ${TABLE_NAME} WHERE key = ?`, [key]);
    const rows = res.rows;
    if (!rows || rows.length === 0) return null;
    return rows._array?.[0]?.value as string | null ?? null;
  }

  // Try AsyncStorage
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const mod: unknown = require('@react-native-async-storage/async-storage');
    asyncStorage = ((mod as { default?: AsyncStorageLike })?.default ?? (mod as AsyncStorageLike)) as AsyncStorageLike;
    if (asyncStorage?.getItem) {
      backend = 'asyncstorage';
      const asyncResult = await asyncStorage.getItem(key);
      return asyncResult;
    }
  } catch (_) {
    // ignore
  }

  backend = 'memory';
  const value = cache.get(key);
  return value === undefined ? null : value;
}

export async function removeItem(key: string): Promise<void> {
  try {
    // attempt quick init so we can remove from SQLite immediately
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const maybeSqlite: unknown = require('expo-sqlite');
    const sqliteMod = maybeSqlite as ExpoSqliteModule;
    if (sqliteMod && !db) {
      const openSync = sqliteMod.openDatabaseSync;
      const open = sqliteMod.openDatabase;
      db = typeof openSync === 'function'
        ? openSync(DB_NAME)
        : (typeof open === 'function' ? open(DB_NAME) : null) as SqliteDB | null;
    }
  } catch (e) {
    // ignore
  }

  await initSqliteIfNeeded();
  if (db) {
    backend = 'sqlite';
    await execSql(`DELETE FROM ${TABLE_NAME} WHERE key = ?`, [key]);
    return;
  }

  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const mod: unknown = require('@react-native-async-storage/async-storage');
    asyncStorage = ((mod as { default?: AsyncStorageLike })?.default ?? (mod as AsyncStorageLike)) as AsyncStorageLike;
    if (asyncStorage?.removeItem) {
      backend = 'asyncstorage';
      await asyncStorage.removeItem(key);
      return;
    }
  } catch (_) {
    // ignore
  }

  backend = 'memory';
  cache.delete(key);
}

export function setItemSync(_key: string, _value: string): void {
  cache.set(_key, _value);
}

export function getItemSync(_key: string): string | null {
  return cache.get(_key) ?? null;
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
