import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {Store} from "@ngrx/store";
import {Ship, ShippingState} from "../../models/ship";
import {selectAllShips, selectArrivalNotices} from "../../store/selectors/ship.selectors";
import {ArrivalNotice} from "../../models/fleet-event";
import {ShipService} from "../../services/ship.service";
import * as ShipActions from "../../store/actions/ship.actions";

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css'],
  imports: [
    RouterLink
  ]
})
export class ShipsComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store<{ships: Ship[]}>);
  private readonly shipService = inject(ShipService);
  private readonly router = inject(Router);

  ships = this.store.selectSignal(selectAllShips);
  arrivalNotices = this.store.selectSignal(selectArrivalNotices);

  constructor() {}

  ngOnInit(): void {
    this.store.dispatch(ShipActions.loadShips());
    this.store.dispatch(ShipActions.watchFleet());
  }

  ngOnDestroy(): void {
    this.store.dispatch(ShipActions.stopWatchingFleet());
  }

  dismiss(notice: ArrivalNotice) {
    this.store.dispatch(ShipActions.dismissArrivalNotice({shipId: notice.shipId}));
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
    return '/img/ship.jpg';
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
