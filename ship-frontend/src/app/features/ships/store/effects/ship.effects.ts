import {inject, Injectable} from "@angular/core";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import * as ShipActions from "../actions/ship.actions";
import {catchError, defer, exhaustMap, map, mergeMap, of, retry, switchMap, takeUntil, tap, timer} from "rxjs";
import {HttpErrorResponse} from "@angular/common/http";
import {Router} from "@angular/router";
import {ShipService} from "../../services/ship.service";
import {FleetEventsService} from "../../services/fleet-events.service";
import {FleetEvent} from "../../models/fleet-event";

/** How long the User is told that a ship has arrived. */
export const ARRIVAL_NOTICE_MS = 8000;

/** How long to wait before opening a new fleet-events stream after the browser gave up on the last one. */
export const FLEET_RECONNECT_DELAY_MS = 3000;

@Injectable()
export class ShipEffects {

  private actions$ = inject(Actions);
  private shipService = inject(ShipService);
  private fleetEventsService = inject(FleetEventsService);
  private router = inject(Router);

  /** A newer load wins: a refetch after a pushed arrival is never dropped behind a running one. */
  loadShips$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.loadShips),
      switchMap(() => this.shipService.getShips()
        .pipe(
          map(ships => (ShipActions.loadShipsSuccess({ships}))),
          catchError(() => of(ShipActions.loadShipsFailed()))
        ))
    )
  });

  loadShipClasses$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.loadShipClasses),
      switchMap(() => this.shipService.getShipClasses()
        .pipe(
          map(shipClasses => ShipActions.loadShipClassesSuccess({shipClasses})),
          catchError(() => of(ShipActions.loadShipClassesFailure({error: 'Failed to load ship classes'})))
        ))
    )
  });

  createShip$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.addShip),
      exhaustMap((ship) =>
        this.shipService.addShip(ship)
        .pipe(
          map(ship => (ShipActions.addShipSuccess(ship))),
          catchError(() => of(ShipActions.addShipFailure({error: 'Failed to add ship'})))
        )
      )
    )
  })

  watchFleet$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.watchFleet),
      switchMap(() => defer(() => this.fleetEventsService.events()).pipe(
        retry({delay: FLEET_RECONNECT_DELAY_MS}),
        map(toAction),
        takeUntil(this.actions$.pipe(ofType(ShipActions.stopWatchingFleet)))
      ))
    )
  });

  refetchOnArrival$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.shipArrived),
      map(() => ShipActions.loadShips())
    )
  });

  /** A load still in flight may predate the departure; a newer one cancels it, so the ship stays gone. */
  refetchOnShipLeft$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.shipLeft),
      map(() => ShipActions.loadShips())
    )
  });

  /** The imprisoned ship's Cargo and state changed by itself: the fleet is fetched again. */
  refetchOnImprisonment$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.shipImprisoned),
      map(() => ShipActions.loadShips())
    )
  });

  dismissImprisonmentNotice$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.shipImprisoned),
      mergeMap(({shipId}) => timer(ARRIVAL_NOTICE_MS).pipe(
        map(() => ShipActions.dismissImprisonmentNotice({shipId}))
      ))
    )
  });

  /** A refused new Shipping (409 Problem Details) tells the User the backend's `detail`. */
  createShipping$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.createShipping),
      exhaustMap(({ship}) => this.shipService.createShipping(ship).pipe(
        map(shippingSummary => ShipActions.createShippingSuccess({shippingSummary})),
        catchError((error: HttpErrorResponse) => of(ShipActions.createShippingFailure({
          shipId: ship.id,
          error: error.error?.detail ?? 'A new Shipping could not be started'
        })))
      ))
    )
  });

  createShippingSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.createShippingSuccess),
      tap(({shippingSummary}) => this.router.navigate([`ships/${shippingSummary.shipId}/cargo`]))
    )
  }, {dispatch: false});

  dismissArrivalNotice$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.shipArrived),
      mergeMap(({shipId}) => timer(ARRIVAL_NOTICE_MS).pipe(
        map(() => ShipActions.dismissArrivalNotice({shipId}))
      ))
    )
  });
}

function toAction(event: FleetEvent) {
  switch (event.type) {
    case 'ship-arrived':
      return ShipActions.shipArrived(event.ship);
    case 'ship-left':
      return ShipActions.shipLeft(event.ship);
    case 'ship-imprisoned':
      return ShipActions.shipImprisoned(event.ship);
    case 'connected':
      return ShipActions.loadShips();
  }
}
