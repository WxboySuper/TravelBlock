// clearCache is imported dynamically after resetting modules to ensure
// it references the fresh module instance.

describe('airportService edge branches', () => {
  it('works when String.prototype.normalize is not available', async () => {
    // Temporarily remove normalize to exercise fallback path
     
    const proto = String.prototype as any;
    const orig = proto.normalize;

    try {
      // remove normalize
      proto.normalize = undefined;
      // Clear cache and require the service to run load path. Import both
      // `loadAirports` and `clearCache` dynamically so they reference the
      // freshly loaded module instance after `jest.resetModules()`.
      jest.resetModules();
      const { loadAirports, clearCache } = await import('../../services/airportService');
      await clearCache();
      const data = await loadAirports();
      expect(data).toBeDefined();
      expect(Object.keys(data).length).toBeGreaterThan(0);
    } finally {
      // restore
      proto.normalize = orig;
      jest.resetModules();
    }
  });

  it('handles non-string fields in airport records via defensive defaults', async () => {
    // Mock the airports.json to contain non-string fields and ensure loader uses defaults
    jest.resetModules();

    jest.doMock('../../data/airports.json', () => ({
      MIXED: {
        icao: null,
        iata: 123,
        name: null,
        city: 456,
        country: null,
        elevation: 0,
        lat: 0,
        lon: 0,
        tz: 'UTC',
      },
    }));

    try {
      const { loadAirports, clearCache } = await import('../../services/airportService');
      await clearCache();
      const data = await loadAirports();
      // Should have loaded the mocked key
      expect(data).toHaveProperty('MIXED');

      const mixed = (data as unknown as Record<string, unknown>)['MIXED'] as Record<string, unknown>;
      // The loader should produce normalized __lc* fields as strings
      expect(typeof mixed['__lcName']).toBe('string');
      expect(typeof mixed['__lcCity']).toBe('string');
      // IATA numeric should be coerced to string in __lcIata
      expect(typeof mixed['__lcIata']).toBe('string');
    } finally {
      // Remove the manual mock and reset module registry so other tests are unaffected
      jest.dontMock('../../data/airports.json');
      jest.resetModules();
    }
  });
});
