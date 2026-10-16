import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { harborReducers } from './harbor.reducers';
import { HarborEffects } from './harbor.effects';

export const HARBOR_STORE_PROVIDERS = [provideState('harbor', harborReducers), provideEffects([HarborEffects])];
