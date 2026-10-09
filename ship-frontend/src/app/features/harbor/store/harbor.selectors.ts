import { createFeatureSelector, createSelector } from '@ngrx/store';
import { StockedCargo } from '../models/stocked-cargo';
import { Savings } from '../models/savings';
import { IncomingShip } from '../models/incoming-ship';

export interface HarborState {
  stock: StockedCargo[];
  savings: Savings | null;
  incomingShips: IncomingShip[];
  loading: boolean;
  error: unknown;
  /** Why the last purchase at the Market was refused, in the backend's words; null when it was not. */
  purchaseRefusal: string | null;
  /** Why the last unloading of an Incoming Ship was refused, in the backend's words; null when it was not. */
  unloadRefusal: string | null;
  /** The Incoming Ships whose unloading is in flight; their Unload button stays disabled. */
  unloadingShipIds: string[];
  /** Why the last refusal of an Incoming Ship failed, in the backend's words; null when it did not. */
  refuseFailure: string | null;
  /** The Incoming Ships whose refusal is in flight; their Unload and Refuse buttons stay disabled. */
  refusingShipIds: string[];
}

export const selectHarborState = createFeatureSelector<HarborState>('harbor');

export const selectStock = createSelector(selectHarborState, (state) => state.stock);

export const selectStockLoading = createSelector(selectHarborState, (state) => state.loading);

export const selectStockError = createSelector(selectHarborState, (state) => state.error);

export const selectSavings = createSelector(selectHarborState, (state) => state.savings);

export const selectIncomingShips = createSelector(selectHarborState, (state): IncomingShip[] => state.incomingShips);

export const selectPurchaseRefusal = createSelector(selectHarborState, (state) => state.purchaseRefusal);

export const selectUnloadRefusal = createSelector(selectHarborState, (state) => state.unloadRefusal);

export const selectUnloadingShipIds = createSelector(selectHarborState, (state) => state.unloadingShipIds ?? []);

export const selectRefuseFailure = createSelector(selectHarborState, (state) => state.refuseFailure);

export const selectRefusingShipIds = createSelector(selectHarborState, (state) => state.refusingShipIds ?? []);
