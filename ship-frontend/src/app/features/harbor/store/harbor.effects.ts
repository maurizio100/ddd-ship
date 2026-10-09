import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, concatMap, map, Observable, of, switchMap } from 'rxjs';
import { Action } from '@ngrx/store';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { MarketService } from '../services/market.service';

const PURCHASE_FAILED = 'The Market could not complete the purchase';

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);
  private savingsService = inject(SavingsService);
  private marketService = inject(MarketService);

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

  loadIncomingShips$ = createEffect((): Observable<Action> => of());

  watchArrivals$ = createEffect((): Observable<Action> => of());

  /** The backend owns the money: after a purchase the Stock and the Savings are read back, never computed here. */
  refreshAfterPurchase$ = createEffect(() =>
    this.actions$.pipe(
      ofType(HarborActions.buyCargoSuccess),
      switchMap(() => [HarborActions.loadStock(), HarborActions.loadSavings()]),
    ),
  );
}
