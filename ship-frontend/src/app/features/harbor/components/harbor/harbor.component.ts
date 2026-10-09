import { Component, inject, OnInit, signal } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../store/harbor.actions';
import { selectPurchaseRefusal, selectSavings, selectStock } from '../../store/harbor.selectors';
import { StockedCargo } from '../../models/stocked-cargo';

@Component({
  selector: 'app-harbor',
  templateUrl: './harbor.component.html',
  styleUrl: './harbor.component.scss',
  imports: [LowerCasePipe, MatButtonModule],
})
export class HarborComponent implements OnInit {
  private readonly store = inject(Store);

  stock = this.store.selectSignal(selectStock);
  savings = this.store.selectSignal(selectSavings);
  purchaseRefusal = this.store.selectSignal(selectPurchaseRefusal);

  /** The quantity the User entered per Cargo; a Cargo without an entry buys 1. */
  private readonly quantities = signal<Record<string, number>>({});

  ngOnInit(): void {
    this.store.dispatch(HarborActions.loadStock());
    this.store.dispatch(HarborActions.loadSavings());
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
}
