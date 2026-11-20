import { createFeatureSelector, createSelector } from '@ngrx/store';
import { StockedCargo } from '../models/stocked-cargo';
import { Savings } from '../models/savings';
import { IncomingShip } from '../models/incoming-ship';
import { Recruit } from '../models/recruit';
import { ImprisonedShip } from '../models/imprisoned-ship';
import { FinePayment } from '../models/fine-payment';

export interface HarborState {
  stock: StockedCargo[];
  savings: Savings | null;
  incomingShips: IncomingShip[];
  recruitPool: Recruit[];
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
  /** The Imprisoned Ships at this, their Home, Harbor, each with its Fine. */
  imprisonedShips: ImprisonedShip[];
  /** The last paid Fine, shown on the fleet until dismissed; null when there is none. */
  fineNotice: FinePayment | null;
  /** Why the last Fine payment was refused, for which ship, in the backend's words; null when it was not. */
  fineRefusal: { shipId: string; message: string } | null;
  /** The Imprisoned Ships whose Fine payment is in flight; their Pay Fine button stays disabled. */
  payingFineShipIds: string[];
}

export const selectHarborState = createFeatureSelector<HarborState>('harbor');

export const selectStock = createSelector(selectHarborState, (state) => state.stock);

export const selectStockLoading = createSelector(selectHarborState, (state) => state.loading);

export const selectStockError = createSelector(selectHarborState, (state) => state.error);

export const selectSavings = createSelector(selectHarborState, (state) => state.savings);

export const selectRecruitPool = createSelector(selectHarborState, (state): Recruit[] => state.recruitPool);
export const selectIncomingShips = createSelector(selectHarborState, (state): IncomingShip[] => state.incomingShips);

export const selectPurchaseRefusal = createSelector(selectHarborState, (state) => state.purchaseRefusal);

export const selectUnloadRefusal = createSelector(selectHarborState, (state) => state.unloadRefusal);

export const selectUnloadingShipIds = createSelector(selectHarborState, (state) => state.unloadingShipIds ?? []);

export const selectRefuseFailure = createSelector(selectHarborState, (state) => state.refuseFailure);

export const selectRefusingShipIds = createSelector(selectHarborState, (state) => state.refusingShipIds ?? []);

/**
 * The Fine selectors read the root state, not the `harbor` feature, so they are safe while that feature is not
 * registered (the app bar reads them on every page): they read no Imprisoned Ship then.
 */
const selectHarborStateIfRegistered = (state: { harbor?: HarborState }): HarborState | undefined => state.harbor;

const NO_SHIPS: ImprisonedShip[] = [];
const NO_IDS: string[] = [];

export const selectImprisonedShips = createSelector(
  selectHarborStateIfRegistered,
  (state): ImprisonedShip[] => state?.imprisonedShips ?? NO_SHIPS,
);

export const selectFineNotice = createSelector(selectHarborStateIfRegistered, (state) => state?.fineNotice ?? null);

export const selectFineRefusal = createSelector(selectHarborStateIfRegistered, (state) => state?.fineRefusal ?? null);

export const selectPayingFineShipIds = createSelector(
  selectHarborStateIfRegistered,
  (state): string[] => state?.payingFineShipIds ?? NO_IDS,
);

/** Whether this Harbor holds an Imprisoned Ship; false while the `harbor` feature is not registered. */
export const selectHasImprisonedShips = (state: { harbor?: HarborState }): boolean =>
  (state.harbor?.imprisonedShips?.length ?? 0) > 0;
