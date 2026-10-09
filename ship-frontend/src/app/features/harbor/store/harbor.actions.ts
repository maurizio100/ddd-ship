import {createAction, props} from '@ngrx/store';
import {StockedCargo} from '../models/stocked-cargo';
import {Savings} from '../models/savings';

export const loadStock = createAction('[Harbor] Load Stock');

export const loadStockSuccess = createAction(
  '[Harbor] Load Stock Success',
  props<{stock: StockedCargo[]}>()
);

export const loadStockFailure = createAction(
  '[Harbor] Load Stock Failure',
  props<{error: unknown}>()
);

export const loadSavings = createAction('[Harbor] Load Savings');

export const loadSavingsSuccess = createAction(
  '[Harbor] Load Savings Success',
  props<{savings: Savings}>()
);

export const loadSavingsFailure = createAction(
  '[Harbor] Load Savings Failure',
  props<{error: unknown}>()
);
