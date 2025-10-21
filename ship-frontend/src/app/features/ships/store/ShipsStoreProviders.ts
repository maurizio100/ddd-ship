import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { shipReducers } from './reducers/ship.reducers';
import { ShipEffects } from './effects/ship.effects';

export const SHIPS_STORE_PROVIDERS = [
  provideState('ships', shipReducers),
  provideEffects([ShipEffects])
];
