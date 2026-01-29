
import { loadAirports, getAirportsWithinDistance } from '../services/airportService';
import { performance } from 'perf_hooks';

async function run() {
  console.log('Loading airports...');
  await loadAirports();
  console.log('Airports loaded.');

  const iterations = 100;
  const origin = { lat: 40.7128, lon: -74.0060 }; // NYC
  const radius = 50; // 50 miles

  console.log(`Running ${iterations} iterations of getAirportsWithinDistance (radius: ${radius} miles)...`);

  const start = performance.now();

  for (let i = 0; i < iterations; i++) {
    getAirportsWithinDistance(origin, radius);
  }

  const end = performance.now();
  const totalTime = end - start;
  const avgTime = totalTime / iterations;

  console.log(`Total time: ${totalTime.toFixed(2)}ms`);
  console.log(`Average time per call: ${avgTime.toFixed(2)}ms`);
}

run().catch(console.error);
