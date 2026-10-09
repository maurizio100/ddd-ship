import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Action } from '@ngrx/store';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of, ReplaySubject, throwError } from 'rxjs';

import { HarborEffects } from './harbor.effects';
import * as HarborActions from './harbor.actions';
import { StockService } from '../services/stock.service';
import { SavingsService } from '../services/savings.service';
import { MarketService } from '../services/market.service';
import { IncomingShipsService } from '../services/incoming-ships.service';
import * as ShipActions from '../../ships/store/actions/ship.actions';
import { aSavings, aStockedCargo, anIncomingShipListing } from '../../../../testing/fixtures';

describe('HarborEffects', () => {
  let actions$: ReplaySubject<Action>;
  let effects: HarborEffects;
  let stockService: jasmine.SpyObj<StockService>;
  let savingsService: jasmine.SpyObj<SavingsService>;
  let marketService: jasmine.SpyObj<MarketService>;
  let incomingShipsService: jasmine.SpyObj<IncomingShipsService>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    stockService = jasmine.createSpyObj<StockService>('StockService', ['getStock']);
    savingsService = jasmine.createSpyObj<SavingsService>('SavingsService', ['getSavings']);
    marketService = jasmine.createSpyObj<MarketService>('MarketService', ['buyCargo']);
    incomingShipsService = jasmine.createSpyObj<IncomingShipsService>('IncomingShipsService', ['getIncomingShips', 'unload']);
    TestBed.configureTestingModule({
      providers: [
        HarborEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: StockService, useValue: stockService },
        { provide: SavingsService, useValue: savingsService },
        { provide: MarketService, useValue: marketService },
        { provide: IncomingShipsService, useValue: incomingShipsService },
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

  const cargoId = 'c0a8f3a2-0000-4000-8000-000000000001';

  it('buyCargo$ buys at the Market and emits buyCargoSuccess', () => {
    marketService.buyCargo.and.returnValue(of(undefined));
    const emitted: Action[] = [];
    effects.buyCargo$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.buyCargo({ cargoId, quantity: 2 }));

    expect(marketService.buyCargo).toHaveBeenCalledWith({ cargoId, quantity: 2 });
    expect(emitted).toEqual([HarborActions.buyCargoSuccess()]);
  });

  it('buyCargo$ emits buyCargoFailure with the refusal from the Problem Details', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Savings do not cover', detail: 'The Savings do not cover 100.00 $' },
    });
    marketService.buyCargo.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.buyCargo$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.buyCargo({ cargoId, quantity: 2 }));

    expect(emitted).toEqual([HarborActions.buyCargoFailure({ error, refusal: 'The Savings do not cover 100.00 $' })]);
  });

  it('buyCargo$ falls back to a general refusal when the error has no detail', () => {
    const error = new HttpErrorResponse({ status: 0 });
    marketService.buyCargo.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.buyCargo$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.buyCargo({ cargoId, quantity: 2 }));

    expect(emitted).toEqual([
      HarborActions.buyCargoFailure({ error, refusal: 'The Market could not complete the purchase' }),
    ]);
  });

  it('refreshAfterPurchase$ reloads the Stock and the Savings after a purchase', () => {
    const emitted: Action[] = [];
    effects.refreshAfterPurchase$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.buyCargoSuccess());

    expect(emitted).toEqual([HarborActions.loadStock(), HarborActions.loadSavings()]);
  });

  it('loadIncomingShips$ emits loadIncomingShipsSuccess with the service result', () => {
    const incomingShips = [anIncomingShipListing()];
    incomingShipsService.getIncomingShips.and.returnValue(of(incomingShips));
    const emitted: Action[] = [];
    effects.loadIncomingShips$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadIncomingShips());

    expect(emitted).toEqual([HarborActions.loadIncomingShipsSuccess({ incomingShips })]);
  });

  it('loadIncomingShips$ emits loadIncomingShipsFailure when the service fails', () => {
    const error = new Error('boom');
    incomingShipsService.getIncomingShips.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.loadIncomingShips$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadIncomingShips());

    expect(emitted).toEqual([HarborActions.loadIncomingShipsFailure({ error })]);
  });

  it('watchFleet$ starts and stops the ships store fleet-events stream with the arrivals watch', () => {
    const emitted: Action[] = [];
    effects.watchFleet$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.watchArrivals());
    actions$.next(HarborActions.stopWatchingArrivals());

    expect(emitted).toEqual([ShipActions.watchFleet(), ShipActions.stopWatchingFleet()]);
  });

  it('watchArrivals$ reloads the Incoming Ships when a ship arrives and when the stream (re)connects, until stopped', () => {
    const arrived = ShipActions.shipArrived({ shipId: 'id', shipName: 'Salty Whisker', originHarbor: 'Tortuga' });
    const emitted: Action[] = [];
    effects.watchArrivals$.subscribe((action) => emitted.push(action));
    actions$.next(HarborActions.watchArrivals());

    actions$.next(ShipActions.loadShips());
    actions$.next(arrived);
    actions$.next(ShipActions.shipLeft({ shipId: 'id', shipName: 'Black Pearl', destinationHarbor: 'Nassau' }));

    expect(emitted).toEqual([HarborActions.loadIncomingShips(), HarborActions.loadIncomingShips()]);

    actions$.next(HarborActions.stopWatchingArrivals());
    actions$.next(arrived);

    expect(emitted.length).toBe(2);
  });

  it('unloadIncomingShip$ unloads the Incoming Ship and emits unloadIncomingShipSuccess', () => {
    incomingShipsService.unload.and.returnValue(of(undefined));
    const emitted: Action[] = [];
    effects.unloadIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(incomingShipsService.unload).toHaveBeenCalledWith('b1a2c3d4-0000-4000-8000-000000000001');
    expect(emitted).toEqual([HarborActions.unloadIncomingShipSuccess()]);
  });

  it('unloadIncomingShip$ emits unloadIncomingShipFailure with the refusal from the Problem Details', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Savings do not cover', detail: 'The Savings do not cover the Delivery Price of 115.00 $' },
    });
    incomingShipsService.unload.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.unloadIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(emitted).toEqual([
      HarborActions.unloadIncomingShipFailure({
        error,
        refusal: 'The Savings do not cover the Delivery Price of 115.00 $',
      }),
    ]);
  });

  it('unloadIncomingShip$ falls back to a general refusal when the error has no detail', () => {
    const error = new HttpErrorResponse({ status: 0 });
    incomingShipsService.unload.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.unloadIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(emitted).toEqual([
      HarborActions.unloadIncomingShipFailure({ error, refusal: 'The Incoming Ship could not be unloaded' }),
    ]);
  });

  it('refreshAfterUnloading$ reloads the Incoming Ships, the Stock and the Savings after an unloading', () => {
    const emitted: Action[] = [];
    effects.refreshAfterUnloading$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShipSuccess());

    expect(emitted).toEqual([HarborActions.loadIncomingShips(), HarborActions.loadStock(), HarborActions.loadSavings()]);
  });
});
