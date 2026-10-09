import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, concatMap, defer, filter, map, of, retry, switchMap, takeUntil } from 'rxjs';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { MarketService } from '../services/market.service';
import { IncomingShipsService } from '../services/incoming-ships.service';
import { FleetEventsService } from '../../ships/services/fleet-events.service';
import { FLEET_RECONNECT_DELAY_MS } from '../../ships/store/effects/ship.effects';

const PURCHASE_FAILED = 'The Market could not complete the purchase';

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);
  private savingsService = inject(SavingsService);
  private marketService = inject(MarketService);
  private incomingShipsService = inject(IncomingShipsService);
  private fleetEventsService = inject(FleetEventsService);

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

  /**
   * An arriving ship may be an Incoming Ship, so the list is read again on every Arrival and whenever the
   * fleet-events stream (re)opens, since an Arrival may have been missed meanwhile (as ShipEffects.watchFleet$).
   */
  watchArrivals$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.watchArrivals),
      switchMap(() =>
        defer(() => this.fleetEventsService.events()).pipe(
          retry({ delay: FLEET_RECONNECT_DELAY_MS }),
          filter((event) => event.type === 'ship-arrived' || event.type === 'connected'),
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
