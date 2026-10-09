import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';

@Injectable()
export class HarborEffects {
  private actions$ = inject(Actions);
  private stockService = inject(StockService);

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
}
