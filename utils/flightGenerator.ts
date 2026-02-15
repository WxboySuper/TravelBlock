/**
 * Flight data generation utilities
 *
 * Generates realistic flight numbers, gates, times, seat maps, and booking data.
 *
 * @module utils/flightGenerator
 */

import type { Airport } from '@/types/airport';
import {
    AircraftConfig,
    BoardingPass,
    FlightBooking,
    Seat,
    SeatClass,
} from '@/types/flight';
import { calculateDistance } from './distance';

/** TravelBlock airline code */
const AIRLINE_CODE = 'TB';

/** Available gate ranges by terminal */
const GATES = {
  A: 30, // A1-A30
  B: 20, // B1-B20
  C: 15, // C1-C15
};

/** Seat letters (excluding I to avoid confusion with 1) */
const SEAT_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K'];

/**
 * Get aircraft configuration based on flight duration.
 *
 * - Short-haul (<90 min): Regional jet, 2-2 layout, Economy only
 * - Medium-haul (90min-3.5hr): Narrowbody, 3-3 layout, Premium Economy + Economy
 * - Long-haul (>3.5hr): Widebody, 3-3-3 layout, Business + Premium Economy + Economy
 *
 * @param durationSeconds - Flight duration in seconds
 * @returns Aircraft configuration
 */
export function getAircraftForFlightTime(
  durationSeconds: number
): AircraftConfig {
  const durationMinutes = durationSeconds / 60;

  // Short-haul: <90 minutes - Regional jet
  if (durationMinutes < 90) {
    return {
      type: 'regional',
      name: 'Embraer E-175',
      seatConfig: [2, 2],
      economyRows: [1, 20],
      premiumRows: null,
      businessRows: null,
      totalCapacity: 80,
    };
  }

  // Medium-haul: 90min - 3.5hr - Narrowbody
  if (durationMinutes < 210) {
    // 3.5 hours = 210 minutes
    return {
      type: 'narrowbody',
      name: 'Boeing 737-800',
      seatConfig: [3, 3],
      premiumRows: [1, 5], // Front
      economyRows: [6, 30], // Rear
      businessRows: null,
      totalCapacity: 180,
    };
  }

  // Long-haul: >3.5hr - Widebody
  return {
    type: 'widebody',
    name: 'Boeing 787-9 Dreamliner',
    seatConfig: [3, 3, 3],
    businessRows: [1, 6], // Front
    premiumRows: [7, 12], // Middle
    economyRows: [13, 42], // Rear
    totalCapacity: 240,
  };
}

/**
 * Generate a realistic flight number.
 *
 * Format: "TB ####" where #### is a 3-4 digit number based on route.
 *
 * @param origin - Origin airport
 * @param destination - Destination airport
 * @returns Flight number (e.g., "TB 1847")
 */
export function generateFlightNumber(
  origin: Airport,
  destination: Airport
): string {
  // Generate pseudo-random but consistent number based on ICAO codes
  const seed =
    origin.icao.charCodeAt(0) * 1000 +
    destination.icao.charCodeAt(0) * 100 +
    origin.icao.charCodeAt(1) * 10 +
    destination.icao.charCodeAt(1);

  const flightNum = (seed % 9000) + 1000; // 1000-9999 range
  return `${AIRLINE_CODE} ${flightNum}`;
}

/**
 * Generate a random gate.
 *
 * @returns Gate string (e.g., "A15", "B7", "C12")
 */
export function generateGate(): string {
  const terminals = Object.keys(GATES) as Array<keyof typeof GATES>;
  const terminal = terminals[Math.floor(Math.random() * terminals.length)];
  const gateMax = GATES[terminal];
  const gateNum = Math.floor(Math.random() * gateMax) + 1;
  return `${terminal}${gateNum}`;
}

/**
 * Generate departure time (current time + 15 minute buffer).
 *
 * @returns ISO 8601 timestamp
 */
export function generateDepartureTime(): string {
  const now = new Date();
  const departure = new Date(now.getTime() + 15 * 60 * 1000); // +15 minutes
  return departure.toISOString();
}

/**
 * Calculate arrival time based on departure time and flight duration.
 *
 * @param departureTime - ISO 8601 departure time
 * @param durationSeconds - Flight duration in seconds
 * @returns ISO 8601 arrival time
 */
export function calculateArrivalTime(
  departureTime: string,
  durationSeconds: number
): string {
  const departure = new Date(departureTime);
  const arrival = new Date(departure.getTime() + durationSeconds * 1000);
  return arrival.toISOString();
}

/**
 * Calculate boarding time (departure time - 30 minutes).
 *
 * @param departureTime - ISO 8601 departure time
 * @returns ISO 8601 boarding time
 */
export function generateBoardingTime(departureTime: string): string {
  const departure = new Date(departureTime);
  const boarding = new Date(departure.getTime() - 30 * 60 * 1000); // -30 minutes
  return boarding.toISOString();
}

/**
 * Generate a booking reference code.
 *
 * Format: 6 alphanumeric characters (e.g., "TB4F9X")
 *
 * @returns Booking reference code
 */
export function generateBookingReference(): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // Exclude I, O, 0, 1
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

/**
 * Generate seat map for aircraft.
 *
 * @param aircraft - Aircraft configuration
 * @returns Array of available seats
 */
export function generateSeatMap(aircraft: AircraftConfig): Seat[] {
  const seats: Seat[] = [];
  const seatsPerRow = aircraft.seatConfig.reduce((sum, n) => sum + n, 0);

  // Helper to get seat class for a row
  const getSeatClass = (row: number): SeatClass => {
    if (
      aircraft.businessRows &&
      row >= aircraft.businessRows[0] &&
      row <= aircraft.businessRows[1]
    ) {
      return SeatClass.Business;
    }
    if (
      aircraft.premiumRows &&
      row >= aircraft.premiumRows[0] &&
      row <= aircraft.premiumRows[1]
    ) {
      return SeatClass.PremiumEconomy;
    }
    return SeatClass.Economy;
  };

  // Helper to get points cost (currently all free)
  const getPointsCost = (seatClass: SeatClass): number => {
    // Future: return different costs based on class
    // For now, all seats are free
    return 0;
  };

  // Determine row range
  const minRow = Math.min(
    aircraft.economyRows[0],
    aircraft.premiumRows?.[0] ?? Infinity,
    aircraft.businessRows?.[0] ?? Infinity
  );
  const maxRow = Math.max(
    aircraft.economyRows[1],
    aircraft.premiumRows?.[1] ?? 0,
    aircraft.businessRows?.[1] ?? 0
  );

  // Generate seats for each row
  for (let row = minRow; row <= maxRow; row++) {
    const seatClass = getSeatClass(row);
    const pointsCost = getPointsCost(seatClass);
    const isExitRow = row === 12 || row === 24; // Common exit row positions

    for (let i = 0; i < seatsPerRow; i++) {
      seats.push({
        row,
        letter: SEAT_LETTERS[i],
        seatClass,
        pointsCost,
        isAvailable: true,
        isExitRow,
      });
    }
  }

  return seats;
}

/**
 * Generate complete flight booking.
 *
 * @param origin - Origin airport
 * @param destination - Destination airport
 * @param durationSeconds - Flight duration in seconds
 * @returns Complete flight booking
 */
export function generateFlightBooking(
  origin: Airport,
  destination: Airport,
  durationSeconds: number
): FlightBooking {
  const flightNumber = generateFlightNumber(origin, destination);
  const gate = generateGate();
  const terminal = gate.charAt(0);
  const departureTime = generateDepartureTime();
  const arrivalTime = calculateArrivalTime(departureTime, durationSeconds);
  const boardingTime = generateBoardingTime(departureTime);
  const aircraft = getAircraftForFlightTime(durationSeconds);
  const bookingReference = generateBookingReference();
  const distanceKm = calculateDistance(
    origin.lat,
    origin.lon,
    destination.lat,
    destination.lon
  );

  return {
    flightNumber,
    origin,
    destination,
    durationSeconds,
    distanceKm,
    departureTime,
    arrivalTime,
    boardingTime,
    gate,
    terminal,
    aircraft,
    bookingReference,
    bookedAt: new Date().toISOString(),
  };
}

/**
 * Generate QR code data string from boarding pass information.
 *
 * @param booking - Flight booking
 * @param seat - Selected seat
 * @param passengerName - Passenger name
 * @returns QR code data string
 */
export function generateQRCodeData(
  booking: FlightBooking,
  seat: Seat,
  passengerName: string
): string {
  // Format: FLIGHTNUM|ORIGIN|DEST|SEAT|GATE|NAME
  return `${booking.flightNumber}|${booking.origin.icao}|${booking.destination.icao}|${seat.row}${seat.letter}|${booking.gate}|${passengerName}`;
}

/**
 * Generate boarding pass serial number.
 *
 * @returns Serial number (e.g., "TB20260215001")
 */
export function generateSerialNumber(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const seq = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `${AIRLINE_CODE}${year}${month}${day}${seq}`;
}

/**
 * Generate complete boarding pass.
 *
 * @param booking - Flight booking
 * @param seat - Selected seat
 * @param passengerName - Passenger name (default: "Traveler")
 * @returns Complete boarding pass
 */
export function generateBoardingPass(
  booking: FlightBooking,
  seat: Seat,
  passengerName: string = 'Traveler'
): BoardingPass {
  const qrCodeData = generateQRCodeData(booking, seat, passengerName);
  const serialNumber = generateSerialNumber();

  return {
    booking,
    seat,
    passengerName,
    qrCodeData,
    serialNumber,
    isCheckedIn: true,
    checkedInAt: new Date().toISOString(),
    isBoarded: false,
  };
}
