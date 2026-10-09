import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Action } from '@ngrx/store';
import { Observable, of, ReplaySubject, throwError } from 'rxjs';

import { HarborEffects } from './harbor.effects';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { aStockedCargo } from '../../../../testing/fixtures';

describe('HarborEffects', () => {
  let actions$: ReplaySubject<Action>;
  let effects: HarborEffects;
  let stockService: jasmine.SpyObj<StockService>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    stockService = jasmine.createSpyObj<StockService>('StockService', ['getStock']);
    TestBed.configureTestingModule({
      providers: [
        HarborEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: StockService, useValue: stockService },
      ],
    });
    effects = TestBed.inject(HarborEffects);
  });

  it('loadStock$ emits loadStockSuccess with the service result', () => {
    const stock = [aStockedCargo()];
    stockService.getStock.and.returnValue(of(stock));
    const emitted: Action[] = [];
    effects.loadStock$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadStock());

    expect(emitted).toEqual([HarborActions.loadStockSuccess({ stock })]);
  });

  it('loadStock$ emits loadStockFailure when the service fails', () => {
    const error = new Error('boom');
    stockService.getStock.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.loadStock$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadStock());

    expect(emitted).toEqual([HarborActions.loadStockFailure({ error })]);
  });
});
