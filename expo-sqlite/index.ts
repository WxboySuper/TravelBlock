/* eslint-disable */
// Minimal dev shim for `expo-sqlite` used during development.
// Keep this file intentionally simple; it implements a small subset of the
// `executeSql`/`transaction` behavior required by the project's KV store.
export function openDatabase(_name: string) {
  type MockTx = {
    executeSql: (
      sql: string,
      params?: unknown[],
      success?: (tx: unknown, res?: unknown) => void,
      error?: (tx: unknown, err: unknown) => boolean | void,
    ) => void;
  };

  const tx: MockTx = {
    // executeSql(sql, params?, success?, error?)
    executeSql(sql: string, params?: unknown[], success?: (tx: unknown, res?: unknown) => void, error?: (tx: unknown, err: unknown) => boolean | void) {
      // Simulate an error path for SQL that contains the token `__SIM_ERROR__`.
      const shouldError = typeof sql === 'string' && sql.indexOf('__SIM_ERROR__') !== -1;
      if (shouldError) {
        const err = new Error('Simulated SQLite error');
        if (typeof error === 'function') {
          try {
            // Allow the consumer to return `true` to indicate the error was handled
            // or nothing / false otherwise.
            const handled = error(tx, err);
            if (handled) return;
          } catch (e) {
            // If the error callback itself throws, fall through to console.error below.
            // eslint-disable-next-line no-console
            console.error('Error handler threw in mock executeSql:', e);
          }
        }
        // If no error callback provided or it didn't handle the error, call console.error
        // to surface the simulated failure in test logs, but don't throw here.
        // eslint-disable-next-line no-console
        console.error('expo-sqlite mock executeSql error:', err);
        return;
      }

      // Default: invoke success callback with an empty rows result
      if (typeof success === 'function') {
        const result = { rows: { length: 0, item: (_i: number) => undefined } };
        success(tx, result);
      }
    },
  };

  return {
    // transaction(cb, errorCallback?, successCallback?)
    transaction(cb: (tx: MockTx) => void, errorCallback?: (err: any) => boolean | void, successCallback?: () => void) {
      try {
        cb(tx);
        if (typeof successCallback === 'function') successCallback();
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error('expo-sqlite mock transaction error:', err);
        if (typeof errorCallback === 'function') {
          try {
            errorCallback(err);
          } catch (e) {
            // eslint-disable-next-line no-console
            console.error('expo-sqlite mock transaction error handler threw:', e);
          }
        }
      }
    },
  };
}

export default { openDatabase };
