import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { HarborComponent } from './harbor.component';
import { StockedCargo } from '../../models/stocked-cargo';
import { Savings } from '../../models/savings';
import * as HarborActions from '../../store/harbor.actions';
import { aSavings, aStockedCargo } from '../../../../../testing/fixtures';

describe('HarborComponent (The harbor management page shows the Harbor\'s Stock)', () => {
  let fixture: ComponentFixture<HarborComponent>;
  let store: MockStore;

  function render(
    stock: StockedCargo[],
    savings: Savings | null = null,
    purchaseRefusal: string | null = null,
  ): void {
    TestBed.configureTestingModule({
      imports: [HarborComponent],
      providers: [
        provideMockStore({
          initialState: { harbor: { stock, savings, loading: false, error: null, purchaseRefusal } },
        }),
      ],
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

  const text = (testId: string): string | undefined => all(testId)[0]?.textContent?.trim();

  it('A Harbor opened for the first time holds its Starting Savings', () => {
    render([aStockedCargo()], aSavings({ amount: '1000.00' }));

    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadSavings());
    expect(text('harbor-savings')).toBe('1000.00 $');
  });

  it('A Harbor opened for the first time rolls a Price for every Cargo', () => {
    render(
      [
        aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000001', name: 'Ale', price: '42.00' }),
        aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000002', name: 'Rum', price: '57.00' }),
        aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000003', name: 'Silk', price: '30.00' }),
      ],
      aSavings(),
    );

    const prices = all('harbor-stock-row').map((row) =>
      row.querySelector('[data-testid="harbor-stock-price"]')?.textContent?.trim(),
    );
    expect(prices).toEqual(['42.00 $', '57.00 $', '30.00 $']);
  });

  it('A Harbor opening again keeps its Savings', () => {
    render([aStockedCargo()], aSavings({ amount: '640.50' }));

    expect(text('harbor-savings')).toBe('640.50 $');
  });

  it('shows a dash for a Cargo without a Price', () => {
    render([aStockedCargo({ name: 'Rum', price: null })], aSavings());

    expect(all('harbor-stock-row')[0].querySelector('[data-testid="harbor-stock-price"]')?.textContent?.trim()).toBe('—');
  });

  it('Buying Cargo adds it to the Stock and pays from the Savings', () => {
    const ale = aStockedCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000001', name: 'Ale', quantity: 1, price: '50.00' });
    render([ale], aSavings({ amount: '150.00' }));

    // When the User buys 2 Ale at the Market
    const input = all('harbor-buy-quantity')[0] as HTMLInputElement;
    input.value = '2';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    (all('harbor-buy')[0] as HTMLButtonElement).click();

    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.buyCargo({ cargoId: ale.cargoId, quantity: 2 }));

    // Then, once the harbor page has refreshed, the Stock holds 3 Ale and the Savings are 50.00 $
    store.setState({
      harbor: {
        stock: [{ ...ale, quantity: 3 }],
        savings: aSavings({ amount: '50.00' }),
        loading: false,
        error: null,
        purchaseRefusal: null,
      },
    });
    fixture.detectChanges();
    expect(rows()).toEqual([{ name: 'Ale', quantity: '3' }]);
    expect(text('harbor-savings')).toBe('50.00 $');
  });

  it('A purchase the Savings cannot cover is refused', () => {
    render(
      [aStockedCargo({ name: 'Ale', quantity: 1, price: '50.00' })],
      aSavings({ amount: '80.00' }),
      'The Savings do not cover 100.00 $',
    );

    expect(text('harbor-purchase-refusal')).toBe('The Savings do not cover 100.00 $');
    expect(rows()).toEqual([{ name: 'Ale', quantity: '1' }]);
    expect(text('harbor-savings')).toBe('80.00 $');
  });
});
