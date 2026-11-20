import { createAction, props } from '@ngrx/store';
import { Hire } from '../models/hire';

export const hireRecruit = createAction('[Crew] Hire Recruit', props<{ shipId: string; recruitId: string }>());
export const hireRecruitSuccess = createAction('[Crew] Hire Recruit Success', props<{ hire: Hire }>());
export const hireRecruitFailure = createAction(
  '[Crew] Hire Recruit Failure',
  props<{ recruitId: string; error: unknown; refusal: string; savingsShort: boolean }>(),
);

export const dismissCrewMember = createAction(
  '[Crew] Dismiss Crew Member',
  props<{ shipId: string; crewMemberId: string }>(),
);
export const dismissCrewMemberSuccess = createAction('[Crew] Dismiss Crew Member Success', props<{ crewMemberId: string }>());
export const dismissCrewMemberFailure = createAction(
  '[Crew] Dismiss Crew Member Failure',
  props<{ crewMemberId: string; error: unknown; refusal: string }>(),
);

export const loadHarborName = createAction('[Crew] Load Harbor Name');
export const loadHarborNameSuccess = createAction('[Crew] Load Harbor Name Success', props<{ harborName: string }>());
export const loadHarborNameFailure = createAction('[Crew] Load Harbor Name Failure', props<{ error: unknown }>());
