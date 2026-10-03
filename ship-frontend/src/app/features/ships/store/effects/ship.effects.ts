import {inject, Injectable} from "@angular/core";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import * as ShipActions from "../actions/ship.actions";
import {catchError, EMPTY, exhaustMap, map, Observable, of} from "rxjs";
import {Action} from "@ngrx/store";
import {ShipService} from "../../services/ship.service";

/** How long the User is told that a ship has arrived. */
export const ARRIVAL_NOTICE_MS = 8000;

@Injectable()
export class ShipEffects {

  private actions$ = inject(Actions);
  private shipService = inject(ShipService);

  loadShips$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ShipActions.loadShips),
      exhaustMap(() => this.shipService.getShips()
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

  watchFleet$ = createEffect((): Observable<Action> => EMPTY);

  refetchOnArrival$ = createEffect((): Observable<Action> => EMPTY);

  dismissArrivalNotice$ = createEffect((): Observable<Action> => EMPTY);
}
