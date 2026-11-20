import { Component, computed, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../../harbor/store/harbor.actions';
import * as ShipActions from '../../../ships/store/actions/ship.actions';
import { selectRecruitPool, selectSavings } from '../../../harbor/store/harbor.selectors';
import { selectAllShips } from '../../../ships/store/selectors/ship.selectors';
import { SHIP_CLASS_LABELS, ShippingState, shipStatusLabel } from '../../../ships/models/ship';
import { hireRecruit, loadHarborName } from '../../store/crew.actions';
import {
  selectHarborName,
  selectHarborNameError,
  selectHireRefusal,
  selectHiringRecruitIds,
  selectNewInPoolRecruitIds,
} from '../../store/crew.selectors';

/** The Crew screen of a ship: its Crew, its free berths and the Recruit Pool of the Harbor, from which the User Hires. */
@Component({
  selector: 'app-crew',
  templateUrl: './crew.component.html',
  styleUrl: './crew.component.scss',
  imports: [RouterLink, MatIconModule, MatButtonModule],
})
export class CrewComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly shipId = inject(ActivatedRoute).snapshot.paramMap.get('id');

  savings = this.store.selectSignal(selectSavings);
  recruitPool = this.store.selectSignal(selectRecruitPool);
  harborName = this.store.selectSignal(selectHarborName);
  harborNameError = this.store.selectSignal(selectHarborNameError);
  hiringRecruitIds = this.store.selectSignal(selectHiringRecruitIds);
  hireRefusal = this.store.selectSignal(selectHireRefusal);
  newInPoolRecruitIds = this.store.selectSignal(selectNewInPoolRecruitIds);
  private readonly ships = this.store.selectSignal(selectAllShips);

  ship = computed(() => this.ships().find((ship) => ship.id === this.shipId) ?? null);
  classLabel = computed(() => {
    const ship = this.ship();
    return ship ? SHIP_CLASS_LABELS[ship.shipClass] : '';
  });
  freeBerths = computed(() =>
    Array.from({ length: Math.max((this.ship()?.crewCapacity ?? 0) - (this.ship()?.crew.length ?? 0), 0) }),
  );
  atSea = computed(() => this.ship()?.shippingState === ShippingState.SHIPPING);
  /** Away from the Home Harbor: only known once this Harbor's name is. */
  away = computed(() => {
    const harborName = this.harborName();
    return !!harborName && this.ship()?.homeHarbor !== harborName;
  });
  crewFull = computed(() => {
    const ship = this.ship();
    return !!ship && ship.crew.length >= ship.crewCapacity;
  });
  /** The Recruit Pool is offered only to a ship in port at its Home Harbor with a free berth. */
  poolOffered = computed(() => !this.atSea() && !this.away() && !this.crewFull());
  hiringFee = computed(() => this.recruitPool()[0]?.hiringFee ?? null);
  facts = computed(() => {
    const ship = this.ship();
    if (!ship) return '';
    const harborName = this.harborName();
    const status = shipStatusLabel(ship);
    const where = status === 'In port' && harborName ? `${status} at ${harborName}` : status;
    return `a ${this.classLabel()} under ${ship.catain} · Home Harbor ${ship.homeHarbor} · ${where}`;
  });

  ngOnInit(): void {
    this.store.dispatch(loadHarborName());
    this.store.dispatch(ShipActions.loadShips());
    this.store.dispatch(HarborActions.loadSavings());
    this.store.dispatch(HarborActions.loadRecruitPool());
  }

  hire(shipId: string, recruitId: string): void {
    this.store.dispatch(hireRecruit({ shipId, recruitId }));
  }

  initial(name: string): string {
    return name.charAt(0).toUpperCase();
  }
}
