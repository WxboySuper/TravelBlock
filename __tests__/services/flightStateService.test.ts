import { persistDivertedFlightState } from '@/services/flightStateService';
import type { Airport } from '@/types/airport';
import type { BoardingPass, FlightBooking, Seat } from '@/types/flight';

const originAirport: Airport = {
  icao: 'KORD',
  iata: 'ORD',
  name: "O'Hare International Airport",
  city: 'Chicago',
  state: 'IL',
  country: 'US',
  elevation: 672,
  lat: 41.9742,
  lon: -87.9073,
  tz: 'America/Chicago',
};

const divertedAirport: Airport = {
  icao: 'KPIT',
  iata: 'PIT',
  name: 'Pittsburgh International Airport',
  city: 'Pittsburgh',
  state: 'PA',
  country: 'US',
  elevation: 1203,
  lat: 40.4915,
  lon: -80.2329,
  tz: 'America/New_York',
};

const divertedBooking: FlightBooking = {
  flightNumber: 'TB 1847D',
  origin: originAirport,
  destination: divertedAirport,
  durationSeconds: 1800,
  distanceKm: 320,
  departureTime: '2026-03-15T12:00:00.000Z',
  arrivalTime: '2026-03-15T12:30:00.000Z',
  boardingTime: '2026-03-15T11:30:00.000Z',
  gate: 'A12',
  terminal: 'A',
  aircraft: {
    type: 'narrowbody',
    name: 'Boeing 737-800',
    seatConfig: [3, 3],
    economyRows: [6, 30],
    premiumRows: [1, 5],
    businessRows: null,
    totalCapacity: 180,
  },
  bookingReference: 'TB4F9X',
  bookedAt: '2026-03-15T11:45:00.000Z',
};

const seat: Seat = {
  row: 12,
  letter: 'A',
  seatClass: 'economy' as Seat['seatClass'],
  pointsCost: 0,
  isAvailable: true,
  isExitRow: true,
};

const boardingPass: BoardingPass = {
  booking: divertedBooking,
  seat,
  passengerName: 'Traveler',
  qrCodeData: 'QRDATA',
  serialNumber: 'TB20260315001',
  isCheckedIn: true,
  checkedInAt: '2026-03-15T11:50:00.000Z',
  isBoarded: true,
};

describe('persistDivertedFlightState', () => {
  it('updates booking, destination, and boarding pass together', async () => {
    const writers = {
      setBooking: jest.fn(() => Promise.resolve()),
      setDestination: jest.fn(() => Promise.resolve()),
      setBoardingPass: jest.fn(() => Promise.resolve()),
    };

    await persistDivertedFlightState(divertedBooking, boardingPass, writers);

    expect(writers.setBooking).toHaveBeenCalledWith(divertedBooking);
    expect(writers.setDestination).toHaveBeenCalledWith(divertedAirport);
    expect(writers.setBoardingPass).toHaveBeenCalledWith({
      ...boardingPass,
      booking: divertedBooking,
    });
  });

  it('skips boarding pass updates when no boarding pass exists yet', async () => {
    const writers = {
      setBooking: jest.fn(() => Promise.resolve()),
      setDestination: jest.fn(() => Promise.resolve()),
      setBoardingPass: jest.fn(() => Promise.resolve()),
    };

    await persistDivertedFlightState(divertedBooking, null, writers);

    expect(writers.setBooking).toHaveBeenCalledWith(divertedBooking);
    expect(writers.setDestination).toHaveBeenCalledWith(divertedAirport);
    expect(writers.setBoardingPass).not.toHaveBeenCalled();
  });
});
