/** Data of the `ship-arrived` fleet event: a ship arrived at this Harbor from its Origin Harbor. */
export interface ShipArrived {
  shipId: string;
  shipName: string;
  originHarbor: string;
}

/** Data of the `ship-left` fleet event: a ship arrived at its Destination Harbor and left this Harbor's fleet. */
export interface ShipLeft {
  shipId: string;
  shipName: string;
  destinationHarbor: string | null;
}

/** A fleet change pushed by the backend over `GET /web/fleet-events`. */
export type FleetEvent =
  | { type: 'ship-arrived'; ship: ShipArrived }
  | { type: 'ship-left'; ship: ShipLeft };

/** What the User is told about a ship that has just arrived. */
export interface ArrivalNotice {
  shipId: string;
  shipName: string;
  originHarbor: string;
}
