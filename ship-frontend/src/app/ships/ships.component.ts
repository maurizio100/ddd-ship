import {Component, inject, OnInit} from '@angular/core';
import {Ship, ShippingState} from '../models/ship';
import {ShipService} from '../services/ship.service';
import {Router, RouterLink} from '@angular/router';
import {Store} from "@ngrx/store";
import * as ShipActions from "../ngrx/ship.actions";
import {selectAllShips} from "../ngrx/ship.selectors";
import {AsyncPipe} from "@angular/common";

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css'],
  imports: [
    AsyncPipe,
    RouterLink
  ],
  standalone: true
})
export class ShipsComponent implements OnInit {
  // shippingState = ShippingState;
  private store = inject(Store<{ships: Ship[]}>);
  ships$ = this.store.select(selectAllShips);

  constructor(
    private shipService: ShipService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.store.dispatch(ShipActions.loadShips());
  }

  createShipping(ship: Ship) {
    this.shipService
      .createShipping(ship)
      .subscribe((shippingSummary) =>
        this.router.navigate([`ships/${shippingSummary.shipId}/cargo`])
      );
  }

  editShipping(ship: Ship) {
    this.router.navigate([`ships/${ship.id}/cargo`]);
  }

  getImageUrl(ship: Ship) {
    return '/assets/img/ship.jpg';
  }

  canCreateNewShipping(ship: Ship): boolean {
    return (
      ship.shippingState === ShippingState.IDLE ||
      ship.shippingState === ShippingState.DONE
    );
  }

  shippingExists(ship: Ship): boolean {
    return ship.shippingState === ShippingState.PREPARING;
  }

  isShipAway(ship: Ship): boolean {
    return ship.shippingState === ShippingState.SHIPPING;
  }

  protected readonly ShippingState = ShippingState;
}
