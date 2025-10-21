import {Component, inject, OnInit} from '@angular/core';
import { Catain } from '../models/catain';
import { CatainService } from '../services/catain.service';
import { NewShipRequest } from '../models/new-ship-request';
import { Router } from '@angular/router';
import {Location, NgClass} from '@angular/common';
import {Store} from "@ngrx/store";
import * as ShipActions from "../ngrx/ship.actions";
import {Ship} from "../models/ship";
import {Actions, ofType} from "@ngrx/effects";
import {take} from "rxjs";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-new-ship',
  templateUrl: './new-ship.component.html',
  styleUrls: ['./new-ship.component.css'],
  imports: [
    FormsModule,
    NgClass
  ]
})
export class NewShipComponent implements OnInit {
  private store = inject(Store<Ship>);
  private actions$ = inject(Actions);

  catains: Catain[] = [];

  shipRequest: NewShipRequest = {
    name: '',
    catainId: '',
  };

  constructor(
    private catainService: CatainService,
    private router: Router,
    private location: Location,
  ) {}

  ngOnInit(): void {
    this.getCatains();
  }

  getCatains(): void {
    this.catainService
      .getCatains()
      .subscribe((catains) => (this.catains = catains));
  }

  getImageUrl(id: string): string {
    return this.catainService.getCatainImageUrl(id);
  }

  createShip() {
    this.store.dispatch(ShipActions.addShip(this.shipRequest));
    this.actions$.pipe(
      ofType(ShipActions.addShipSuccess),
      take(1)
    ).subscribe(() => this.router.navigate(['/ships']));
  }

  cancel() {
    this.location.back();
  }
}
