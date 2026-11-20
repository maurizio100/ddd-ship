import {createReducer, on} from "@ngrx/store";
import {ShipState} from "../selectors/ship.selectors";
import * as ShipActions from "../actions/ship.actions";

export const initialState: ShipState = {
  ships: [],
  shipClasses: [],
  shipClassesUnavailable: false,
  loading: false,
  error: null,
  arrivalNotices: [],
  imprisonmentNotices: [],
  newShippingRefusal: null,
}

export const shipReducers = createReducer(
  initialState,
  on(ShipActions.loadShips, (state, {}) => ({
      ...state,
      loading: true,
      error: null
  })),
  on(ShipActions.loadShipsSuccess, (state, {ships}) => ({
    ...state,
    ships: ships,
    loading: false,
    error: null,
    newShippingRefusal: null
  })),

  on(ShipActions.loadShipClassesSuccess, (state, {shipClasses}) => ({
    ...state,
    shipClasses,
    shipClassesUnavailable: false
  })),

  on(ShipActions.loadShipClassesFailure, (state, {}) => ({
    ...state,
    shipClassesUnavailable: true
  })),

  on(ShipActions.addShip, (state, {}) => ({
      ...state,
      loading: true,
      error: null
  })),

  on(ShipActions.addShipSuccess, (state, ship) => ({
    ...state,
    ships: [...state.ships, ship],
    loading: false,
    error: null
  })),

  on(ShipActions.addShipFailure, (state, {error}) => ({
    ...state,
    loading: false,
    error: error
  })),

  on(ShipActions.shipArrived, (state, {shipId, shipName, originHarbor, imprisoned, crew}) => ({
    ...state,
    arrivalNotices: [
      ...state.arrivalNotices.filter(notice => notice.shipId !== shipId),
      {shipId, shipName, originHarbor, imprisoned, crew: crew ?? []}
    ]
  })),

  on(ShipActions.shipLeft, (state, {shipId}) => ({
    ...state,
    ships: state.ships.filter(ship => ship.id !== shipId)
  })),

  on(ShipActions.dismissArrivalNotice, (state, {shipId}) => ({
    ...state,
    arrivalNotices: state.arrivalNotices.filter(notice => notice.shipId !== shipId)
  })),

  /** Only a ship that sails home gets a notice; one imprisoned in place shows its "Imprisoned" chip after the reload. */
  on(ShipActions.shipImprisoned, (state, {shipId, shipName, homeHarbor, sailsHome}) => sailsHome ? ({
    ...state,
    imprisonmentNotices: [
      ...state.imprisonmentNotices.filter(notice => notice.shipId !== shipId),
      {shipId, shipName, homeHarbor}
    ]
  }) : state),

  on(ShipActions.dismissImprisonmentNotice, (state, {shipId}) => ({
    ...state,
    imprisonmentNotices: state.imprisonmentNotices.filter(notice => notice.shipId !== shipId)
  })),

  on(ShipActions.createShipping, (state) => ({
    ...state,
    newShippingRefusal: null
  })),

  on(ShipActions.createShippingFailure, (state, {shipId, error}) => ({
    ...state,
    newShippingRefusal: {shipId, message: error}
  })),
)
