/**
 * Flight booking and boarding types
 *
 * @module types/flight
 */

import type { Airport } from './airport';

/**
 * Seat class types
 */
export enum SeatClass {
  Economy = 'economy',
  PremiumEconomy = 'premium-economy',
  Business = 'business',
}

/**
 * Aircraft type based on flight duration
 */
export type AircraftType = 'regional' | 'narrowbody' | 'widebody';

/**
 * Seat configuration for aircraft cabin
 */
export interface AircraftConfig {
  /** Aircraft type */
  type: AircraftType;
  /** Aircraft model name */
  name: string;
  /** Seats per row (e.g., [2,2] or [3,3]) */
  seatConfig: number[];
  /** Economy class row range [start, end] */
  economyRows: [number, number];
  /** Premium Economy class row range [start, end] or null if not available */
  premiumRows: [number, number] | null;
  /** Business class row range [start, end] or null if not available */
  businessRows: [number, number] | null;
  /** Total passenger capacity */
  totalCapacity: number;
}

/**
 * Individual seat in aircraft
 */
export interface Seat {
  /** Row number */
  row: number;
  /** Seat letter (A, B, C, etc.) */
  letter: string;
  /** Seat class */
  seatClass: SeatClass;
  /** Points cost for this seat (0 = free, future monetization) */
  pointsCost: number;
  /** Whether this seat is available for selection */
  isAvailable: boolean;
  /** Whether this is an exit row with extra legroom */
  isExitRow: boolean;
}

/**
 * Flight booking details
 */
export interface FlightBooking {
  /** Flight number (e.g., "TB 1847") */
  flightNumber: string;
  /** Origin airport */
  origin: Airport;
  /** Destination airport */
  destination: Airport;
  /** Flight duration in seconds */
  durationSeconds: number;
  /** Distance in kilometers */
  distanceKm: number;
  /** Departure time (ISO 8601 string) */
  departureTime: string;
  /** Arrival time (ISO 8601 string) */
  arrivalTime: string;
  /** Boarding time (ISO 8601 string) */
  boardingTime: string;
  /** Departure gate (e.g., "A15") */
  gate: string;
  /** Terminal (e.g., "1", "A") */
  terminal: string;
  /** Aircraft configuration for this flight */
  aircraft: AircraftConfig;
  /** Booking reference code (e.g., "TB4F9X") */
  bookingReference: string;
  /** Timestamp when booking was created */
  bookedAt: string;
}

/**
 * Boarding pass data
 */
export interface BoardingPass {
  /** Flight booking details */
  booking: FlightBooking;
  /** Selected seat */
  seat: Seat;
  /** Passenger name */
  passengerName: string;
  /** QR code data string */
  qrCodeData: string;
  /** Boarding pass serial number */
  serialNumber: string;
  /** Whether passenger has checked in */
  isCheckedIn: boolean;
  /** Check-in timestamp (ISO 8601 string) */
  checkedInAt: string | null;
  /** Whether boarding pass has been scanned/torn */
  isBoarded: boolean;
}

/**
 * Flight status
 */
export enum FlightStatus {
  Scheduled = 'scheduled',
  Boarding = 'boarding',
  Departed = 'departed',
  InFlight = 'in-flight',
  Arrived = 'arrived',
  Cancelled = 'cancelled',
  Delayed = 'delayed',
}
