import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, concatMap, map, of, switchMap, takeUntil, tap, timer } from 'rxjs';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { MarketService } from '../services/market.service';
import { IncomingShipsService } from '../services/incoming-ships.service';
import { RecruitPoolService } from '../services/recruit-pool.service';
import { ImprisonedShipsService } from '../services/imprisoned-ships.service';
import * as ShipActions from '../../ships/store/actions/ship.actions';

const PURCHASE_FAILED = 'The Market could not complete the purchase';
const UNLOAD_FAILED = 'The Incoming Ship could not be unloaded';
const REFUSE_FAILED = 'The Incoming Ship could not be refused';
const FINE_FAILED = 'The Fine could not be paid';
/** How long the notice of a paid Fine stays on the fleet before it dismisses itself. */
export const FINE_NOTICE_MS = 8000;

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);
  private savingsService = inject(SavingsService);
  private marketService = inject(MarketService);
  private incomingShipsService = inject(IncomingShipsService);
  private recruitPoolService = inject(RecruitPoolService);
  private imprisonedShipsService = inject(ImprisonedShipsService);
  private router = inject(Router);

  loadStock$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.loadStock),
      switchMap(() =>
        this.stockService.getStock().pipe(
          map((stock) => HarborActions.loadStockSuccess({ stock })),
          catchError((error) => of(HarborActions.loadStockFailure({ error }))),
        ),
      ),
    ),
  );

  loadSavings$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.loadSavings),
      switchMap(() =>
        this.savingsService.getSavings().pipe(
          map((savings) => HarborActions.loadSavingsSuccess({ savings })),
          catchError((error) => of(HarborActions.loadSavingsFailure({ error }))),
        ),
      ),
    ),
  );

  /** concatMap: a purchase moves money, so a later one never cancels it. */
  buyCargo$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.buyCargo),
      concatMap(({ cargoId, quantity }) =>
        this.marketService.buyCargo({ cargoId, quantity }).pipe(
          map(() => HarborActions.buyCargoSuccess()),
          catchError((error: HttpErrorResponse) =>
            of(HarborActions.buyCargoFailure({ error, refusal: error.error?.detail ?? PURCHASE_FAILED })),
          ),
        ),
      ),
    ),
  );

  /** A newer load wins: a refetch after a pushed arrival is never dropped behind a running one. */
  loadIncomingShips$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.loadIncomingShips),
      switchMap(() =>
        this.incomingShipsService.getIncomingShips().pipe(
          map((incomingShips) => HarborActions.loadIncomingShipsSuccess({ incomingShips })),
          catchError((error) => of(HarborActions.loadIncomingShipsFailure({ error }))),
        ),
      ),
    ),
  );

  /** A newer load wins, as for the Incoming Ships. */
  loadRecruitPool$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.loadRecruitPool),
      switchMap(() =>
        this.recruitPoolService.getRecruitPool().pipe(
          map((recruitPool) => HarborActions.loadRecruitPoolSuccess({ recruitPool })),
          catchError((error) => of(HarborActions.loadRecruitPoolFailure({ error }))),
        ),
      ),
    ),
  );

  /** The harbor page rides on the ships store's fleet-events stream, so there is one connection only. */
  watchFleet$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.watchArrivals, HarborActions.stopWatchingArrivals),
      map((action) =>
        action.type === HarborActions.watchArrivals.type ? ShipActions.watchFleet() : ShipActions.stopWatchingFleet(),
      ),
    ),
  );

  /**
   * An arriving ship may be an Incoming Ship, so the list is read again on every Arrival and whenever the
   * stream (re)connects, which the ships store answers with a fleet load, since an Arrival may have been missed.
   * An Arrival may also put the Earnings of a ship coming home into the Savings, so the Savings are read back too.
   */
  watchArrivals$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.watchArrivals),
      switchMap(() =>
        this.actions$.pipe(
          ofType(ShipActions.shipArrived, ShipActions.loadShips),
          switchMap(() => [HarborActions.loadIncomingShips(), HarborActions.loadSavings()]),
          takeUntil(this.actions$.pipe(ofType(HarborActions.stopWatchingArrivals))),
        ),
      ),
    ),
  );

  /** The backend owns the money: after a purchase the Stock and the Savings are read back, never computed here. */
  refreshAfterPurchase$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.buyCargoSuccess),
      switchMap(() => [HarborActions.loadStock(), HarborActions.loadSavings()]),
    ),
  );

  /** concatMap: an unloading moves money, so a later one never cancels it. */
  unloadIncomingShip$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.unloadIncomingShip),
      concatMap(({ shipId }) =>
        this.incomingShipsService.unload(shipId).pipe(
          map(() => HarborActions.unloadIncomingShipSuccess({ shipId })),
          catchError((error: HttpErrorResponse) =>
            of(HarborActions.unloadIncomingShipFailure({ shipId, error, refusal: error.error?.detail ?? UNLOAD_FAILED })),
          ),
        ),
      ),
    ),
  );

  /** The backend owns the money: after an unloading the Incoming Ships, the Stock and the Savings are read back. */
  refreshAfterUnloading$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.unloadIncomingShipSuccess),
      switchMap(() => [HarborActions.loadIncomingShips(), HarborActions.loadStock(), HarborActions.loadSavings()]),
    ),
  );

  /** A refused unloading may mean another User unloaded the ship first: read the Incoming Ships back. */
  refreshIncomingShipsAfterRefusedUnloading$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.unloadIncomingShipFailure),
      map(() => HarborActions.loadIncomingShips()),
    ),
  );

  /** concatMap: a refusal sends a ship to sea, so a later one never cancels it. */
  refuseIncomingShip$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.refuseIncomingShip),
      concatMap(({ shipId }) =>
        this.incomingShipsService.refuse(shipId).pipe(
          map(() => HarborActions.refuseIncomingShipSuccess({ shipId })),
          catchError((error: HttpErrorResponse) =>
            of(HarborActions.refuseIncomingShipFailure({ shipId, error, refusal: error.error?.detail ?? REFUSE_FAILED })),
          ),
        ),
      ),
    ),
  );

  /** A refusal moves no money, so only the Incoming Ships are read back, whether it succeeded or failed. */
  refreshAfterRefusal$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.refuseIncomingShipSuccess, HarborActions.refuseIncomingShipFailure),
      map(() => HarborActions.loadIncomingShips()),
    ),
  );

  /** A newer load wins, as for the Incoming Ships. */
  loadImprisonedShips$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.loadImprisonedShips),
      switchMap(() =>
        this.imprisonedShipsService.getImprisonedShips().pipe(
          map((imprisonedShips) => HarborActions.loadImprisonedShipsSuccess({ imprisonedShips })),
          catchError((error) => of(HarborActions.loadImprisonedShipsFailure({ error }))),
        ),
      ),
    ),
  );

  /** concatMap: a Fine payment moves money, so a later one never cancels it. */
  payFine$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.payFine),
      concatMap(({ shipId }) =>
        this.imprisonedShipsService.payFine(shipId).pipe(
          map((payment) => HarborActions.payFineSuccess({ payment })),
          catchError((error: HttpErrorResponse) =>
            of(HarborActions.payFineFailure({ shipId, error, refusal: error.error?.detail ?? FINE_FAILED })),
          ),
        ),
      ),
    ),
  );

  /** The backend owns the money: after a paid Fine the Imprisoned Ships, the Savings and the fleet are read back. */
  refreshAfterFinePaid$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.payFineSuccess),
      switchMap(() => [HarborActions.loadImprisonedShips(), HarborActions.loadSavings(), ShipActions.loadShips()]),
    ),
  );

  /** The freed ship is shown on the fleet, where it can get a new Shipping. */
  showFleetAfterFinePaid$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(HarborActions.payFineSuccess),
        tap(() => this.router.navigate(['/ships'])),
      ),
    { dispatch: false },
  );

  /** The Fine notice dismisses itself after a while; a newer payment restarts the wait. */
  dismissFineNotice$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.payFineSuccess),
      switchMap(() => timer(FINE_NOTICE_MS).pipe(map(() => HarborActions.dismissFineNotice()))),
    ),
  );

  /** A refused payment may mean the ship was freed elsewhere or the Savings changed: read both back. */
  refreshAfterRefusedFine$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.payFineFailure),
      switchMap(() => [HarborActions.loadImprisonedShips(), HarborActions.loadSavings()]),
    ),
  );

  /** Every fleet load, also the one after a pushed imprisonment, reads the Imprisoned Ships back, so the app-bar pill follows. */
  refreshImprisonedShipsWithFleet$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipActions.loadShipsSuccess),
      map(() => HarborActions.loadImprisonedShips()),
    ),
  );
}
