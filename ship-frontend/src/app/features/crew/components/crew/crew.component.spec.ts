import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { CrewComponent } from './crew.component';
import { Recruit } from '../../../harbor/models/recruit';
import { ShipClass, ShippingState } from '../../../ships/models/ship';
import * as HarborActions from '../../../harbor/store/harbor.actions';
import * as ShipActions from '../../../ships/store/actions/ship.actions';
import { anAvailableShip, aSavings } from '../../../../../testing/fixtures';

const RECRUITS: Recruit[] = ['Mittens', 'Patch', 'Saffron', 'Biscuit', 'Pepper'].map((name, i) => ({
  id: `00000000-0000-4000-8000-00000000000${i}`,
  name,
  trait: `trait ${i}`,
}));

describe('CrewComponent (A Harbor offers five Recruits)', () => {
  let fixture: ComponentFixture<CrewComponent>;
  let store: MockStore;

  function render(options: { recruits?: Recruit[]; savings?: { amount: string } | null; shipId?: string } = {}): void {
    const ship = anAvailableShip({ shipClass: ShipClass.BRIG, crewCapacity: 3, shippingState: ShippingState.IDLE });
    TestBed.configureTestingModule({
      imports: [CrewComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map([['id', options.shipId ?? ship.id]]) } } },
        provideMockStore({
          initialState: {
            ships: { ships: [ship], loading: false, error: null, arrivalNotices: [] },
            harbor: {
              stock: [],
              savings: options.savings === undefined ? aSavings() : options.savings,
              incomingShips: [],
              imprisonedShips: [],
              recruitPool: options.recruits ?? RECRUITS,
              loading: false,
              error: null,
              purchaseRefusal: null,
            },
          },
        }),
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(CrewComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));
  const text = (testId: string): string => all(testId)[0]?.textContent!.trim();

  it('shows the five Recruits of the Recruit Pool with name and trait', () => {
    render();

    const rows = all('crew-recruit');
    expect(rows.length).toBe(5);
    expect(all('crew-recruit-name').map((n) => n.textContent!.trim())).toEqual(RECRUITS.map((r) => r.name));
    expect(all('crew-recruit-trait').map((n) => n.textContent!.trim())).toEqual(RECRUITS.map((r) => r.trait));
    expect(rows[1].querySelector('[aria-hidden="true"]')!.textContent!.trim()).toBe('P');
  });

  it('shows the header, the capacity and the free berths of the ship', () => {
    render();

    expect(fixture.nativeElement.querySelector('.kicker').textContent).toContain('The crew of');
    expect(text('crew-ship-name')).toBe('Black Pearl');
    expect(text('crew-ship-facts')).toBe('a Brig under Catain Furry Jones · Home Harbor Port Royal · In port');
    expect(text('crew-savings')).toContain('1000.00');
    expect(text('crew-capacity')).toBe('0 / 3 · Brig');
    expect(all('crew-free-berth').length).toBe(3);
    expect(all('crew-free-berth')[0].textContent).toContain('A free berth');
  });

  it('links back to the Available Ships and offers no Hire button', () => {
    render();

    expect(all('crew-back')[0].getAttribute('href')).toBe('/ships');
    expect(all('crew-back')[0].textContent).toContain('Available Ships');
    expect(fixture.nativeElement.textContent).not.toContain('Hire');
  });

  it('omits the Savings while they are unknown and says so for an empty pool', () => {
    render({ savings: null, recruits: [] });

    expect(all('crew-savings')).toEqual([]);
    expect(all('crew-recruit')).toEqual([]);
    expect(text('crew-recruit-pool-empty')).toBe('No Recruits offered');
  });

  it('says so for an unknown ship and still links back', () => {
    render({ shipId: 'unknown' });

    expect(text('crew-ship-not-found')).toBe('Ship not found');
    expect(all('crew-back').length).toBe(1);
    expect(all('crew-ship-name')).toEqual([]);
  });

  it('loads the fleet, the Savings and the Recruit Pool on init', () => {
    render();

    expect(store.dispatch).toHaveBeenCalledWith(ShipActions.loadShips());
    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadSavings());
    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadRecruitPool());
  });
});
