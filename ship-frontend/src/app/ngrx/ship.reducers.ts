import {createReducer, on} from "@ngrx/store";
import {ShipState} from "./index";
import * as ShipActions from "./ship.actions";

export const initialState: ShipState = {
  ships: [],
  loading: false,
  error: null
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
    error: null
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
)
