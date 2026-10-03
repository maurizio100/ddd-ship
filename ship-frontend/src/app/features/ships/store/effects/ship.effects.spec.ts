import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Action } from '@ngrx/store';
import { Observable, of, ReplaySubject, Subject } from 'rxjs';

import { ARRIVAL_NOTICE_MS, ShipEffects } from './ship.effects';
import * as ShipActions from '../actions/ship.actions';
import { FleetEventsService } from '../../services/fleet-events.service';
import { ShipService } from '../../services/ship.service';
import { FleetEvent } from '../../models/fleet-event';
import { anArrivalNotice, anAvailableShip } from '../../../../../testing/fixtures';

describe('ShipEffects (fleet events)', () => {
  let actions$: ReplaySubject<Action>;
  let pushed: Subject<FleetEvent>;
  let effects: ShipEffects;
  let shipService: jasmine.SpyObj<ShipService>;

  beforeEach(() => {
    actions$ = new ReplaySubject<Action>();
    pushed = new Subject<FleetEvent>();
    shipService = jasmine.createSpyObj<ShipService>('ShipService', ['getShips', 'addShip']);
    const fleetEventsService = jasmine.createSpyObj<FleetEventsService>('FleetEventsService', ['events']);
    fleetEventsService.events.and.returnValue(pushed);
    TestBed.configureTestingModule({
      providers: [
        ShipEffects,
        provideMockActions(() => actions$ as Observable<Action>),
        { provide: ShipService, useValue: shipService },
        { provide: FleetEventsService, useValue: fleetEventsService },
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
});
