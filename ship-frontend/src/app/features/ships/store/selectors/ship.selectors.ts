import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Ship, ShipClassOffer} from "../../models/ship";
import {ArrivalNotice, ImprisonmentNotice} from "../../models/fleet-event";

/** A new Shipping the backend refused for the ship [shipId], with what the User is told. */
export interface NewShippingRefusal {
  shipId: string;
  message: string;
}

export interface ShipState {
  ships: Ship[];
  /** The Ship Classes on offer with their Crew Capacity, as the backend lists them. */
  shipClasses: ShipClassOffer[];
  loading: boolean;
  error: string | null;
  /** Ships that have just arrived, until the notice is dismissed. */
  arrivalNotices: ArrivalNotice[];
  /** Ships the Coast Guard imprisoned here that sail home, until the notice is dismissed. */
  imprisonmentNotices: ImprisonmentNotice[];
  /** The last refused new Shipping, until the next attempt or reload. */
  newShippingRefusal: NewShippingRefusal | null;
}

export const selectShipsState = createFeatureSelector<ShipState>('ships');

export const selectAllShips = createSelector(
  selectShipsState,
  state => state.ships
);

export const selectShipClasses = createSelector(
  selectShipsState,
  state => state.shipClasses
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

export const selectImprisonmentNotices = createSelector(
  selectShipsState,
  state => state.imprisonmentNotices
);

export const selectNewShippingRefusal = createSelector(
  selectShipsState,
  state => state.newShippingRefusal
);
