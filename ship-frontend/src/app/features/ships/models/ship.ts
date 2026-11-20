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
  /** True for an Imprisoned Ship, or a ship at sea coming home imprisoned. */
  imprisoned: boolean;
  shipClass: ShipClass;
  crewCapacity: number;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}

export enum ShipClass {
  SLOOP = 'SLOOP',
  BRIG = 'BRIG',
  GALLEON = 'GALLEON',
}

export const SHIP_CLASS_LABELS: Record<ShipClass, string> = {
  [ShipClass.SLOOP]: 'Sloop',
  [ShipClass.BRIG]: 'Brig',
  [ShipClass.GALLEON]: 'Galleon',
};

/** The status wording of a ship: an Imprisoned Ship in port, else by its Shipping. */
export function shipStatusLabel(ship: Ship): string {
  if (ship.imprisoned && ship.shippingState !== ShippingState.SHIPPING) return 'Imprisoned';
  switch (ship.shippingState) {
    case ShippingState.PREPARING:
      return 'Preparing';
    case ShippingState.SHIPPING:
      return 'At sea';
    default:
      return 'In port';
  }
}
