import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, concatMap, map, of, switchMap, takeUntil } from 'rxjs';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { MarketService } from '../services/market.service';
import { IncomingShipsService } from '../services/incoming-ships.service';
import * as ShipActions from '../../ships/store/actions/ship.actions';

const PURCHASE_FAILED = 'The Market could not complete the purchase';

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);
  private savingsService = inject(SavingsService);
  private marketService = inject(MarketService);
  private incomingShipsService = inject(IncomingShipsService);

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
   */
  watchArrivals$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.watchArrivals),
      switchMap(() =>
        this.actions$.pipe(
          ofType(ShipActions.shipArrived, ShipActions.loadShips),
          map(() => HarborActions.loadIncomingShips()),
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
}
