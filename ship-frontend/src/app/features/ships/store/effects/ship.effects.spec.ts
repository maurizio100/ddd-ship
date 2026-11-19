import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Action } from '@ngrx/store';
import { Observable, of, ReplaySubject, Subject, throwError } from 'rxjs';

import { ARRIVAL_NOTICE_MS, FLEET_RECONNECT_DELAY_MS, ShipEffects } from './ship.effects';
import * as ShipActions from '../actions/ship.actions';
import { FleetEventsService } from '../../services/fleet-events.service';
import { ShipService } from '../../services/ship.service';
import { FleetEvent } from '../../models/fleet-event';
import { anArrivalNotice, anAvailableShip, aShippingSummary } from '../../../../../testing/fixtures';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

describe('ShipEffects (fleet events)', () => {
  let actions$: ReplaySubject<Action>;
  let pushed: Subject<FleetEvent>;
  let effects: ShipEffects;
  let shipService: jasmine.SpyObj<ShipService>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    pushed = new Subject<FleetEvent>();
    shipService = jasmine.createSpyObj<ShipService>('ShipService', ['getShips', 'addShip', 'createShipping']);
    const fleetEventsService = jasmine.createSpyObj<FleetEventsService>('FleetEventsService', ['events']);
    fleetEventsService.events.and.returnValue(pushed);
    TestBed.configureTestingModule({
      providers: [
        ShipEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: ShipService, useValue: shipService },
        { provide: FleetEventsService, useValue: fleetEventsService },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
      ],
    });
    effects = TestBed.inject(ShipEffects);
  });

  it('watchFleet$ maps pushed events to shipArrived / shipLeft until stopWatchingFleet', () => {
    const emitted: Action[] = [];
    effects.watchFleet$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.watchFleet());
    pushed.next({ type: 'ship-arrived', ship: anArrivalNotice() });
    pushed.next({ type: 'ship-left', ship: { shipId: 'id-2', shipName: 'Interceptor', destinationHarbor: 'Nassau' } });
    actions$.next(ShipActions.stopWatchingFleet());
    pushed.next({ type: 'ship-arrived', ship: anArrivalNotice({ shipName: 'Too late' }) });

    expect(emitted).toEqual([
      ShipActions.shipArrived(anArrivalNotice()),
      ShipActions.shipLeft({ shipId: 'id-2', shipName: 'Interceptor', destinationHarbor: 'Nassau' }),
    ]);
  });

  it('refetchOnArrival$ reloads the fleet when a ship arrives', () => {
    const emitted: Action[] = [];
    effects.refetchOnArrival$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.shipArrived(anArrivalNotice()));

    expect(emitted).toEqual([ShipActions.loadShips()]);
  });

  it('dismissArrivalNotice$ dismisses the notice after ARRIVAL_NOTICE_MS', fakeAsync(() => {
    const emitted: Action[] = [];
    const subscription = effects.dismissArrivalNotice$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.shipArrived(anArrivalNotice()));
    tick(ARRIVAL_NOTICE_MS - 1);
    expect(emitted).toEqual([]);
    tick(1);

    expect(emitted).toEqual([ShipActions.dismissArrivalNotice({ shipId: anArrivalNotice().shipId })]);
    subscription.unsubscribe();
  }));

  it('loadShips$ starts a new fetch for a load requested while one is running', () => {
    const first = new Subject<ReturnType<typeof anAvailableShip>[]>();
    shipService.getShips.and.returnValues(first, of([anAvailableShip()]));
    const emitted: Action[] = [];
    effects.loadShips$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.loadShips());
    actions$.next(ShipActions.loadShips());

    expect(shipService.getShips).toHaveBeenCalledTimes(2);
    expect(emitted).toEqual([ShipActions.loadShipsSuccess({ ships: [anAvailableShip()] })]);
  });

  it('refetchOnShipLeft$ reloads the fleet so a stale in-flight load cannot resurrect the ship', () => {
    const emitted: Action[] = [];
    effects.refetchOnShipLeft$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.shipLeft({ shipId: 'id-2', shipName: 'Interceptor', destinationHarbor: 'Nassau' }));

    expect(emitted).toEqual([ShipActions.loadShips()]);
  });

  it('watchFleet$ reloads the fleet whenever the stream (re)opens', () => {
    const emitted: Action[] = [];
    effects.watchFleet$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.watchFleet());
    pushed.next({ type: 'connected' });

    expect(emitted).toEqual([ShipActions.loadShips()]);
  });

  it('watchFleet$ resubscribes after a failed stream once the delay has passed', fakeAsync(() => {
    const emitted: Action[] = [];
    const sources = [new Subject<FleetEvent>(), new Subject<FleetEvent>()];
    const fleetEventsService = TestBed.inject(FleetEventsService) as jasmine.SpyObj<FleetEventsService>;
    fleetEventsService.events.and.returnValues(sources[0], sources[1]);
    const subscription = effects.watchFleet$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.watchFleet());
    sources[0].error(new Error('stream closed'));
    expect(fleetEventsService.events).toHaveBeenCalledTimes(1);
    tick(FLEET_RECONNECT_DELAY_MS);
    expect(fleetEventsService.events).toHaveBeenCalledTimes(2);
    sources[1].next({ type: 'connected' });

    expect(emitted).toEqual([ShipActions.loadShips()]);
    subscription.unsubscribe();
  }));

  it('watchFleet$ maps a pushed ship-imprisoned to shipImprisoned', () => {
    const emitted: Action[] = [];
    effects.watchFleet$.subscribe((action) => emitted.push(action));
    const imprisoned = { shipId: 'id-1', shipName: 'Salty Whisker', homeHarbor: 'Tortuga', sailsHome: true };

    actions$.next(ShipActions.watchFleet());
    pushed.next({ type: 'ship-imprisoned', ship: imprisoned });

    expect(emitted).toEqual([ShipActions.shipImprisoned(imprisoned)]);
  });

  it('refetchOnImprisonment$ reloads the fleet when the Coast Guard imprisons a ship here', () => {
    const emitted: Action[] = [];
    effects.refetchOnImprisonment$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.shipImprisoned({ shipId: 'id-1', shipName: 'Salty Whisker', homeHarbor: 'Tortuga', sailsHome: false }));

    expect(emitted).toEqual([ShipActions.loadShips()]);
  });

  it('dismissImprisonmentNotice$ dismisses the notice after ARRIVAL_NOTICE_MS', fakeAsync(() => {
    const emitted: Action[] = [];
    const subscription = effects.dismissImprisonmentNotice$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.shipImprisoned({ shipId: 'id-1', shipName: 'Salty Whisker', homeHarbor: 'Tortuga', sailsHome: true }));
    tick(ARRIVAL_NOTICE_MS - 1);
    expect(emitted).toEqual([]);
    tick(1);

    expect(emitted).toEqual([ShipActions.dismissImprisonmentNotice({ shipId: 'id-1' })]);
    subscription.unsubscribe();
  }));

  it('createShipping$ starts the new Shipping and reports its summary', () => {
    const ship = anAvailableShip();
    shipService.createShipping.and.returnValue(of(aShippingSummary()));
    const emitted: Action[] = [];
    effects.createShipping$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.createShipping({ ship }));

    expect(shipService.createShipping).toHaveBeenCalledWith(ship);
    expect(emitted).toEqual([ShipActions.createShippingSuccess({ shippingSummary: aShippingSummary() })]);
  });

  it('createShipping$ maps a 409 to createShippingFailure with the Problem detail, or a fallback', () => {
    const ship = anAvailableShip();
    shipService.createShipping.and.returnValues(
      throwError(() => new HttpErrorResponse({
        status: 409,
        error: { title: 'New Shipping refused', detail: 'Black Pearl is imprisoned' },
      })),
      throwError(() => new HttpErrorResponse({ status: 409 })),
    );
    const emitted: Action[] = [];
    effects.createShipping$.subscribe((action) => emitted.push(action));

    actions$.next(ShipActions.createShipping({ ship }));
    actions$.next(ShipActions.createShipping({ ship }));

    expect(emitted).toEqual([
      ShipActions.createShippingFailure({ shipId: ship.id, error: 'Black Pearl is imprisoned' }),
      ShipActions.createShippingFailure({ shipId: ship.id, error: 'A new Shipping could not be started' }),
    ]);
  });

  it('createShippingSuccess$ opens the new Shipping to load Cargo', () => {
    const router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    effects.createShippingSuccess$.subscribe();

    actions$.next(ShipActions.createShippingSuccess({ shippingSummary: aShippingSummary() }));

    expect(router.navigate).toHaveBeenCalledWith([`ships/${aShippingSummary().shipId}/cargo`]);
  });
});
