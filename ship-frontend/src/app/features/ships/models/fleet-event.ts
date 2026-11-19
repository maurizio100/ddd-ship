/**
 * Data of the `ship-arrived` fleet event: a ship arrived at this Harbor from its Origin Harbor; `imprisoned` when it
 * arrived at its Home Harbor as an Imprisoned Ship.
 */
export interface ShipArrived {
  shipId: string;
  shipName: string;
  originHarbor: string;
  imprisoned: boolean;
}

/** Data of the `ship-left` fleet event: a ship arrived at its Destination Harbor and left this Harbor's fleet. */
export interface ShipLeft {
  shipId: string;
  shipName: string;
  destinationHarbor: string | null;
}

/**
 * Data of the `ship-imprisoned` fleet event: the Coast Guard imprisoned a ship here and its Cargo is confiscated; it
 * sails home to its Home Harbor, or, at its Home Harbor, stays here imprisoned.
 */
export interface ShipImprisoned {
  shipId: string;
  shipName: string;
  homeHarbor: string;
  sailsHome: boolean;
}

/** A fleet change pushed by the backend over `GET /web/fleet-events`. */
export type FleetEvent =
  | { type: 'ship-arrived'; ship: ShipArrived }
  | { type: 'ship-left'; ship: ShipLeft }
  | { type: 'ship-imprisoned'; ship: ShipImprisoned }
  /** The stream has just (re)opened: anything missed meanwhile has to be fetched. */
  | { type: 'connected' };

/** What the User is told about a ship that has just arrived. */
export interface ArrivalNotice {
  shipId: string;
  shipName: string;
  originHarbor: string;
  /** Whether the ship arrived at its Home Harbor as an Imprisoned Ship. */
  imprisoned: boolean;
}

/** What the User is told about a ship the Coast Guard imprisoned here and that sails home. */
export interface ImprisonmentNotice {
  shipId: string;
  shipName: string;
  homeHarbor: string;
}
