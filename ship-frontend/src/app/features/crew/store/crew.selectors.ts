import { createFeatureSelector, createSelector } from '@ngrx/store';

export interface HireRefusal {
  recruitId: string;
  message: string;
  savingsShort: boolean;
}

export interface DismissRefusal {
  crewMemberId: string;
  message: string;
}

export interface CrewState {
  hiringRecruitIds: string[];
  dismissingCrewMemberIds: string[];
  dismissRefusal: DismissRefusal | null;
  hireRefusal: HireRefusal | null;
  newInPoolRecruitIds: string[];
  harborName: string | null;
  error: unknown;
}

export const selectCrewState = createFeatureSelector<CrewState>('crew');
export const selectHiringRecruitIds = createSelector(selectCrewState, (state) => state.hiringRecruitIds);
export const selectDismissingCrewMemberIds = createSelector(selectCrewState, (state) => state.dismissingCrewMemberIds);
export const selectDismissRefusal = createSelector(selectCrewState, (state) => state.dismissRefusal);
export const selectHireRefusal = createSelector(selectCrewState, (state) => state.hireRefusal);
export const selectNewInPoolRecruitIds = createSelector(selectCrewState, (state) => state.newInPoolRecruitIds);
export const selectHarborName = createSelector(selectCrewState, (state) => state.harborName);
export const selectHarborNameError = createSelector(selectCrewState, (state) => state.error);
