export interface Ship {
  id: string;
  name: string;
  catain: string;
  shippingState: ShippingState | null;
  /** The Harbor where the ship was registered; never changes. */
  homeHarbor: string;
  /** The Origin Harbor of the ship's latest Arrival at this Harbor; null for a ship registered here. */
  arrivedFrom: string | null;
  /** True for a ship that arrived with Cargo aboard and must be unloaded or refused first. */
  incoming: boolean;
  /** The Earnings the ship carries until it reaches its Home Harbor, a decimal string with two decimals, '0.00' for none. */
  earnings: string;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}
