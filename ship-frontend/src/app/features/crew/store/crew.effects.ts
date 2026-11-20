import { inject, Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, concatMap, map, of, switchMap } from 'rxjs';
import * as CrewActions from './crew.actions';
import * as HarborActions from '../../harbor/store/harbor.actions';
import * as ShipActions from '../../ships/store/actions/ship.actions';
import { CrewService } from '../services/crew.service';
import { HarborNameService } from '../../../core/harbor-name.service';

const HIRE_FAILED = 'The Recruit could not be Hired';
const SAVINGS_SHORT_TITLE = 'Savings do not cover';

@Injectable()
export class CrewEffects {
  private actions$ = inject(Actions);
  private crewService = inject(CrewService);
  private harborNameService = inject(HarborNameService);

  /** concatMap: a Hire moves money, so a later one never cancels it. */
  hireRecruit$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CrewActions.hireRecruit),
      concatMap(({ shipId, recruitId }) =>
        this.crewService.hire(shipId, recruitId).pipe(
          map((hire) => CrewActions.hireRecruitSuccess({ hire })),
          catchError((error: HttpErrorResponse) =>
            of(
              CrewActions.hireRecruitFailure({
                recruitId,
                error,
                refusal: error.error?.detail ?? HIRE_FAILED,
                savingsShort: error.error?.title === SAVINGS_SHORT_TITLE,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  /** The backend owns the money and the Crew: after a Hire the fleet, the Savings and the pool are read back. */
  refreshAfterHire$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CrewActions.hireRecruitSuccess),
      switchMap(() => [ShipActions.loadShips(), HarborActions.loadSavings(), HarborActions.loadRecruitPool()]),
    ),
  );

  /** A refused Hire may mean the Crew, the Savings or the pool changed elsewhere: read them back too. */
  refreshAfterRefusedHire$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CrewActions.hireRecruitFailure),
      switchMap(() => [ShipActions.loadShips(), HarborActions.loadSavings(), HarborActions.loadRecruitPool()]),
    ),
  );

  loadHarborName$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CrewActions.loadHarborName),
      switchMap(() =>
        this.harborNameService.getHarborName().pipe(
          map((harborName) => CrewActions.loadHarborNameSuccess({ harborName })),
          catchError((error) => of(CrewActions.loadHarborNameFailure({ error }))),
        ),
      ),
    ),
  );
}
