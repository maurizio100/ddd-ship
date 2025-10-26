import {Component, inject, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import {AsyncPipe, Location, NgClass} from '@angular/common';
import {Store} from "@ngrx/store";
import {Actions, ofType} from "@ngrx/effects";
import {take} from "rxjs";
import {FormsModule} from "@angular/forms";
import {Ship} from "../../models/ship";
import {Catain} from "../../../catains/model/catain";
import {NewShipRequest} from "../../models/new-ship-request";
import * as ShipActions from "../../store/actions/ship.actions";
import * as CatainsActions from "../../../catains/store/catains.actions";
import {environment} from "../../../../../environments/environment";

@Component({
  selector: 'app-new-ship',
  templateUrl: './new-ship.component.html',
  styleUrls: ['./new-ship.component.css'],
  imports: [
    FormsModule,
    NgClass,
    AsyncPipe
  ]
})
export class NewShipComponent implements OnInit {
  private readonly shipStore = inject(Store<Ship>);
  private readonly catainStore = inject(Store<Catain>);
  private readonly actions$ = inject(Actions);
  private readonly router = inject(Router);
  private readonly location = inject(Location);

  private readonly catainsUrl = `${environment.baseUrl}/catains`

  catains$ = this.catainStore.select(state => state.catains.catains);

  shipRequest: NewShipRequest = {
    name: '',
    catainId: '',
  };

  ngOnInit(): void {
    this.catainStore.dispatch(CatainsActions.loadCatains());
  }

  getImageUrl(id: string): string {
    return `${this.catainsUrl}/${id}/image`;
  }

  createShip() {
    this.shipStore.dispatch(ShipActions.addShip(this.shipRequest));
    this.actions$.pipe(
      ofType(ShipActions.addShipSuccess),
      take(1)
    ).subscribe(() => this.router.navigate(['/ships']));
  }

  cancel() {
    this.location.back();
  }
}
