import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { ShipsComponent } from './ships.component';
import { Ship, ShippingState } from '../../models/ship';
import { ShipService } from '../../services/ship.service';
import * as ShipActions from '../../store/actions/ship.actions';
import { anAvailableShip } from '../../../../../testing/fixtures';

describe('ShipsComponent (The voyage ends at the Origin Harbor)', () => {
  let fixture: ComponentFixture<ShipsComponent>;
  let store: MockStore;

  function render(ships: Ship[]): void {
    TestBed.configureTestingModule({
      imports: [ShipsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { ships: { ships, loading: false, error: null } } }),
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
