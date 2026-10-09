import { createReducer, on } from '@ngrx/store';
import { HarborState } from './harbor.selectors';
import * as HarborActions from './harbor.actions';

export const initialState: HarborState = {
  stock: [],
  savings: null,
  incomingShips: [],
  loading: false,
  error: null,
  purchaseRefusal: null,
  unloadRefusal: null,
};

export const harborReducers = createReducer(
  initialState,
  on(HarborActions.loadStock, (state) => ({ ...state, loading: true, error: null })),
  on(HarborActions.loadStockSuccess, (state, { stock }) => ({ ...state, stock, loading: false, error: null })),
  on(HarborActions.loadStockFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(HarborActions.loadSavings, (state) => ({ ...state, loading: true, error: null })),
  on(HarborActions.loadSavingsSuccess, (state, { savings }) => ({ ...state, savings, loading: false, error: null })),
  on(HarborActions.loadSavingsFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(HarborActions.loadIncomingShipsSuccess, (state, { incomingShips }) => ({ ...state, incomingShips })),
  on(HarborActions.loadIncomingShipsFailure, (state, { error }) => ({ ...state, error })),
  on(HarborActions.buyCargo, (state) => ({ ...state, purchaseRefusal: null })),
  on(HarborActions.buyCargoSuccess, (state) => ({ ...state, purchaseRefusal: null })),
  on(HarborActions.buyCargoFailure, (state, { refusal }) => ({ ...state, purchaseRefusal: refusal })),
);
