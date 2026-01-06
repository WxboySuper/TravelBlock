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

export type ExpoSqliteModule = {
  openDatabase?: (name: string) => SqliteDB;
  openDatabaseSync?: (name: string) => SqliteDB;
};

export function tryQuickSqliteInitSync(DB_NAME: string): SqliteDB | null {
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const maybeSqlite: unknown = require('expo-sqlite');
    const sqliteMod = maybeSqlite as ExpoSqliteModule;
    if (!sqliteMod) return null;
    const openSync = sqliteMod.openDatabaseSync;
    const open = sqliteMod.openDatabase;
    const opened = typeof openSync === 'function'
      ? openSync(DB_NAME)
      : (typeof open === 'function' ? open(DB_NAME) : null);
    if (opened && typeof opened.transaction === 'function') return opened as SqliteDB;
  } catch {
    // ignore
  }
  return null;
}

export function performSqliteInit(mod: ExpoSqliteModule | null | undefined, DB_NAME: string): SqliteDB | null {
  if (!mod || typeof mod.openDatabase !== 'function') return null;
  const open = mod.openDatabase;
  const opened = typeof open === 'function' ? open(DB_NAME) : null;
  if (!opened || typeof opened.transaction !== 'function') return null;
  return opened as SqliteDB;
}

// eslint-disable-next-line @typescript-eslint/require-await
export async function createTableIfNeeded(db: SqliteDB, TABLE_NAME: string): Promise<void> {
  try {
    await new Promise<void>((resolve, reject) => {
      db.transaction((tx: SqliteTx) => {
        tx.executeSql(`CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (key TEXT PRIMARY KEY NOT NULL, value TEXT)`);
      }, (err: unknown) => reject(err), () => resolve());
    });
  } catch {
    // ignore failures
  }
}

// eslint-disable-next-line @typescript-eslint/require-await
export async function migrateCacheToDbIfNeeded(db: SqliteDB, entries: Array<[string, string]>, TABLE_NAME: string): Promise<void> {
  if (!entries || entries.length === 0) return;
  await new Promise<void>((resolve, reject) => {
    db.transaction((tx: SqliteTx) => {
      for (const [k, v] of entries) {
        tx.executeSql(`INSERT OR REPLACE INTO ${TABLE_NAME} (key, value) VALUES (?, ?)`, [k, v]);
      }
    }, (err: unknown) => reject(err), () => resolve());
  });
}

export type { SqliteDB, SqliteTx };
