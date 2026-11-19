import {createAction, props} from "@ngrx/store";
import {Ship} from "../../models/ship";
import {NewShipRequest} from "../../models/new-ship-request";
import {ShipArrived, ShipImprisoned, ShipLeft} from "../../models/fleet-event";
import {ShippingSummary} from "../../../shipping/models/shipping-summary";

const shipActions = {
  loadShips: '[Ship] Load Ships',
  loadShipsSuccess: '[Ship] Load Ships Success',
  loadShipsFailed: '[Ship] Load Ships Failed',
  addShip: '[Ship] Add Ship',
  addShipSuccess: '[Ship] Add Ship Success',
  addShipFailure: '[Ship] Add Ship Failure',
  watchFleet: '[Ship] Watch Fleet',
  stopWatchingFleet: '[Ship] Stop Watching Fleet',
  shipArrived: '[Ship] Ship Arrived',
  shipLeft: '[Ship] Ship Left',
  dismissArrivalNotice: '[Ship] Dismiss Arrival Notice',
  shipImprisoned: '[Ship] Ship Imprisoned',
  dismissImprisonmentNotice: '[Ship] Dismiss Imprisonment Notice',
  createShipping: '[Ship] Create Shipping',
  createShippingSuccess: '[Ship] Create Shipping Success',
  createShippingFailure: '[Ship] Create Shipping Failure',
}

export const loadShips = createAction(
  shipActions.loadShips
);

export const loadShipsSuccess = createAction(
  shipActions.loadShipsSuccess,
  props<{ships: Ship[]}>()
);

export const loadShipsFailed = createAction(
  shipActions.loadShipsFailed
);

export const addShip = createAction(
  shipActions.addShip,
  props<NewShipRequest>()
);

export const addShipSuccess = createAction(
  shipActions.addShipSuccess,
  props<Ship>()
);

export const addShipFailure = createAction(
  shipActions.addShipFailure,
  props<{error: string}>()
);

/** Start listening to this Harbor's fleet changes as they happen. */
export const watchFleet = createAction(
  shipActions.watchFleet
);

export const stopWatchingFleet = createAction(
  shipActions.stopWatchingFleet
);

/** Pushed by the backend: a ship arrived at this Harbor. */
export const shipArrived = createAction(
  shipActions.shipArrived,
  props<ShipArrived>()
);

/** Pushed by the backend: a ship arrived elsewhere and left this Harbor's fleet. */
export const shipLeft = createAction(
  shipActions.shipLeft,
  props<ShipLeft>()
);

export const dismissArrivalNotice = createAction(
  shipActions.dismissArrivalNotice,
  props<{shipId: string}>()
);

/** Pushed by the backend: the Coast Guard imprisoned a ship here and its Cargo is confiscated. */
export const shipImprisoned = createAction(
  shipActions.shipImprisoned,
  props<ShipImprisoned>()
);

export const dismissImprisonmentNotice = createAction(
  shipActions.dismissImprisonmentNotice,
  props<{shipId: string}>()
);

/** The User starts a new Shipping for a ship. */
export const createShipping = createAction(
  shipActions.createShipping,
  props<{ship: Ship}>()
);

export const createShippingSuccess = createAction(
  shipActions.createShippingSuccess,
  props<{shippingSummary: ShippingSummary}>()
);

/** The new Shipping was refused; [error] is what the User is told. */
export const createShippingFailure = createAction(
  shipActions.createShippingFailure,
  props<{shipId: string; error: string}>()
);
