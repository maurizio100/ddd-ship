import * as CatainsActions from "./catains.actions";
import {createReducer, on} from "@ngrx/store";
import {CatainsState} from "./catains.selectors";

export const initialState: CatainsState = {
  catains: [],
  loading: false,
  error: null,
}

export const catainsReducers = createReducer(
  initialState,
  on(CatainsActions.loadCatains, (state, {}) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(CatainsActions.loadCatainsSuccess, (state, {catains}) => ({
    ...state,
    catains: catains,
    loading: false,
    error: null,
  }))
)
