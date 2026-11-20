import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Action } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, ReplaySubject, throwError } from 'rxjs';

import { CrewEffects } from './crew.effects';
import * as CrewActions from './crew.actions';
import * as HarborActions from '../../harbor/store/harbor.actions';
import * as ShipActions from '../../ships/store/actions/ship.actions';
import { CrewService } from '../services/crew.service';
import { HarborNameService } from '../../../core/harbor-name.service';
import { Hire } from '../models/hire';
import { aRecruit } from '../../../../testing/fixtures';

const hire: Hire = {
  shipId: 'ship-1',
  name: 'Black Pearl',
  crewMember: { id: 'r1', name: 'Mittens', trait: 'Strong Paws', portrait: { fur: 'ginger', coat: 'tabby', eyes: 'green', accessory: 'none' } as const },
  hiringFee: '50.00',
  savings: '950.00',
  newRecruit: aRecruit({ id: 'r2' }),
};

describe('CrewEffects', () => {
  let actions$: ReplaySubject<Action>;
  let service: jasmine.SpyObj<CrewService>;
  let harborNameService: jasmine.SpyObj<HarborNameService>;
  let effects: CrewEffects;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    service = jasmine.createSpyObj<CrewService>('CrewService', ['hire', 'dismiss']);
    harborNameService = jasmine.createSpyObj<HarborNameService>('HarborNameService', ['getHarborName']);
    TestBed.configureTestingModule({
      providers: [
        CrewEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: CrewService, useValue: service },
        { provide: HarborNameService, useValue: harborNameService },
      ],
    });
    effects = TestBed.inject(CrewEffects);
  });

  it('hireRecruit$ emits Success with the Hire', () => {
    service.hire.and.returnValue(of(hire));
    const emitted: Action[] = [];
    effects.hireRecruit$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.hireRecruit({ shipId: 'ship-1', recruitId: 'r1' }));

    expect(service.hire).toHaveBeenCalledWith('ship-1', 'r1');
    expect(emitted).toEqual([CrewActions.hireRecruitSuccess({ hire })]);
  });

  it('hireRecruit$ emits Failure with the Problem Details detail and savingsShort from the title', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Savings do not cover', detail: 'The Savings do not cover the Hiring Fee of 50.00 $' },
    });
    service.hire.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.hireRecruit$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.hireRecruit({ shipId: 'ship-1', recruitId: 'r1' }));

    expect(emitted).toEqual([
      CrewActions.hireRecruitFailure({
        recruitId: 'r1',
        error,
        refusal: 'The Savings do not cover the Hiring Fee of 50.00 $',
        savingsShort: true,
      }),
    ]);
  });

  it('hireRecruit$ falls back to a general refusal without a detail', () => {
    const error = new HttpErrorResponse({ status: 0 });
    service.hire.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.hireRecruit$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.hireRecruit({ shipId: 'ship-1', recruitId: 'r1' }));

    expect(emitted).toEqual([
      CrewActions.hireRecruitFailure({ recruitId: 'r1', error, refusal: 'The Recruit could not be Hired', savingsShort: false }),
    ]);
  });

  it('a Hire, successful or refused, reloads the fleet, the Savings and the Recruit Pool', () => {
    const reload = [ShipActions.loadShips(), HarborActions.loadSavings(), HarborActions.loadRecruitPool()];
    const afterHire: Action[] = [];
    const afterRefusal: Action[] = [];
    effects.refreshAfterHire$.subscribe((a) => afterHire.push(a));
    effects.refreshAfterRefusedHire$.subscribe((a) => afterRefusal.push(a));

    actions$.next(CrewActions.hireRecruitSuccess({ hire }));
    actions$.next(CrewActions.hireRecruitFailure({ recruitId: 'r1', error: {}, refusal: 'x', savingsShort: false }));

    expect(afterHire).toEqual(reload);
    expect(afterRefusal).toEqual(reload);
  });

  it('dismissCrewMember$ emits Success', () => {
    service.dismiss.and.returnValue(of(undefined));
    const emitted: Action[] = [];
    effects.dismissCrewMember$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.dismissCrewMember({ shipId: 'ship-1', crewMemberId: 'c1' }));

    expect(service.dismiss).toHaveBeenCalledWith('ship-1', 'c1');
    expect(emitted).toEqual([CrewActions.dismissCrewMemberSuccess({ crewMemberId: 'c1' })]);
  });

  it('dismissCrewMember$ emits Failure with the detail', () => {
    const error = new HttpErrorResponse({ status: 409, error: { detail: 'Mittens is not aboard' } });
    service.dismiss.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.dismissCrewMember$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.dismissCrewMember({ shipId: 'ship-1', crewMemberId: 'c1' }));

    expect(emitted).toEqual([
      CrewActions.dismissCrewMemberFailure({ crewMemberId: 'c1', error, refusal: 'Mittens is not aboard' }),
    ]);
  });

  it('dismissCrewMember$ falls back to a general refusal', () => {
    const error = new HttpErrorResponse({ status: 0 });
    service.dismiss.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.dismissCrewMember$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.dismissCrewMember({ shipId: 'ship-1', crewMemberId: 'c1' }));

    expect(emitted).toEqual([
      CrewActions.dismissCrewMemberFailure({ crewMemberId: 'c1', error, refusal: 'The Crew Member could not be dismissed' }),
    ]);
  });

  it('a dismissal, successful or refused, reloads the fleet and the Recruit Pool but not the Savings', () => {
    const reload = [ShipActions.loadShips(), HarborActions.loadRecruitPool()];
    const emitted: Action[] = [];
    effects.refreshAfterDismiss$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.dismissCrewMemberSuccess({ crewMemberId: 'c1' }));
    actions$.next(CrewActions.dismissCrewMemberFailure({ crewMemberId: 'c1', error: {}, refusal: 'x' }));

    expect(emitted).toEqual([...reload, ...reload]);
  });

  it('loadHarborName$ emits the Harbor name', () => {
    harborNameService.getHarborName.and.returnValue(of('Port Royal'));
    const emitted: Action[] = [];
    effects.loadHarborName$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.loadHarborName());

    expect(emitted).toEqual([CrewActions.loadHarborNameSuccess({ harborName: 'Port Royal' })]);
  });

  it('loadHarborName$ emits Failure with the error', () => {
    const error = new HttpErrorResponse({ status: 500 });
    harborNameService.getHarborName.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.loadHarborName$.subscribe((a) => emitted.push(a));

    actions$.next(CrewActions.loadHarborName());

    expect(emitted).toEqual([CrewActions.loadHarborNameFailure({ error })]);
  });
});
