import {Catain} from "../model/catain";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface CatainsState {
  catains: Catain[];
  loading: boolean;
  error: string | null;
}

export const selectCatainsState = createFeatureSelector<CatainsState>('catains');

export const selectAllCatains = createSelector(
  selectCatainsState,
  state => state.catains
);

export const selectError = createSelector(
  selectCatainsState,
  state => state.error
);

export const selectLoading = createSelector(
  selectCatainsState,
  state => state.loading
);
