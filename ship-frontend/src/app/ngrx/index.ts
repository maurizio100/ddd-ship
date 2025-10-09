import {Ship} from "../models/ship";

export interface ShipState {
  ships: Ship[];
  loading: boolean;
  error: string | null;
}
