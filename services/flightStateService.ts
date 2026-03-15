import type { BoardingPass, FlightBooking } from '@/types/flight';
import type { Airport } from '@/types/airport';

type FlightStateWriters = {
  setBooking: (booking: FlightBooking | null) => Promise<void>;
  setDestination: (airport: Airport | null) => Promise<void>;
  setBoardingPass: (boardingPass: BoardingPass | null) => Promise<void>;
};

export async function persistDivertedFlightState(
  newBooking: FlightBooking,
  boardingPass: BoardingPass | null,
  writers: FlightStateWriters
): Promise<void> {
  await Promise.all([
    writers.setBooking(newBooking),
    writers.setDestination(newBooking.destination),
    boardingPass
      ? writers.setBoardingPass({
          ...boardingPass,
          booking: newBooking,
        })
      : Promise.resolve(),
  ]);
}
