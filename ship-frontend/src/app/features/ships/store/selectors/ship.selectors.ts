import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Ship} from "../../models/ship";
import {ArrivalNotice} from "../../models/fleet-event";

export interface ShipState {
  ships: Ship[];
  loading: boolean;
  error: string | null;
  /** Ships that have just arrived, until the notice is dismissed. */
  arrivalNotices: ArrivalNotice[];
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

export const selectArrivalNotices = createSelector(
  selectShipsState,
  state => state.arrivalNotices
);
