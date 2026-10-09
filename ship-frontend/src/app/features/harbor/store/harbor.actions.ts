import {createAction, props} from '@ngrx/store';
import {StockedCargo} from '../models/stocked-cargo';
import {Savings} from '../models/savings';
import {IncomingShip} from '../models/incoming-ship';

export const loadStock = createAction('[Harbor] Load Stock');

export const loadStockSuccess = createAction(
  '[Harbor] Load Stock Success',
  props<{stock: StockedCargo[]}>()
);

export const loadStockFailure = createAction(
  '[Harbor] Load Stock Failure',
  props<{error: unknown}>()
);

export const loadSavings = createAction('[Harbor] Load Savings');

export const loadSavingsSuccess = createAction(
  '[Harbor] Load Savings Success',
  props<{savings: Savings}>()
);

export const loadSavingsFailure = createAction(
  '[Harbor] Load Savings Failure',
  props<{error: unknown}>()
);

export const buyCargo = createAction(
  '[Harbor] Buy Cargo',
  props<{cargoId: string; quantity: number}>()
);

export const buyCargoSuccess = createAction('[Harbor] Buy Cargo Success');

export const buyCargoFailure = createAction(
  '[Harbor] Buy Cargo Failure',
  props<{error: unknown; refusal: string}>()
);

export const loadIncomingShips = createAction('[Harbor] Load Incoming Ships');

export const loadIncomingShipsSuccess = createAction(
  '[Harbor] Load Incoming Ships Success',
  props<{incomingShips: IncomingShip[]}>()
);

export const loadIncomingShipsFailure = createAction(
  '[Harbor] Load Incoming Ships Failure',
  props<{error: unknown}>()
);

export const watchArrivals = createAction('[Harbor] Watch Arrivals');

export const stopWatchingArrivals = createAction('[Harbor] Stop Watching Arrivals');

export const unloadIncomingShip = createAction(
  '[Harbor] Unload Incoming Ship',
  props<{shipId: string}>()
);

export const unloadIncomingShipSuccess = createAction('[Harbor] Unload Incoming Ship Success');

export const unloadIncomingShipFailure = createAction(
  '[Harbor] Unload Incoming Ship Failure',
  props<{error: unknown; refusal: string}>()
);
