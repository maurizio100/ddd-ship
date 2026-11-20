import { createAction, props } from '@ngrx/store';

export const hireRecruit = createAction('[Crew] Hire Recruit', props<{ shipId: string; recruitId: string }>());
