import { Cargo } from './cargo';

export interface Ship {
  id: string;
  name: string;
  catain: string;
  cargo: Cargo[];
  weight: number;
  maxweight: number;
  shippingState: ShippingState | null;
  /** The Origin Harbor of the ship's latest Arrival at this Harbor; null for a ship registered here. */
  arrivedFrom: string | null;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}
