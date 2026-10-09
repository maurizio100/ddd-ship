import { createReducer, on } from '@ngrx/store';
import { HarborState } from './harbor.selectors';
import * as HarborActions from './harbor.actions';

export const initialState: HarborState = {
  stock: [],
  savings: null,
  loading: false,
  error: null,
  purchaseRefusal: null,
};

export const harborReducers = createReducer(
  initialState,
  on(HarborActions.loadStock, (state) => ({ ...state, loading: true, error: null })),
  on(HarborActions.loadStockSuccess, (state, { stock }) => ({ ...state, stock, loading: false, error: null })),
  on(HarborActions.loadStockFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(HarborActions.loadSavings, (state) => ({ ...state, loading: true, error: null })),
  on(HarborActions.loadSavingsSuccess, (state, { savings }) => ({ ...state, savings, loading: false, error: null })),
  on(HarborActions.loadSavingsFailure, (state, { error }) => ({ ...state, loading: false, error })),
);
