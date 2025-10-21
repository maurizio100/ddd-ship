import {inject, Injectable} from "@angular/core";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import * as ShipActions from "../actions/ship.actions";
import {catchError, exhaustMap, map, of} from "rxjs";
import {ShipService} from "../../services/ship.service";

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
}

