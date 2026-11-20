import { crewReducers, initialState } from './crew.reducers';
import * as CrewActions from './crew.actions';
import { aRecruit } from '../../../../testing/fixtures';
import { Hire } from '../models/hire';

const hire: Hire = {
  shipId: 'ship-1',
  name: 'Black Pearl',
  crewMember: { id: 'r1', name: 'Mittens', trait: 'Strong Paws' },
  hiringFee: '50.00',
  savings: '950.00',
  newRecruit: aRecruit({ id: 'r2', name: 'Patch' }),
};

describe('crew reducers', () => {
  it('hireRecruit marks the Recruit as in flight and clears the refusal', () => {
    const state = { ...initialState, hireRefusal: { recruitId: 'r0', message: 'no', savingsShort: false } };

    const next = crewReducers(state, CrewActions.hireRecruit({ shipId: 'ship-1', recruitId: 'r1' }));

    expect(next.hiringRecruitIds).toEqual(['r1']);
    expect(next.hireRefusal).toBeNull();
  });

  it('hireRecruitSuccess ends the flight and notes the new Recruit as new in the pool', () => {
    const state = { ...initialState, hiringRecruitIds: ['r1'], newInPoolRecruitIds: ['r9'] };

    const next = crewReducers(state, CrewActions.hireRecruitSuccess({ hire }));

    expect(next.hiringRecruitIds).toEqual([]);
    expect(next.newInPoolRecruitIds).toEqual(['r9', 'r2']);
  });

  it('hireRecruitFailure ends the flight and sets the refusal', () => {
    const state = { ...initialState, hiringRecruitIds: ['r1'] };

    const next = crewReducers(
      state,
      CrewActions.hireRecruitFailure({ recruitId: 'r1', error: {}, refusal: 'Crew full', savingsShort: true }),
    );

    expect(next.hiringRecruitIds).toEqual([]);
    expect(next.hireRefusal).toEqual({ recruitId: 'r1', message: 'Crew full', savingsShort: true });
  });

  it('loadHarborName clears a refusal left by another ship', () => {
    const state = { ...initialState, hireRefusal: { recruitId: 'r0', message: 'no', savingsShort: false } };

    expect(crewReducers(state, CrewActions.loadHarborName()).hireRefusal).toBeNull();
  });

  it('loadHarborNameSuccess keeps the Harbor name', () => {
    expect(crewReducers(initialState, CrewActions.loadHarborNameSuccess({ harborName: 'Port Royal' })).harborName).toBe(
      'Port Royal',
    );
  });

  it('dismissCrewMember marks the Crew Member in flight and clears the refusal', () => {
    const state = { ...initialState, dismissRefusal: { crewMemberId: 'c0', message: 'no' } };

    const next = crewReducers(state, CrewActions.dismissCrewMember({ shipId: 's', crewMemberId: 'c1' }));

    expect(next.dismissingCrewMemberIds).toEqual(['c1']);
    expect(next.dismissRefusal).toBeNull();
  });

  it('dismissCrewMemberSuccess clears the flight and leaves the new-in-pool list alone', () => {
    const state = { ...initialState, dismissingCrewMemberIds: ['c1'], newInPoolRecruitIds: ['r9'] };

    const next = crewReducers(state, CrewActions.dismissCrewMemberSuccess({ crewMemberId: 'c1' }));

    expect(next.dismissingCrewMemberIds).toEqual([]);
    expect(next.newInPoolRecruitIds).toEqual(['r9']);
  });

  it('dismissCrewMemberFailure clears the flight and keeps the refusal', () => {
    const state = { ...initialState, dismissingCrewMemberIds: ['c1'] };

    const next = crewReducers(state, CrewActions.dismissCrewMemberFailure({ crewMemberId: 'c1', error: {}, refusal: 'no' }));

    expect(next.dismissingCrewMemberIds).toEqual([]);
    expect(next.dismissRefusal).toEqual({ crewMemberId: 'c1', message: 'no' });
  });

  it('loadHarborName clears a dismiss refusal', () => {
    const state = { ...initialState, dismissRefusal: { crewMemberId: 'c1', message: 'no' } };

    expect(crewReducers(state, CrewActions.loadHarborName()).dismissRefusal).toBeNull();
  });
});
