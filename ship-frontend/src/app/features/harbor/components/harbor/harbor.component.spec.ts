import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { HarborComponent } from './harbor.component';
import { StockedCargo } from '../../models/stocked-cargo';
import * as HarborActions from '../../store/harbor.actions';
import { aStockedCargo } from '../../../../../testing/fixtures';

describe('HarborComponent (The harbor management page shows the Harbor\'s Stock)', () => {
  let fixture: ComponentFixture<HarborComponent>;
  let store: MockStore;

  function render(stock: StockedCargo[]): void {
    TestBed.configureTestingModule({
      imports: [HarborComponent],
      providers: [provideMockStore({ initialState: { harbor: { stock, loading: false, error: null } } })],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(HarborComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const rows = (): { name: string; quantity: string }[] =>
    all('harbor-stock-row').map((row) => ({
      name: row.querySelector('[data-testid="harbor-stock-cargo-name"]')!.textContent!.trim(),
      quantity: row.querySelector('[data-testid="harbor-stock-quantity"]')!.textContent!.trim(),
    }));

  it('shows every Cargo of the catalog with its Stock', () => {
    render([
      aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000001', name: 'Ale', quantity: 3 }),
      aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000002', name: 'Rum', quantity: 3 }),
      aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000003', name: 'Silk', quantity: 3 }),
    ]);

    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadStock());
    expect(rows()).toEqual([
      { name: 'Ale', quantity: '3' },
      { name: 'Rum', quantity: '3' },
      { name: 'Silk', quantity: '3' },
    ]);
  });

  it('shows Rum with a Stock of 0', () => {
    render([
      aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000001', name: 'Ale', quantity: 3 }),
      aStockedCargo({ name: 'Rum', quantity: 0 }),
    ]);

    expect(rows()).toEqual([
      { name: 'Ale', quantity: '3' },
      { name: 'Rum', quantity: '0' },
    ]);
  });

  it('shows the quantity in the store', () => {
    render([aStockedCargo({ name: 'Rum', quantity: 2 })]);

    expect(rows()).toEqual([{ name: 'Rum', quantity: '2' }]);
  });
});
