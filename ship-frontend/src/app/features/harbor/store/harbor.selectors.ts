import { createFeatureSelector, createSelector } from '@ngrx/store';
import { StockedCargo } from '../models/stocked-cargo';
import { Savings } from '../models/savings';

export interface HarborState {
  stock: StockedCargo[];
  savings: Savings | null;
  loading: boolean;
  error: unknown;
  /** Why the last purchase at the Market was refused, in the backend's words; null when it was not. */
  purchaseRefusal: string | null;
}

export const selectHarborState = createFeatureSelector<HarborState>('harbor');

export const selectStock = createSelector(selectHarborState, (state) => state.stock);

export const selectStockLoading = createSelector(selectHarborState, (state) => state.loading);

export const selectStockError = createSelector(selectHarborState, (state) => state.error);

export const selectSavings = createSelector(selectHarborState, (state) => state.savings);

export const selectPurchaseRefusal = createSelector(selectHarborState, (state) => state.purchaseRefusal);
