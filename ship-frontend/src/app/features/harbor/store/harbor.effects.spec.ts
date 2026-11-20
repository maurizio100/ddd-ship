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
import { RecruitPoolService } from '../services/recruit-pool.service';
import { IncomingShipsService } from '../services/incoming-ships.service';
import { ImprisonedShipsService } from '../services/imprisoned-ships.service';
import { Router } from '@angular/router';
import { fakeAsync, tick } from '@angular/core/testing';
import * as ShipActions from '../../ships/store/actions/ship.actions';
import { aFinePayment, aSavings, aStockedCargo, anImprisonedShip, anIncomingShipListing } from '../../../../testing/fixtures';

describe('HarborEffects', () => {
  let actions$: ReplaySubject<Action>;
  let effects: HarborEffects;
  let stockService: jasmine.SpyObj<StockService>;
  let savingsService: jasmine.SpyObj<SavingsService>;
  let marketService: jasmine.SpyObj<MarketService>;
  let recruitPoolService: jasmine.SpyObj<RecruitPoolService>;
  let incomingShipsService: jasmine.SpyObj<IncomingShipsService>;
  let imprisonedShipsService: jasmine.SpyObj<ImprisonedShipsService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    stockService = jasmine.createSpyObj<StockService>('StockService', ['getStock']);
    savingsService = jasmine.createSpyObj<SavingsService>('SavingsService', ['getSavings']);
    marketService = jasmine.createSpyObj<MarketService>('MarketService', ['buyCargo']);
    recruitPoolService = jasmine.createSpyObj<RecruitPoolService>('RecruitPoolService', ['getRecruitPool']);
    incomingShipsService = jasmine.createSpyObj<IncomingShipsService>('IncomingShipsService', ['getIncomingShips', 'unload', 'refuse']);
    imprisonedShipsService = jasmine.createSpyObj<ImprisonedShipsService>('ImprisonedShipsService', ['getImprisonedShips', 'payFine']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));
    TestBed.configureTestingModule({
      providers: [
        HarborEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: StockService, useValue: stockService },
        { provide: SavingsService, useValue: savingsService },
        { provide: MarketService, useValue: marketService },
        { provide: RecruitPoolService, useValue: recruitPoolService },
        { provide: IncomingShipsService, useValue: incomingShipsService },
        { provide: ImprisonedShipsService, useValue: imprisonedShipsService },
        { provide: Router, useValue: router },
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

  it('loadRecruitPool$ emits loadRecruitPoolSuccess with the service result', () => {
    const recruitPool = [{ id: 'r1', name: 'Mittens', trait: 'Sure-pawed' }];
    recruitPoolService.getRecruitPool.and.returnValue(of(recruitPool));
    const emitted: Action[] = [];
    effects.loadRecruitPool$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadRecruitPool());

    expect(emitted).toEqual([HarborActions.loadRecruitPoolSuccess({ recruitPool })]);
  });

  it('loadRecruitPool$ emits loadRecruitPoolFailure when the service fails', () => {
    const error = new Error('boom');
    recruitPoolService.getRecruitPool.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.loadRecruitPool$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.loadRecruitPool());

    expect(emitted).toEqual([HarborActions.loadRecruitPoolFailure({ error })]);
  });

  it('watchFleet$ starts and stops the ships store fleet-events stream with the arrivals watch', () => {
    const emitted: Action[] = [];
    effects.watchFleet$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.watchArrivals());
    actions$.next(HarborActions.stopWatchingArrivals());

    expect(emitted).toEqual([ShipActions.watchFleet(), ShipActions.stopWatchingFleet()]);
  });

  it('watchArrivals$ reloads the Incoming Ships and the Savings when a ship arrives and when the stream (re)connects, until stopped', () => {
    const arrived = ShipActions.shipArrived({ shipId: 'id', shipName: 'Salty Whisker', originHarbor: 'Tortuga', imprisoned: false });
    const emitted: Action[] = [];
    effects.watchArrivals$.subscribe((action) => emitted.push(action));
    actions$.next(HarborActions.watchArrivals());

    actions$.next(ShipActions.loadShips());
    actions$.next(arrived);
    actions$.next(ShipActions.shipLeft({ shipId: 'id', shipName: 'Black Pearl', destinationHarbor: 'Nassau' }));

    expect(emitted).toEqual([
      HarborActions.loadIncomingShips(),
      HarborActions.loadSavings(),
      HarborActions.loadIncomingShips(),
      HarborActions.loadSavings(),
    ]);

    actions$.next(HarborActions.stopWatchingArrivals());
    actions$.next(arrived);

    expect(emitted.length).toBe(4);
  });

  it('unloadIncomingShip$ unloads the Incoming Ship and emits unloadIncomingShipSuccess', () => {
    incomingShipsService.unload.and.returnValue(of(undefined));
    const emitted: Action[] = [];
    effects.unloadIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(incomingShipsService.unload).toHaveBeenCalledWith('b1a2c3d4-0000-4000-8000-000000000001');
    expect(emitted).toEqual([HarborActions.unloadIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' })]);
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
        shipId: 'b1a2c3d4-0000-4000-8000-000000000001',
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
      HarborActions.unloadIncomingShipFailure({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001', error, refusal: 'The Incoming Ship could not be unloaded' }),
    ]);
  });

  it('refreshAfterUnloading$ reloads the Incoming Ships, the Stock and the Savings after an unloading', () => {
    const emitted: Action[] = [];
    effects.refreshAfterUnloading$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(emitted).toEqual([HarborActions.loadIncomingShips(), HarborActions.loadStock(), HarborActions.loadSavings()]);
  });

  it('refreshIncomingShipsAfterRefusedUnloading$ reloads the Incoming Ships after a refused unloading', () => {
    const emitted: Action[] = [];
    effects.refreshIncomingShipsAfterRefusedUnloading$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.unloadIncomingShipFailure({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001', error: 'boom', refusal: 'nope' }));

    expect(emitted).toEqual([HarborActions.loadIncomingShips()]);
  });

  it('refuseIncomingShip$ refuses the Incoming Ship and emits refuseIncomingShipSuccess', () => {
    incomingShipsService.refuse.and.returnValue(of(undefined));
    const emitted: Action[] = [];
    effects.refuseIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.refuseIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(incomingShipsService.refuse).toHaveBeenCalledWith('b1a2c3d4-0000-4000-8000-000000000001');
    expect(emitted).toEqual([HarborActions.refuseIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' })]);
  });

  it('refuseIncomingShip$ emits refuseIncomingShipFailure with the detail from the Problem Details', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Not an Incoming Ship', detail: 'Salty Whisker is not an Incoming Ship anymore' },
    });
    incomingShipsService.refuse.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.refuseIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.refuseIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(emitted).toEqual([
      HarborActions.refuseIncomingShipFailure({
        shipId: 'b1a2c3d4-0000-4000-8000-000000000001',
        error,
        refusal: 'Salty Whisker is not an Incoming Ship anymore',
      }),
    ]);
  });

  it('refuseIncomingShip$ falls back to a general failure text when the error has no detail', () => {
    const error = new HttpErrorResponse({ status: 0 });
    incomingShipsService.refuse.and.returnValue(throwError(() => error));
    const emitted: Action[] = [];
    effects.refuseIncomingShip$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.refuseIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));

    expect(emitted).toEqual([
      HarborActions.refuseIncomingShipFailure({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001', error, refusal: 'The Incoming Ship could not be refused' }),
    ]);
  });

  it('refreshAfterRefusal$ reloads only the Incoming Ships after a refusal succeeds or fails', () => {
    const emitted: Action[] = [];
    effects.refreshAfterRefusal$.subscribe((action) => emitted.push(action));

    actions$.next(HarborActions.refuseIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));
    actions$.next(HarborActions.refuseIncomingShipFailure({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001', error: 'boom', refusal: 'nope' }));

    expect(emitted).toEqual([HarborActions.loadIncomingShips(), HarborActions.loadIncomingShips()]);
  });

  describe('the Fine', () => {
    const shipId = '5f1d7c2e-0000-4000-8000-000000000002';

    it('loadImprisonedShips$ emits loadImprisonedShipsSuccess with the service result', () => {
      imprisonedShipsService.getImprisonedShips.and.returnValue(of([anImprisonedShip()]));
      const emitted: Action[] = [];
      effects.loadImprisonedShips$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.loadImprisonedShips());

      expect(emitted).toEqual([HarborActions.loadImprisonedShipsSuccess({ imprisonedShips: [anImprisonedShip()] })]);
    });

    it('loadImprisonedShips$ emits loadImprisonedShipsFailure when the service fails', () => {
      const error = new Error('boom');
      imprisonedShipsService.getImprisonedShips.and.returnValue(throwError(() => error));
      const emitted: Action[] = [];
      effects.loadImprisonedShips$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.loadImprisonedShips());

      expect(emitted).toEqual([HarborActions.loadImprisonedShipsFailure({ error })]);
    });

    it('payFine$ pays the Fine and emits payFineSuccess with the payment', () => {
      imprisonedShipsService.payFine.and.returnValue(of(aFinePayment()));
      const emitted: Action[] = [];
      effects.payFine$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFine({ shipId }));

      expect(imprisonedShipsService.payFine).toHaveBeenCalledWith(shipId);
      expect(emitted).toEqual([HarborActions.payFineSuccess({ payment: aFinePayment() })]);
    });

    it('payFine$ emits payFineFailure with the refusal from the Problem Details', () => {
      const error = new HttpErrorResponse({
        status: 409,
        error: { title: 'Savings do not cover', detail: 'The Savings do not cover the Fine of 50.00 $' },
      });
      imprisonedShipsService.payFine.and.returnValue(throwError(() => error));
      const emitted: Action[] = [];
      effects.payFine$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFine({ shipId }));

      expect(emitted).toEqual([
        HarborActions.payFineFailure({ shipId, error, refusal: 'The Savings do not cover the Fine of 50.00 $' }),
      ]);
    });

    it('payFine$ falls back to a general refusal when the error has no detail', () => {
      const error = new HttpErrorResponse({ status: 0 });
      imprisonedShipsService.payFine.and.returnValue(throwError(() => error));
      const emitted: Action[] = [];
      effects.payFine$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFine({ shipId }));

      expect(emitted).toEqual([HarborActions.payFineFailure({ shipId, error, refusal: 'The Fine could not be paid' })]);
    });

    it('refreshAfterFinePaid$ reloads the Imprisoned Ships, the Savings and the fleet', () => {
      const emitted: Action[] = [];
      effects.refreshAfterFinePaid$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFineSuccess({ payment: aFinePayment() }));

      expect(emitted).toEqual([HarborActions.loadImprisonedShips(), HarborActions.loadSavings(), ShipActions.loadShips()]);
    });

    it('showFleetAfterFinePaid$ shows the fleet', () => {
      effects.showFleetAfterFinePaid$.subscribe();

      actions$.next(HarborActions.payFineSuccess({ payment: aFinePayment() }));

      expect(router.navigate).toHaveBeenCalledWith(['/ships']);
    });

    it('dismissFineNotice$ dismisses the Fine notice after a while', fakeAsync(() => {
      const emitted: Action[] = [];
      const subscription = effects.dismissFineNotice$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFineSuccess({ payment: aFinePayment() }));
      tick(7999);
      expect(emitted).toEqual([]);
      tick(1);

      expect(emitted).toEqual([HarborActions.dismissFineNotice()]);
      subscription.unsubscribe();
    }));

    it('refreshAfterRefusedFine$ reloads the Imprisoned Ships and the Savings', () => {
      const emitted: Action[] = [];
      effects.refreshAfterRefusedFine$.subscribe((action) => emitted.push(action));

      actions$.next(HarborActions.payFineFailure({ shipId, error: 'boom', refusal: 'nope' }));

      expect(emitted).toEqual([HarborActions.loadImprisonedShips(), HarborActions.loadSavings()]);
    });

    it('refreshImprisonedShipsWithFleet$ reloads the Imprisoned Ships whenever the fleet is loaded', () => {
      const emitted: Action[] = [];
      effects.refreshImprisonedShipsWithFleet$.subscribe((action) => emitted.push(action));

      actions$.next(ShipActions.loadShipsSuccess({ ships: [] }));

      expect(emitted).toEqual([HarborActions.loadImprisonedShips()]);
    });
  });
});
