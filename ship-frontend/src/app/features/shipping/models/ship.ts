import { Cargo } from './cargo';

export interface Ship {
  id: string;
  name: string;
  catain: string;
  cargo: Cargo[];
  /** Cargo this Harbor refused aboard its own ship: it cannot be unloaded here, and it sails with the next Release. */
  cargoAboard: Cargo[];
  weight: number;
  maxweight: number;
  shippingState: ShippingState | null;
  /** The Harbor where the ship was registered; never changes. */
  homeHarbor: string;
  /** The Origin Harbor of the ship's latest Arrival at this Harbor; null for a ship registered here. */
  arrivedFrom: string | null;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}
