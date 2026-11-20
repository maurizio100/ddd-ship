import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { crewReducers } from './crew.reducers';
import { CrewEffects } from './crew.effects';

export const CREW_STORE_PROVIDERS = [provideState('crew', crewReducers), provideEffects([CrewEffects])];
