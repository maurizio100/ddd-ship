import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { dismissCrewMember } from '../../store/crew.actions';
import { CrewComponent } from './crew.component';
import { Recruit } from '../../../harbor/models/recruit';
import { Ship, ShipClass, ShippingState } from '../../../ships/models/ship';
import { CrewState } from '../../store/crew.selectors';
import { hireRecruit, loadHarborName } from '../../store/crew.actions';
import * as HarborActions from '../../../harbor/store/harbor.actions';
import * as ShipActions from '../../../ships/store/actions/ship.actions';
import { anAvailableShip, aRecruit, aSavings } from '../../../../../testing/fixtures';

const RECRUITS: Recruit[] = ['Mittens', 'Patch', 'Saffron', 'Biscuit', 'Pepper'].map((name, i) => ({
  id: `00000000-0000-4000-8000-00000000000${i}`,
  name,
  trait: `trait ${i}`,
  hiringFee: '50.00',
}));

const MITTENS = { id: '00000000-0000-4000-8000-0000000000aa', name: 'Mittens', trait: 'Strong Paws' };
const NO_REFUSAL = null;

describe('CrewComponent (A Harbor offers five Recruits)', () => {
  let fixture: ComponentFixture<CrewComponent>;
  let store: MockStore;

  function render(
    options: {
      recruits?: Recruit[];
      savings?: { amount: string } | null;
      shipId?: string;
      ship?: Partial<Ship>;
      crew?: Partial<CrewState>;
    } = {},
  ): void {
    const ship = anAvailableShip({
      shipClass: ShipClass.BRIG,
      crewCapacity: 3,
      shippingState: ShippingState.IDLE,
      ...options.ship,
    });
    const crewState: CrewState = {
      hiringRecruitIds: [],
      dismissingCrewMemberIds: [],
      dismissRefusal: null,
      hireRefusal: NO_REFUSAL,
      newInPoolRecruitIds: [],
      harborName: 'Port Royal',
      error: null,
      ...options.crew,
    };
    TestBed.configureTestingModule({
      imports: [CrewComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map([['id', options.shipId ?? ship.id]]) } } },
        provideMockStore({
          initialState: {
            crew: crewState,
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
  const byTestId = (testId: string): HTMLElement | null => all(testId)[0] ?? null;
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
    expect(text('crew-ship-facts')).toBe('a Brig under Furry Jones · Home Harbor Port Royal · In port at Port Royal');
    expect(text('crew-savings')).toContain('1000.00');
    expect(text('crew-capacity')).toBe('0 / 3 · Brig');
    expect(all('crew-free-berth').length).toBe(3);
    expect(all('crew-free-berth')[0].textContent).toContain('A free berth');
  });

  it('links back to the Available Ships and offers a Hire button per Recruit', () => {
    render();

    expect(all('crew-back')[0].getAttribute('href')).toBe('/ships');
    expect(all('crew-back')[0].textContent).toContain('Available Ships');
    expect(all('crew-hire').length).toBe(5);
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

  it('loads the Harbor name on init', () => {
    render();

    expect(store.dispatch).toHaveBeenCalledWith(loadHarborName());
  });

  it('Hiring a Recruit adds them to the Crew and pays the Hiring Fee', () => {
    const mittens = aRecruit();
    render({ recruits: [mittens, ...RECRUITS.slice(1)], ship: { shipClass: ShipClass.SLOOP, crewCapacity: 2 } });

    all('crew-hire')[0].click();

    expect(store.dispatch).toHaveBeenCalledWith(hireRecruit({ shipId: '5f1d7c2e-0000-4000-8000-000000000001', recruitId: mittens.id }));
    expect(text('crew-hiring-fee')).toBe('Hiring Fee 50.00 $, paid from the Savings of Port Royal');

    // after the read-back: the Crew Member is aboard and the Savings are 950.00
    TestBed.resetTestingModule();
    render({
      savings: { amount: '950.00' },
      ship: { shipClass: ShipClass.SLOOP, crewCapacity: 2, crew: [MITTENS] },
    });
    expect(all('crew-member').length).toBe(1);
    expect(text('crew-member-name')).toBe('Mittens');
    expect(text('crew-member-trait')).toBe('Strong Paws');
    expect(text('crew-capacity')).toBe('1 / 2 · Sloop');
    expect(all('crew-free-berth').length).toBe(1);
    expect(text('crew-savings')).toContain('950.00');
  });

  it('the Hire button is labelled with the fee, and every Hire button is disabled while a Hire is in flight', () => {
    render({ crew: { hiringRecruitIds: [RECRUITS[0].id] } });

    expect(all('crew-hire')[0].getAttribute('aria-label')).toBe('Hire Mittens for 50.00 $');
    expect(all('crew-hire').every((b) => (b as HTMLButtonElement).disabled)).toBeTrue();
  });

  it('A new Recruit takes the place of a Hired one', () => {
    const newcomer = RECRUITS[4];
    render({ recruits: RECRUITS.slice(1), crew: { newInPoolRecruitIds: [newcomer.id] } });

    expect(all('crew-recruit-name').map((n) => n.textContent!.trim())).not.toContain('Mittens');
    expect(all('crew-recruit').length).toBe(4);
    const rows = all('crew-recruit');
    expect(rows[3].querySelector('[data-testid="crew-recruit-new"]')!.textContent).toContain('new in the pool');
    expect(rows[0].querySelector('[data-testid="crew-recruit-new"]')).toBeNull();
  });

  it('A ship with a full Crew is offered no Recruits', () => {
    render({ ship: { shipClass: ShipClass.SLOOP, crewCapacity: 2, crew: [MITTENS, { ...MITTENS, id: 'x', name: 'Patch' }] } });

    expect(all('crew-recruit')).toEqual([]);
    expect(all('crew-hire')).toEqual([]);
    expect(text('crew-full')).toBe(
      'The Crew is full: a Sloop carries 2 Crew Members. The Recruit Pool comes back once a Crew Member is dismissed.',
    );
  });

  it('A ship with a full Crew cannot Hire', () => {
    const message = 'The Crew of Black Pearl is full';
    render({ crew: { hireRefusal: { recruitId: RECRUITS[0].id, message, savingsShort: false } } });

    const alert = fixture.nativeElement.querySelector('[role="alert"]');
    expect(alert.textContent).toContain(message);
    expect(all('crew-hire-refusal')[0]).toBe(alert);
  });

  it('A Harbor that cannot pay the Hiring Fee cannot Hire', () => {
    render({
      savings: { amount: '40.00' },
      crew: {
        hireRefusal: {
          recruitId: RECRUITS[0].id,
          message: 'The Savings do not cover the Hiring Fee of 50.00 $',
          savingsShort: true,
        },
      },
    });

    expect(text('crew-hire-refusal')).toContain('The Savings do not cover the Hiring Fee of 50.00 $');
    expect(text('crew-hiring-fee')).toBe('Hiring Fee 50.00 $ · Savings 40.00 $');
    expect(all('crew-hire').length).toBe(5);
    expect(text('crew-empty')).toBe('No Crew yet');
  });

  it('A ship cannot Hire away from its Home Harbor', () => {
    render({ ship: { shipClass: ShipClass.SLOOP, crewCapacity: 2, homeHarbor: 'Tortuga' } });

    expect(all('crew-recruit')).toEqual([]);
    expect(all('crew-hire')).toEqual([]);
    expect(all('crew-hiring-fee')).toEqual([]);
    expect(text('crew-away')).toBe(
      "The Crew is Hired and dismissed only at the Home Harbor Tortuga. Port Royal's Recruit Pool is not offered here.",
    );
    expect(text('crew-ship-facts')).toBe('a Sloop under Furry Jones · Home Harbor Tortuga · In port at Port Royal');
  });

  it('A ship at sea cannot Hire', () => {
    render({ ship: { name: 'Old Barnacle', shippingState: ShippingState.SHIPPING } });

    expect(all('crew-recruit')).toEqual([]);
    expect(all('crew-hire')).toEqual([]);
    expect(text('crew-away')).toBe(
      'Old Barnacle is at sea: the Crew is Hired once it is back in port at its Home Harbor Port Royal.',
    );
  });

  it('shows the refusal of a full Crew even when the reloaded ship shows the full branch', () => {
    render({
      ship: { shipClass: ShipClass.SLOOP, crewCapacity: 1, crew: [MITTENS] },
      crew: { hireRefusal: { recruitId: RECRUITS[0].id, message: 'The Crew of Black Pearl is full', savingsShort: false } },
    });

    expect(text('crew-full')).toContain('The Crew is full');
    expect(text('crew-hire-refusal')).toBe('The Crew of Black Pearl is full');
  });

  it('read-only views name free berths without inviting a Hire and mark the Home Harbor with a home icon', () => {
    render({ ship: { shippingState: ShippingState.SHIPPING } });

    expect(all('crew-free-berth')[0].textContent!.trim()).toBe('A free berth');
    expect(all('crew-away')[0].querySelector('mat-icon')!.getAttribute('fontIcon')).toBe('home');
  });

  it('offers the invitation on free berths when the Recruit Pool is offered', () => {
    render();

    expect(all('crew-free-berth')[0].textContent).toContain('A free berth: Hire a Recruit');
  });

  it('styles the Hire button as a flat Material button', () => {
    render();

    expect(all('crew-hire')[0].hasAttribute('mat-flat-button')).toBeTrue();
  });

  it('offers no Hire button while the Harbor name is unknown', () => {
    render({ crew: { harborName: null } });

    expect(all('crew-hire')).toEqual([]);
  });

  it('shows an alert instead of the Hire buttons when the Harbor name could not be loaded', () => {
    render({ crew: { harborName: null, error: 'boom' } });

    const alert = byTestId('crew-harbor-error');
    expect(alert!.getAttribute('role')).toBe('alert');
    expect(alert!.textContent!.trim()).toBe('The Harbor could not be loaded. Hiring is unavailable.');
    expect(all('crew-hire')).toEqual([]);
  });

  it('shows no Harbor alert while the Harbor name loads fine', () => {
    render();

    expect(byTestId('crew-harbor-error')).toBeNull();
  });

  describe('Dismiss', () => {
    const crewed = { crew: [MITTENS] };

    it('offers a Dismiss button per Crew Member in port at the Home Harbor, with a label and a note', () => {
      render({ ship: crewed });

      const button = all('crew-dismiss')[0];
      expect(all('crew-dismiss').length).toBe(1);
      expect(button.getAttribute('aria-label')).toBe('Dismiss Mittens, no refund of the Hiring Fee');
      expect(button.hasAttribute('mat-stroked-button')).toBeTrue();
      expect(text('crew-dismiss-note')).toBe(
        'A dismissed Crew Member leaves for good: no refund, and they do not return to the Recruit Pool.',
      );
    });

    it('dispatches dismissCrewMember on click', () => {
      render({ ship: crewed });

      all('crew-dismiss')[0].click();

      expect(store.dispatch).toHaveBeenCalledWith(
        dismissCrewMember({ shipId: fixture.componentInstance.ship()!.id, crewMemberId: MITTENS.id }),
      );
    });

    it('disables the button while its dismissal is in flight', () => {
      render({ ship: crewed, crew: { dismissingCrewMemberIds: [MITTENS.id] } });

      expect((all('crew-dismiss')[0] as HTMLButtonElement).disabled).toBeTrue();
    });

    it('offers no Dismiss away from the Home Harbor', () => {
      render({ ship: crewed, crew: { harborName: 'Tortuga' } });

      expect(all('crew-dismiss')).toEqual([]);
      expect(byTestId('crew-dismiss-note')).toBeNull();
    });

    it('offers no Dismiss while the ship is at sea', () => {
      render({ ship: { ...crewed, shippingState: ShippingState.SHIPPING } });

      expect(all('crew-dismiss')).toEqual([]);
      expect(byTestId('crew-dismiss-note')).toBeNull();
    });

    it('shows the refusal as an alert', () => {
      render({ ship: crewed, crew: { dismissRefusal: { crewMemberId: MITTENS.id, message: 'Mittens is not aboard' } } });

      const alert = byTestId('crew-dismiss-refusal');
      expect(alert!.getAttribute('role')).toBe('alert');
      expect(alert!.textContent!.trim()).toBe('Mittens is not aboard');
    });
  });
});
