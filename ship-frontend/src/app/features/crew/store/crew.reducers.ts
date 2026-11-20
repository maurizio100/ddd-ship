import { createReducer, on } from '@ngrx/store';
import { CrewState } from './crew.selectors';
import * as CrewActions from './crew.actions';

export const initialState: CrewState = {
  hiringRecruitIds: [],
  dismissingCrewMemberIds: [],
  dismissRefusal: null,
  hireRefusal: null,
  newInPoolRecruitIds: [],
  harborName: null,
  error: null,
};

export const crewReducers = createReducer(
  initialState,
  on(CrewActions.hireRecruit, (state, { recruitId }) => ({
    ...state,
    hiringRecruitIds: [...state.hiringRecruitIds, recruitId],
    hireRefusal: null,
  })),
  on(CrewActions.hireRecruitSuccess, (state, { hire }) => ({
    ...state,
    hiringRecruitIds: state.hiringRecruitIds.filter((id) => id !== hire.crewMember.id),
    newInPoolRecruitIds: [...state.newInPoolRecruitIds, hire.newRecruit.id],
  })),
  on(CrewActions.hireRecruitFailure, (state, { recruitId, refusal, savingsShort }) => ({
    ...state,
    hiringRecruitIds: state.hiringRecruitIds.filter((id) => id !== recruitId),
    hireRefusal: { recruitId, message: refusal, savingsShort },
  })),
  on(CrewActions.dismissCrewMember, (state, { crewMemberId }) => ({
    ...state,
    dismissingCrewMemberIds: [...state.dismissingCrewMemberIds, crewMemberId],
    dismissRefusal: null,
  })),
  on(CrewActions.dismissCrewMemberSuccess, (state, { crewMemberId }) => ({
    ...state,
    dismissingCrewMemberIds: state.dismissingCrewMemberIds.filter((id) => id !== crewMemberId),
  })),
  on(CrewActions.dismissCrewMemberFailure, (state, { crewMemberId, refusal }) => ({
    ...state,
    dismissingCrewMemberIds: state.dismissingCrewMemberIds.filter((id) => id !== crewMemberId),
    dismissRefusal: { crewMemberId, message: refusal },
  })),
  // A refusal belongs to the visit to one ship: entering the Crew screen starts clean.
  on(CrewActions.loadHarborName, (state) => ({ ...state, hireRefusal: null, dismissRefusal: null })),
  on(CrewActions.loadHarborNameSuccess, (state, { harborName }) => ({ ...state, harborName })),
  on(CrewActions.loadHarborNameFailure, (state, { error }) => ({ ...state, error })),
);
