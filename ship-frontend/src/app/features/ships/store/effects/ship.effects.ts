import {inject, Injectable} from "@angular/core";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import * as ShipActions from "../actions/ship.actions";
import {catchError, exhaustMap, map, mergeMap, of, switchMap, takeUntil, timer} from "rxjs";
import {ShipService} from "../../services/ship.service";
import {FleetEventsService} from "../../services/fleet-events.service";
import {FleetEvent} from "../../models/fleet-event";

/** How long the User is told that a ship has arrived. */
export const ARRIVAL_NOTICE_MS = 8000;

@Injectable()
export class ShipEffects {

  private actions$ = inject(Actions);
  private shipService = inject(ShipService);
  private fleetEventsService = inject(FleetEventsService);

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
      switchMap(() => this.fleetEventsService.events().pipe(
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
  return event.type === 'ship-arrived'
    ? ShipActions.shipArrived(event.ship)
    : ShipActions.shipLeft(event.ship);
}
