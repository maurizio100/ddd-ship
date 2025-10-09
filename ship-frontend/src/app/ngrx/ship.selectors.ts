import {createFeatureSelector, createSelector} from "@ngrx/store";
import {ShipState} from ".";

export const selectShipsState = createFeatureSelector<ShipState>('ships');

export const selectAllShips = createSelector(
  selectShipsState,
  state => state.ships
);

export const selectError = createSelector(
  selectShipsState,
  state => state.error
);

export const selectLoading = createSelector(
  selectShipsState,
  state => state.loading
);
