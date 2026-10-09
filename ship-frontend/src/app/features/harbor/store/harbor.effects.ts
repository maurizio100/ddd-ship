import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);
  private savingsService = inject(SavingsService);

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
}
