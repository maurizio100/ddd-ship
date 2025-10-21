import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Ship} from "../../models/ship";

export interface ShipState {
  ships: Ship[];
  loading: boolean;
  error: string | null;
}

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
