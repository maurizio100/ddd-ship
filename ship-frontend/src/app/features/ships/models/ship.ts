export interface Ship {
  id: string;
  name: string;
  catain: string;
  shippingState: ShippingState | null;
  /** The Origin Harbor of the ship's latest Arrival at this Harbor; null for a ship registered here. */
  arrivedFrom: string | null;
  /** True for a ship that arrived with Cargo aboard and must be unloaded or refused first. */
  incoming: boolean;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}
