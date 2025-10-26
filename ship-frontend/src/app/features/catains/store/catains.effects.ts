import {CatainService} from "../services/catain.service";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import {inject, Injectable} from "@angular/core";
import * as CatainActions from "./catains.actions";
import {exhaustMap, map} from "rxjs";

@Injectable()
export class CatainsEffects {

  private readonly actions$ = inject(Actions);
  private readonly catainService = inject(CatainService);

  loadCatains$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CatainActions.loadCatains),
      exhaustMap(() => this.catainService.getCatains().pipe(
        map(catains => (CatainActions.loadCatainsSuccess({catains}))),
      ))
    )
  });
}
