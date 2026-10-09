import {createAction, props} from '@ngrx/store';
import {StockedCargo} from '../models/stocked-cargo';

export const loadStock = createAction('[Harbor] Load Stock');

export const loadStockSuccess = createAction(
  '[Harbor] Load Stock Success',
  props<{stock: StockedCargo[]}>()
);

export const loadStockFailure = createAction(
  '[Harbor] Load Stock Failure',
  props<{error: unknown}>()
);
