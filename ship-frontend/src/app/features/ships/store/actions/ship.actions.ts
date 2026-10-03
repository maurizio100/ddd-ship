import {createAction, props} from "@ngrx/store";
import {Ship} from "../../models/ship";
import {NewShipRequest} from "../../models/new-ship-request";
import {ShipArrived, ShipLeft} from "../../models/fleet-event";

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
