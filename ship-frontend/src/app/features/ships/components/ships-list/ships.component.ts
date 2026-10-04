import {Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {Store} from "@ngrx/store";
import {Ship, ShippingState} from "../../models/ship";
import {selectAllShips, selectArrivalNotices} from "../../store/selectors/ship.selectors";
import {ArrivalNotice} from "../../models/fleet-event";
import {ShipService} from "../../services/ship.service";
import * as ShipActions from "../../store/actions/ship.actions";
import * as CatainsActions from "../../../catains/store/catains.actions";
import {selectAllCatains} from "../../../catains/store/catains.selectors";
import {environment} from "../../../../../environments/environment";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrl: './ships.component.scss',
  imports: [
    RouterLink,
    MatButtonModule,
    MatIconModule
  ]
})
export class ShipsComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store<{ships: Ship[]}>);
  private readonly shipService = inject(ShipService);
  private readonly router = inject(Router);

  ships = this.store.selectSignal(selectAllShips);
  arrivalNotices = this.store.selectSignal(selectArrivalNotices);

  catains = this.store.selectSignal(selectAllCatains);
  private readonly failedImages = signal<ReadonlySet<string>>(new Set());

  constructor() {}

  ngOnInit(): void {
    this.store.dispatch(CatainsActions.loadCatains());
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

  statusLabel(ship: Ship): string {
    switch (ship.shippingState) {
      case ShippingState.PREPARING:
        return 'Preparing';
      case ShippingState.SHIPPING:
        return 'At sea';
      default:
        return 'In port';
    }
  }

  catainImageUrl(ship: Ship): string | null {
    const catain = this.catains().find((c) => c.name === ship.catain);
    return catain ? `${environment.baseUrl}/catains/${catain.id}/image` : null;
  }

  imageFailed(ship: Ship): boolean {
    return this.failedImages().has(ship.id);
  }

  onImageError(ship: Ship): void {
    this.failedImages.update((failed) => new Set(failed).add(ship.id));
  }
}
