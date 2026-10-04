import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { ShipsComponent } from './ships.component';
import { Ship, ShippingState } from '../../models/ship';
import { ShipService } from '../../services/ship.service';
import * as ShipActions from '../../store/actions/ship.actions';
import { anArrivalNotice, anAvailableShip } from '../../../../../testing/fixtures';
import { Catain } from '../../../catains/model/catain';
import { ArrivalNotice } from '../../models/fleet-event';

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
