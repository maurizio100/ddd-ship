import {Component, inject, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {Store} from "@ngrx/store";
import {AsyncPipe} from "@angular/common";
import {Ship, ShippingState} from "../../models/ship";
import {selectAllShips} from "../../store/selectors/ship.selectors";
import {ShipService} from "../../services/ship.service";
import * as ShipActions from "../../store/actions/ship.actions";

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css'],
  imports: [
    AsyncPipe,
    RouterLink
  ]
})
export class ShipsComponent implements OnInit {
  private readonly store = inject(Store<{ships: Ship[]}>);
  private readonly shipService = inject(ShipService);
  private readonly router = inject(Router);

  ships$ = this.store.select(selectAllShips);

  constructor() {}

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
}
