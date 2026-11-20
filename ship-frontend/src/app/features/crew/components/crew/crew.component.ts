import { Component, computed, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../../harbor/store/harbor.actions';
import * as ShipActions from '../../../ships/store/actions/ship.actions';
import { selectRecruitPool, selectSavings } from '../../../harbor/store/harbor.selectors';
import { selectAllShips } from '../../../ships/store/selectors/ship.selectors';
import { SHIP_CLASS_LABELS, shipStatusLabel } from '../../../ships/models/ship';

/** The read-only Crew screen of a ship: its free berths and the Recruit Pool of the Harbor. */
@Component({
  selector: 'app-crew',
  templateUrl: './crew.component.html',
  styleUrl: './crew.component.scss',
  imports: [RouterLink, MatIconModule],
})
export class CrewComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly shipId = inject(ActivatedRoute).snapshot.paramMap.get('id');

  savings = this.store.selectSignal(selectSavings);
  recruitPool = this.store.selectSignal(selectRecruitPool);
  private readonly ships = this.store.selectSignal(selectAllShips);

  ship = computed(() => this.ships().find((ship) => ship.id === this.shipId) ?? null);
  classLabel = computed(() => {
    const ship = this.ship();
    return ship ? SHIP_CLASS_LABELS[ship.shipClass] : '';
  });
  freeBerths = computed(() => Array.from({ length: this.ship()?.crewCapacity ?? 0 }));
  facts = computed(() => {
    const ship = this.ship();
    return ship
      ? `a ${this.classLabel()} under Catain ${ship.catain} · Home Harbor ${ship.homeHarbor} · ${shipStatusLabel(ship)}`
      : '';
  });

  ngOnInit(): void {
    this.store.dispatch(ShipActions.loadShips());
    this.store.dispatch(HarborActions.loadSavings());
    this.store.dispatch(HarborActions.loadRecruitPool());
  }

  initial(name: string): string {
    return name.charAt(0).toUpperCase();
  }
}
