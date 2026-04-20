import type { Airport } from "@/types/airport";
import type { Position } from "@/utils/flightInterpolation";

export type FlightMapProvider = "google" | "maplibre";

export interface FlightMapAdapterProps {
  origin: Airport;
  destination: Airport;
  currentPosition: Position;
  heading: number;
  isDiverted?: boolean;
  theme: "light" | "dark";
}
