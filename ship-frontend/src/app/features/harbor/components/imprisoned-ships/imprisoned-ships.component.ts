import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../store/harbor.actions';
import * as CatainsActions from '../../../catains/store/catains.actions';
import {
  selectFineRefusal,
  selectImprisonedShips,
  selectPayingFineShipIds,
  selectSavings,
} from '../../store/harbor.selectors';
import { selectAllCatains } from '../../../catains/store/catains.selectors';
import { fineCoverage, ImprisonedShip } from '../../models/imprisoned-ship';
import { environment } from '../../../../../environments/environment';

/** The page of the Imprisoned Ships held at this Harbor, each with the Fine that frees it. */
@Component({
  selector: 'app-imprisoned-ships',
  templateUrl: './imprisoned-ships.component.html',
  styleUrl: './imprisoned-ships.component.scss',
  imports: [MatButtonModule, MatIconModule],
})
export class ImprisonedShipsComponent implements OnInit {
  private readonly store = inject(Store);

  imprisonedShips = this.store.selectSignal(selectImprisonedShips);
  savings = this.store.selectSignal(selectSavings);
  fineRefusal = this.store.selectSignal(selectFineRefusal);
  payingFineShipIds = this.store.selectSignal(selectPayingFineShipIds);
  catains = this.store.selectSignal(selectAllCatains);
  private readonly failedImages = signal<ReadonlySet<string>>(new Set());

  readonly coverage = fineCoverage;

  ngOnInit(): void {
    this.store.dispatch(HarborActions.loadImprisonedShips());
    this.store.dispatch(HarborActions.loadSavings());
    this.store.dispatch(CatainsActions.loadCatains());
  }

  payFine(ship: ImprisonedShip): void {
    this.store.dispatch(HarborActions.payFine({ shipId: ship.shipId }));
  }

  /** The Pay Fine button stays disabled while the Savings fall short or the payment is in flight. */
  canPayFine(ship: ImprisonedShip): boolean {
    return ship.savingsCoverFine && !this.payingFineShipIds().includes(ship.shipId);
  }

  /** Why the last Fine payment for [ship] was refused; null if it was not. */
  refusalFor(ship: ImprisonedShip): string | null {
    const refusal = this.fineRefusal();
    return refusal?.shipId === ship.shipId ? refusal.message : null;
  }

  catainImageUrl(ship: ImprisonedShip): string | null {
    const catain = this.catains().find((c) => c.name === ship.catain);
    return catain ? `${environment.baseUrl}/catains/${catain.id}/image` : null;
  }

  imageFailed(ship: ImprisonedShip): boolean {
    return this.failedImages().has(ship.shipId);
  }

  onImageError(ship: ImprisonedShip): void {
    this.failedImages.update((failed) => new Set(failed).add(ship.shipId));
  }
}
