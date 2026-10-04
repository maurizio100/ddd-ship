import {Component, inject, OnInit, signal} from '@angular/core';
import { Router } from '@angular/router';
import {AsyncPipe, Location} from '@angular/common';
import {Store} from "@ngrx/store";
import {Actions, ofType} from "@ngrx/effects";
import {take} from "rxjs";
import {FormsModule} from "@angular/forms";
import {Ship} from "../../models/ship";
import {Catain} from "../../../catains/model/catain";
import {NewShipRequest} from "../../models/new-ship-request";
import * as ShipActions from "../../store/actions/ship.actions";
import * as CatainsActions from "../../../catains/store/catains.actions";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {environment} from "../../../../../environments/environment";

@Component({
  selector: 'app-new-ship',
  templateUrl: './new-ship.component.html',
  styleUrl: './new-ship.component.scss',
  imports: [
    FormsModule,
    AsyncPipe,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
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

  private readonly failedImages = signal<ReadonlySet<string>>(new Set());

  ngOnInit(): void {
    this.catainStore.dispatch(CatainsActions.loadCatains());
  }

  getImageUrl(id: string): string {
    return `${this.catainsUrl}/${id}/image`;
  }

  isSelected(id: string): boolean {
    return id === this.shipRequest.catainId;
  }

  imageFailed(id: string): boolean {
    return this.failedImages().has(id);
  }

  onImageError(id: string): void {
    this.failedImages.update((failed) => new Set(failed).add(id));
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
