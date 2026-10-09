import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../store/harbor.actions';
import {
  selectIncomingShips,
  selectPurchaseRefusal,
  selectSavings,
  selectStock,
  selectUnloadingShipIds,
  selectUnloadRefusal,
} from '../../store/harbor.selectors';
import { StockedCargo } from '../../models/stocked-cargo';
import { IncomingShip } from '../../models/incoming-ship';

@Component({
  selector: 'app-harbor',
  templateUrl: './harbor.component.html',
  styleUrl: './harbor.component.scss',
  imports: [LowerCasePipe, MatButtonModule],
})
export class HarborComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store);

  stock = this.store.selectSignal(selectStock);
  savings = this.store.selectSignal(selectSavings);
  purchaseRefusal = this.store.selectSignal(selectPurchaseRefusal);
  incomingShips = this.store.selectSignal(selectIncomingShips);
  unloadRefusal = this.store.selectSignal(selectUnloadRefusal);
  unloadingShipIds = this.store.selectSignal(selectUnloadingShipIds);

  /** The quantity the User entered per Cargo; a Cargo without an entry buys 1. */
  private readonly quantities = signal<Record<string, number>>({});

  ngOnInit(): void {
    this.store.dispatch(HarborActions.loadStock());
    this.store.dispatch(HarborActions.loadSavings());
    this.store.dispatch(HarborActions.loadIncomingShips());
    this.store.dispatch(HarborActions.watchArrivals());
  }

  ngOnDestroy(): void {
    this.store.dispatch(HarborActions.stopWatchingArrivals());
  }

  /** The Cargo aboard grouped by name, in the order it is first aboard: "2 × Rum, 1 × Sugar". */
  cargoSummary(ship: IncomingShip): string {
    const counts = new Map<string, number>();
    ship.cargo.forEach((cargo) => counts.set(cargo.name, (counts.get(cargo.name) ?? 0) + 1));
    return Array.from(counts, ([name, count]) => `${count} × ${name}`).join(', ');
  }

  quantityOf(cargo: StockedCargo): number {
    return this.quantities()[cargo.cargoId] ?? 1;
  }

  setQuantity(cargo: StockedCargo, event: Event): void {
    const quantity = Number((event.target as HTMLInputElement).value);
    this.quantities.update((quantities) => ({ ...quantities, [cargo.cargoId]: quantity }));
  }

  canBuy(cargo: StockedCargo): boolean {
    const quantity = this.quantityOf(cargo);
    return cargo.price !== null && Number.isInteger(quantity) && quantity >= 1;
  }

  buy(cargo: StockedCargo): void {
    if (!this.canBuy(cargo)) return;
    this.store.dispatch(HarborActions.buyCargo({ cargoId: cargo.cargoId, quantity: this.quantityOf(cargo) }));
  }

  /** An Incoming Ship cannot be unloaded while its Delivery Price is unknown or its unloading is in flight. */
  canUnload(ship: IncomingShip): boolean {
    return ship.deliveryPrice !== null && !this.unloadingShipIds().includes(ship.shipId);
  }

  unload(ship: IncomingShip): void {
    if (!this.canUnload(ship)) return;
    this.store.dispatch(HarborActions.unloadIncomingShip({ shipId: ship.shipId }));
  }
}
