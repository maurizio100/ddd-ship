import { createFeatureSelector, createSelector } from '@ngrx/store';

export interface HireRefusal {
  recruitId: string;
  message: string;
  savingsShort: boolean;
}

export interface CrewState {
  hiringRecruitIds: string[];
  hireRefusal: HireRefusal | null;
  newInPoolRecruitIds: string[];
  harborName: string | null;
  loading: boolean;
  error: unknown;
}

export const selectCrewState = createFeatureSelector<CrewState>('crew');
export const selectHiringRecruitIds = createSelector(selectCrewState, (state) => state.hiringRecruitIds);
export const selectHireRefusal = createSelector(selectCrewState, (state) => state.hireRefusal);
export const selectNewInPoolRecruitIds = createSelector(selectCrewState, (state) => state.newInPoolRecruitIds);
export const selectHarborName = createSelector(selectCrewState, (state) => state.harborName);
