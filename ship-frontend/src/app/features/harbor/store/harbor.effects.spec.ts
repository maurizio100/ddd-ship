import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Action } from '@ngrx/store';
import { Observable, of, ReplaySubject, throwError } from 'rxjs';

import { HarborEffects } from './harbor.effects';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { aSavings, aStockedCargo } from '../../../../testing/fixtures';

describe('HarborEffects', () => {
  let actions$: ReplaySubject<Action>;
  let effects: HarborEffects;
  let stockService: jasmine.SpyObj<StockService>;
  let savingsService: jasmine.SpyObj<SavingsService>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    stockService = jasmine.createSpyObj<StockService>('StockService', ['getStock']);
    savingsService = jasmine.createSpyObj<SavingsService>('SavingsService', ['getSavings']);
    TestBed.configureTestingModule({
      providers: [
        HarborEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: StockService, useValue: stockService },
        { provide: SavingsService, useValue: savingsService },
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

  it('loadSavings$ emits loadSavingsSuccess with the service result', () => {
    const savings = aSavings({ amount: '640.50' });
    savingsService.getSavings.and.returnValue(of(savings));
    const emitted: Action[] = [];
    effects.loadSavings$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadSavings());

    expect(emitted).toEqual([HarborActions.loadSavingsSuccess({ savings })]);
  });

  it('loadSavings$ emits loadSavingsFailure when the service fails', () => {
    const error = new Error('boom');
    savingsService.getSavings.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.loadSavings$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadSavings());

    expect(emitted).toEqual([HarborActions.loadSavingsFailure({ error })]);
  });
});
