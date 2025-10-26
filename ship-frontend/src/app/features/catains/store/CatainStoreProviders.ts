import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { catainsReducers } from './catains.reducers';
import { CatainsEffects } from './catains.effects';

export const CATAIN_STORE_PROVIDERS = [
  provideState('catains', catainsReducers),
  provideEffects([CatainsEffects])
];
