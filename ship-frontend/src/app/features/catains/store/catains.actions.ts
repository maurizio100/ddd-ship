import {createAction, props} from "@ngrx/store";
import {Catain} from "../model/catain";

const catainsActions = {
  loadCatains: '[Catain] Load Catains',
  loadCatainsSuccess: '[Catain] Load Catains Success',
  loadCatainsFailure: '[Catain] Load Catains Failure'
}

export const loadCatains = createAction(
  catainsActions.loadCatains
);

export const loadCatainsSuccess = createAction(
  catainsActions.loadCatainsSuccess,
  props<{catains: Catain[]}>()
);

export const loadCatainsFailure = createAction(
  catainsActions.loadCatainsFailure
);
