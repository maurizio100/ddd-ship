import { createFeatureSelector, createSelector } from '@ngrx/store';
import { StockedCargo } from '../models/stocked-cargo';

export interface HarborState {
  stock: StockedCargo[];
  loading: boolean;
  error: unknown;
}

export const selectHarborState = createFeatureSelector<HarborState>('harbor');

export const selectStock = createSelector(selectHarborState, (state) => state.stock);

export const selectStockLoading = createSelector(selectHarborState, (state) => state.loading);

export const selectStockError = createSelector(selectHarborState, (state) => state.error);
