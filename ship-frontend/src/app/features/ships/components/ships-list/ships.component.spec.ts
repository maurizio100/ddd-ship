import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { ShipsComponent } from './ships.component';
import { Ship, ShipClass, ShippingState } from '../../models/ship';
import { ShipService } from '../../services/ship.service';
import * as HarborActions from '../../../harbor/store/harbor.actions';
import * as ShipActions from '../../store/actions/ship.actions';
import { aFinePayment, anArrivalNotice, anAvailableShip, anImprisonedShip, anImprisonmentNotice, anIncomingShip, aSavings } from '../../../../../testing/fixtures';
import { Catain } from '../../../catains/model/catain';
import { ArrivalNotice, ImprisonmentNotice } from '../../models/fleet-event';

describe('ShipsComponent (The voyage ends at the Origin Harbor)', () => {
  let fixture: ComponentFixture<ShipsComponent>;
  let store: MockStore;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices: [] }, catains: { catains: [], loading: false, error: null } } }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const shipNames = (): string[] => all('ship-name').map((name) => name.textContent!.trim());

  it('The ship stays at sea until it has arrived', () => {
    render([anAvailableShip({ shippingState: ShippingState.SHIPPING })]);

    expect(shipNames()).toEqual(['Black Pearl']);
    expect(all('ship-at-sea').map((atSea) => atSea.textContent!.trim())).toEqual(['Black Pearl is at sea']);
    expect(all('new-shipping')).toEqual([]);
  });

  it('The Shipping is done once the ship has arrived', () => {
    const flyingDutchman = anAvailableShip({ id: 'a7c1e9b3-0000-4000-8000-000000000004', name: 'Flying Dutchman' });
    render([flyingDutchman]);

    expect(store.dispatch).toHaveBeenCalledWith(ShipActions.loadShips());
    expect(all('ship').length).toBe(1);
    expect(all('ship').some((ship) => ship.textContent!.includes('Black Pearl'))).toBeFalse();
  });

  it('Every ship card has a Crew button to its Crew screen', () => {
    render([
      anAvailableShip({ shippingState: ShippingState.SHIPPING }),
      anAvailableShip({ id: 'a7c1e9b3-0000-4000-8000-000000000004', name: 'Flying Dutchman', shippingState: ShippingState.IDLE }),
    ]);

    expect(all('ship-crew').map((a) => a.getAttribute('href'))).toEqual([
      '/ships/5f1d7c2e-0000-4000-8000-000000000001/crew',
      '/ships/a7c1e9b3-0000-4000-8000-000000000004/crew',
    ]);
  });

  it('An arrived ship can sail back', () => {
    render([anAvailableShip({ shippingState: ShippingState.IDLE })]);

    expect(shipNames()).toEqual(['Black Pearl']);
    expect(all('new-shipping').length).toBe(1);
    expect(all('ship-at-sea')).toEqual([]);
  });
});

describe('ShipsComponent (The fleet shows where an arrived ship came from)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices: [] }, catains: { catains: [], loading: false, error: null } } }),
        {
          provide: ShipService,
          useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']),
        },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const arrivedFrom = (): string[] =>
    all('ship-arrived-from').map((badge) => badge.textContent!.trim());

  it('An arrived ship shows its Origin Harbor', () => {
    render([anAvailableShip({ arrivedFrom: 'Tortuga' })]);

    expect(arrivedFrom()).toEqual(['Arrived from Tortuga']);
  });

  it('A ship registered at the Harbor shows no Origin Harbor', () => {
    render([anAvailableShip({ name: 'Interceptor', arrivedFrom: null })]);

    expect(all('ship-name').map((name) => name.textContent!.trim())).toEqual(['Interceptor']);
    expect(all('ship-arrived-from')).toEqual([]);
  });

  it('A ship shows the Harbor of its latest Arrival', () => {
    render([anAvailableShip({ arrivedFrom: 'Nassau' })]);

    expect(arrivedFrom()).toEqual(['Arrived from Nassau']);
  });
});

describe('ShipsComponent (A ship keeps its Home Harbor)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices: [] }, catains: { catains: [], loading: false, error: null } } }),
        {
          provide: ShipService,
          useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']),
        },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const homeHarbor = (): string[] =>
    all('ship-home-harbor').map((line) => line.textContent!.replace(/\s+/g, ' ').trim());

  const arrivedFrom = (): string[] =>
    all('ship-arrived-from').map((badge) => badge.textContent!.trim());

  it('A newly registered ship has this Harbor as its Home Harbor', () => {
    render([anAvailableShip({ name: 'Salty Whisker', homeHarbor: 'Port Royal', arrivedFrom: null })]);

    expect(homeHarbor().map((text) => text.replace(/^home\s*/, ''))).toEqual(['Home Harbor Port Royal']);
    expect(all('ship-arrived-from')).toEqual([]);
  });

  it('The Home Harbor does not change when the ship sails elsewhere', () => {
    render([anAvailableShip({ homeHarbor: 'Port Royal', arrivedFrom: 'Port Royal' })]);

    expect(homeHarbor().map((text) => text.replace(/^home\s*/, ''))).toEqual(['Home Harbor Port Royal']);
  });

  it('The Home Harbor survives several voyages', () => {
    render([anAvailableShip({ homeHarbor: 'Port Royal', arrivedFrom: 'Tortuga' })]);

    expect(homeHarbor().map((text) => text.replace(/^home\s*/, ''))).toEqual(['Home Harbor Port Royal']);
    expect(arrivedFrom()).toEqual(['Arrived from Tortuga']);
  });
});

describe('ShipsComponent (A ship has a Ship Class)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices: [] }, catains: { catains: [], loading: false, error: null } } }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const shipClassLines = (): string[] =>
    Array.from<HTMLElement>(fixture.nativeElement.querySelectorAll('[data-testid="ship-class"]'))
      .map((line) => line.textContent!.replace(/\s+/g, ' ').replace(/^\s*sailing\s*/, '').trim());

  it("Available Ships show each ship's Ship Class", () => {
    render([anAvailableShip({ name: 'Salty Whisker', shipClass: ShipClass.BRIG, crewCapacity: 4 })]);

    expect(shipClassLines()).toEqual(['Brig · Crew Capacity 4']);
  });

  it('Ships registered before Ship Classes existed are Sloops', () => {
    render([anAvailableShip({ name: 'Old Barnacle', shipClass: ShipClass.SLOOP, crewCapacity: 2 })]);

    expect(shipClassLines()).toEqual(['Sloop · Crew Capacity 2']);
  });

  it('A ship keeps its Ship Class at another Harbor', () => {
    render([anAvailableShip({ shipClass: ShipClass.BRIG, crewCapacity: 4, arrivedFrom: 'Port Royal' })]);

    expect(shipClassLines()).toEqual(['Brig · Crew Capacity 4']);
  });
});

describe('ShipsComponent (A Harbor sees ships arrive and leave as they happen)', () => {
  let fixture: ComponentFixture<ShipsComponent>;
  let store: MockStore;

  function render(ships: Ship[], arrivalNotices: ArrivalNotice[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices }, catains: { catains: [], loading: false, error: null } } }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const statusRegion = (): HTMLElement => all('ships-arrival-notices')[0];

  it('An arriving ship appears without a reload', () => {
    render([anAvailableShip({ arrivedFrom: 'Tortuga' })], [anArrivalNotice()]);

    expect(store.dispatch).toHaveBeenCalledWith(ShipActions.watchFleet());
    expect(all('ship-name').map((name) => name.textContent!.trim())).toEqual(['Black Pearl']);
    expect(statusRegion().getAttribute('role')).toBe('status');
    expect(statusRegion().getAttribute('aria-live')).toBe('polite');
    const notices = Array.from(statusRegion().querySelectorAll('[data-testid="ships-arrival-notice"]'));
    expect(notices.map((notice) => notice.textContent!.trim())).toEqual(['Black Pearl arrived from Tortuga']);

    fixture.destroy();
    expect(store.dispatch).toHaveBeenCalledWith(ShipActions.stopWatchingFleet());
  });

  it('Ships sailing to another Harbor change nothing', () => {
    render([anAvailableShip()], []);

    expect(statusRegion()).toBeTruthy();
    expect(statusRegion().textContent!.trim()).toBe('');
    expect(all('ships-arrival-notice')).toEqual([]);
    expect(all('ship-name').map((name) => name.textContent!.trim())).toEqual(['Black Pearl']);
  });

  it('dismissing a notice dispatches dismissArrivalNotice for its ship', () => {
    render([anAvailableShip()], [anArrivalNotice()]);

    all('ships-arrival-notice-dismiss')[0].click();

    expect(store.dispatch).toHaveBeenCalledWith(
      ShipActions.dismissArrivalNotice({ shipId: anArrivalNotice().shipId }),
    );
  });

  const crewMember = (id: number, name: string, trait: string) => ({
    id: `c0ffee00-0000-4000-8000-00000000000${id}`,
    name,
    trait,
    portrait: { fur: 'cream', coat: 'spotted', eyes: 'blue', accessory: 'earring' } as const,
    refund: '25.00',
  });

  it('The Crew arrives with the ship', () => {
    const saltyWhisker = anAvailableShip({
      id: '5f1d7c2e-0000-4000-8000-000000000002',
      name: 'Salty Whisker',
      arrivedFrom: 'Port Royal',
      crewCapacity: 4,
      crew: [crewMember(1, 'Mittens', 'Strong Paws'), crewMember(2, 'Patch', 'Sharp Eyes')],
    });
    render(
      [saltyWhisker],
      [anArrivalNotice({ shipId: saltyWhisker.id, shipName: 'Salty Whisker', originHarbor: 'Port Royal', crew: ['Mittens', 'Patch'] })],
    );

    const notices = Array.from(statusRegion().querySelectorAll('[data-testid="ships-arrival-notice"]'));
    expect(notices.map((notice) => notice.textContent!.trim())).toEqual([
      'Salty Whisker arrived from Port Royal with its Crew: Mittens and Patch',
    ]);
    expect(all('ship-crew-count').map((count) => count.textContent!.replace(/\s+/g, ' ').trim())).toEqual(['Crew 2 / 4']);
    expect(all('ship-crew-names').map((names) => names.textContent!.trim())).toEqual(['Mittens, Patch']);
  });

  it('A Crew Member is shown at the Harbor with the portrait it came with', () => {
    render([
      anAvailableShip({
        name: 'Salty Whisker',
        arrivedFrom: 'Tortuga',
        crewCapacity: 4,
        crew: [crewMember(1, 'Mittens', 'Strong Paws'), crewMember(2, 'Patch', 'Sharp Eyes')],
      }),
    ], []);

    const svgs = Array.from(fixture.nativeElement.querySelectorAll('[data-testid="ship-crew-portraits"] svg')) as SVGElement[];
    expect(svgs.map((svg) => svg.getAttribute('aria-hidden'))).toEqual(['true', 'true']);
    expect(svgs.map((svg) => svg.getAttribute('data-fur'))).toEqual(['cream', 'cream']);
    expect(fixture.nativeElement.querySelector('[data-testid="ship-crew-count"] mat-icon')!.getAttribute('fonticon')).toBe('groups');
  });

  it('A ship returning to a Harbor shows its current Crew', () => {
    render([
      anAvailableShip({
        name: 'Salty Whisker',
        arrivedFrom: 'Tortuga',
        crewCapacity: 4,
        crew: [crewMember(1, 'Mittens', 'Strong Paws'), crewMember(2, 'Patch', 'Sharp Eyes'), crewMember(3, 'Saffron', 'Quick Claws')],
      }),
    ], []);

    expect(all('ship-crew-names').map((names) => names.textContent!.trim())).toEqual(['Mittens, Patch, Saffron']);
  });
});

describe('ShipsComponent (ship card)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[], catains: Catain[] = []): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            ships: { ships, loading: false, error: null, arrivalNotices: [] },
            catains: { catains, loading: false, error: null },
          },
        }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const one = (testId: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${testId}"]`);

  const chipText = (state: ShippingState | null): string => {
    render([anAvailableShip({ shippingState: state })]);
    return one('ship-status')!.textContent!.trim();
  };

  it('shows In port for IDLE', () => expect(chipText(ShippingState.IDLE)).toBe('In port'));

  it('shows In port for DONE', () => expect(chipText(ShippingState.DONE)).toBe('In port'));

  it('shows Preparing for PREPARING', () => expect(chipText(ShippingState.PREPARING)).toBe('Preparing'));

  it('shows At sea for SHIPPING', () => expect(chipText(ShippingState.SHIPPING)).toBe('At sea'));

  it('shows the Catain Image of a known Catain with its name as alt text', () => {
    render([anAvailableShip()], [{ id: 'cat-1', name: 'Furry Jones' }]);

    const img = one('ship-catain-image') as HTMLImageElement;
    expect(img.getAttribute('src')).toMatch(/\/catains\/cat-1\/image$/);
    expect(img.getAttribute('alt')).toBe('Furry Jones');
    expect(one('ship-catain-fallback')).toBeNull();
    expect(one('ship-catain-name')!.textContent).toContain('under Furry Jones');
  });

  it('falls back to an icon for an unknown Catain', () => {
    render([anAvailableShip()], []);

    expect(one('ship-catain-image')).toBeNull();
    expect(one('ship-catain-fallback')).toBeTruthy();
  });

  it('keeps the ship picture decorative', () => {
    render([anAvailableShip()]);

    expect(one('ship-picture')!.getAttribute('alt')).toBe('');
  });
});

describe('ShipsComponent (An arriving ship becomes an Incoming Ship)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            ships: { ships, loading: false, error: null, arrivalNotices: [] },
            catains: { catains: [], loading: false, error: null },
          },
        }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  it('An Incoming Ship cannot get a new Shipping', () => {
    render([anIncomingShip({ shippingState: ShippingState.IDLE })]);

    expect(all('new-shipping')).toEqual([]);
    expect(all('ship-incoming').map((note) => note.textContent!.trim())).toEqual([
      'Salty Whisker must be unloaded or refused first',
    ]);
  });

  it('A ship arriving with no Cargo joins the fleet as an ordinary ship', () => {
    render([anAvailableShip({ name: 'Empty Tabby', shippingState: ShippingState.IDLE, incoming: false })]);

    expect(all('new-shipping').length).toBe(1);
    expect(all('new-shipping')[0].textContent).toContain('New shipping');
    expect(all('ship-incoming')).toEqual([]);
  });
});

describe('ShipsComponent (Unloading earns the ship its Earnings)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices: [] }, catains: { catains: [], loading: false, error: null } } }),
        {
          provide: ShipService,
          useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']),
        },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const earnings = (): string[] =>
    Array.from<HTMLElement>(fixture.nativeElement.querySelectorAll('[data-testid="ship-earnings"]')).map((line) =>
      line.textContent!.replace(/\s+/g, ' ').trim().replace(/^payments\s*/, ''),
    );

  it('shows the Earnings a ship carries', () => {
    render([anAvailableShip({ name: 'Salty Whisker', earnings: '80.00' })]);

    expect(earnings()).toEqual(['Earnings 80.00 $']);
  });

  it('shows collected Earnings as the backend reports them', () => {
    render([anAvailableShip({ name: 'Salty Whisker', earnings: '120.00' })]);

    expect(earnings()).toEqual(['Earnings 120.00 $']);
  });

  it('shows no Earnings for a ship that carries none', () => {
    render([anAvailableShip({ name: 'Salty Whisker', earnings: '0.00' })]);

    expect(earnings()).toEqual([]);
  });
});

describe('ShipsComponent (A Harbor carries out an imprisonment)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(
    ships: Ship[],
    state: {
      arrivalNotices?: ArrivalNotice[];
      imprisonmentNotices?: ImprisonmentNotice[];
      newShippingRefusal?: { shipId: string; message: string } | null;
    } = {},
  ): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            ships: {
              ships,
              loading: false,
              error: null,
              arrivalNotices: state.arrivalNotices ?? [],
              imprisonmentNotices: state.imprisonmentNotices ?? [],
              newShippingRefusal: state.newShippingRefusal ?? null,
            },
            catains: { catains: [], loading: false, error: null },
          },
        }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const texts = (testId: string): string[] => all(testId).map((element) => element.textContent!.trim());

  it('The User is told when the Coast Guard imprisons a ship here', () => {
    render([anIncomingShip({ shippingState: ShippingState.SHIPPING, imprisoned: true })], {
      imprisonmentNotices: [anImprisonmentNotice()],
    });

    expect(texts('ships-imprisonment-notice')).toEqual([
      'The Coast Guard imprisoned Salty Whisker: its Cargo is confiscated and it sails home to Tortuga',
    ]);
  });

  it('At its Home Harbor the ship is imprisoned in place', () => {
    render([anIncomingShip({ incoming: false, imprisoned: true, homeHarbor: 'Tortuga', shippingState: null })]);

    expect(texts('ship-status')).toEqual(['Imprisoned']);
    expect(all('new-shipping')).toEqual([]);
  });

  it('A ship sent home imprisoned is imprisoned on its Arrival', () => {
    render([anIncomingShip({ incoming: false, imprisoned: true, arrivedFrom: 'Port Royal' })], {
      arrivalNotices: [
        anArrivalNotice({
          shipId: anIncomingShip().id,
          shipName: 'Salty Whisker',
          originHarbor: 'Port Royal',
          imprisoned: true,
        }),
      ],
    });

    expect(texts('ships-arrival-notice')).toEqual(['Salty Whisker arrived from Port Royal, imprisoned']);
  });

  it('An Imprisoned Ship cannot get a new Shipping', () => {
    const saltyWhisker = anIncomingShip({ incoming: false, imprisoned: true, shippingState: null });
    render([saltyWhisker], {
      newShippingRefusal: { shipId: saltyWhisker.id, message: 'Salty Whisker is imprisoned' },
    });

    expect(texts('new-shipping-refusal')).toEqual(['Salty Whisker is imprisoned']);
    expect(all('new-shipping-refusal')[0].getAttribute('role')).toBe('alert');
  });
});

describe('ShipsComponent (an Imprisoned Ship on the fleet page)', () => {
  let fixture: ComponentFixture<ShipsComponent>;
  let store: MockStore;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            ships: { ships, loading: false, error: null, arrivalNotices: [], imprisonmentNotices: [], newShippingRefusal: null, shipClasses: [] },
            catains: { catains: [], loading: false, error: null },
          },
        }),
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  it('a ship at sea home as an imprisoned ship shows "At sea", not "Imprisoned"', () => {
    render([anAvailableShip({ imprisoned: true, shippingState: ShippingState.SHIPPING })]);

    expect(all('ship-status').map((status) => status.textContent!.trim())).toEqual(['At sea']);
  });

  it('New shipping asks the store to start the new Shipping', () => {
    const blackPearl = anAvailableShip({ shippingState: ShippingState.IDLE });
    render([blackPearl]);

    all('new-shipping')[0].click();

    expect(store.dispatch).toHaveBeenCalledWith(ShipActions.createShipping({ ship: blackPearl }));
  });
});

describe('ShipsComponent (Free an Imprisoned Ship by paying its Fine)', () => {
  let fixture: ComponentFixture<ShipsComponent>;
  let store: MockStore;

  const saltyWhiskerImprisoned = () => anIncomingShip({ incoming: false, imprisoned: true, shippingState: null });

  function render(ships: Ship[], harbor: Record<string, unknown> = {}): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            ships: { ships, loading: false, error: null, arrivalNotices: [], imprisonmentNotices: [], newShippingRefusal: null, shipClasses: [] },
            catains: { catains: [], loading: false, error: null },
            harbor: {
              stock: [], savings: aSavings(), incomingShips: [], loading: false, error: null,
              imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [],
              ...harbor,
            },
          },
        }),
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));
  const texts = (testId: string): string[] => all(testId).map((el) => el.textContent!.trim());

  it('The User sees whether the Savings cover the Fine', () => {
    render([saltyWhiskerImprisoned()], { imprisonedShips: [anImprisonedShip({ savingsCoverFine: true })] });
    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadImprisonedShips());
    expect(texts('ship-fine')[0]).toContain('Fine 50.00 $');
    expect(texts('ship-fine-coverage')).toEqual(['The Savings cover the Fine']);
    expect((all('ship-pay-fine')[0] as HTMLButtonElement).disabled).toBeFalse();
    TestBed.resetTestingModule();

    render([saltyWhiskerImprisoned()], {
      savings: aSavings({ amount: '30.00' }),
      imprisonedShips: [anImprisonedShip({ savingsCoverFine: false })],
    });
    expect(texts('ship-fine-coverage')).toEqual(['The Savings do not cover the Fine of 50.00 $']);
    expect((all('ship-pay-fine')[0] as HTMLButtonElement).disabled).toBeTrue();
  });

  it('Paying the Fine frees the ship', () => {
    const imprisoned = anImprisonedShip();
    render([saltyWhiskerImprisoned()], { imprisonedShips: [imprisoned] });
    (all('ship-pay-fine')[0] as HTMLButtonElement).click();
    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.payFine({ shipId: imprisoned.shipId }));
    TestBed.resetTestingModule();

    // once paid, the fleet shows the notice and the freed ship can get a new Shipping
    render([anIncomingShip({ incoming: false, imprisoned: false, shippingState: ShippingState.IDLE })], {
      fineNotice: aFinePayment(),
    });
    expect(texts('ships-fine-notice')[0]).toContain(
      'Salty Whisker is free: the Fine of 50.00 $ is paid, the Savings are 950.00 $',
    );
    expect(all('ship-fine')).toEqual([]);
    expect(all('new-shipping').length).toBe(1);
  });
});

describe('ShipsComponent (arrival text and Crew row)', () => {
  let fixture: ComponentFixture<ShipsComponent>;

  function render(ships: Ship[], arrivalNotices: ArrivalNotice[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null, arrivalNotices }, catains: { catains: [], loading: false, error: null } } }),
        { provide: ShipService, useValue: jasmine.createSpyObj<ShipService>('ShipService', ['createShipping']) },
      ],
    });
    spyOn(TestBed.inject(MockStore), 'dispatch');
    fixture = TestBed.createComponent(ShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  it('A ship without Crew shows no Crew row and keeps the old notice wording', () => {
    render([anAvailableShip({ crew: [] })], [anArrivalNotice({ crew: [] })]);

    expect(all('ship-crew-row')).toEqual([]);
    expect(all('ship-crew-count')).toEqual([]);
    expect(all('ship-crew-names')).toEqual([]);
    expect(all('ships-arrival-notice').map((n) => n.textContent!.trim())).toEqual(['Black Pearl arrived from Tortuga']);
  });

  it('An imprisoned Arrival with a Crew', () => {
    render([], [anArrivalNotice({ imprisoned: true, crew: ['Mittens'] })]);

    expect(all('ships-arrival-notice').map((n) => n.textContent!.trim())).toEqual([
      'Black Pearl arrived from Tortuga, imprisoned, with its Crew: Mittens',
    ]);
  });

  it('Three Crew Members are listed with commas and a final "and"', () => {
    render([], [anArrivalNotice({ crew: ['Mittens', 'Patch', 'Saffron'] })]);

    expect(all('ships-arrival-notice')[0].textContent!.trim()).toBe(
      'Black Pearl arrived from Tortuga with its Crew: Mittens, Patch and Saffron',
    );
  });
});
