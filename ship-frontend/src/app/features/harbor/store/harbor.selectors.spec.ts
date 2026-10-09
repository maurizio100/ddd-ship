import { selectPurchaseRefusal, selectSavings, selectStock, selectStockError, selectStockLoading } from './harbor.selectors';
import { aSavings, aStockedCargo } from '../../../../testing/fixtures';

describe('harbor selectors', () => {
  const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];
  const savings = aSavings({ amount: '640.50' });
  const root = { harbor: { stock, savings, loading: true, error: 'boom', purchaseRefusal: 'The Savings do not cover 100.00 $' } };

  it('selectStock reads stock', () => expect(selectStock(root)).toEqual(stock));
  it('selectStockLoading reads loading', () => expect(selectStockLoading(root)).toBeTrue());
  it('selectStockError reads error', () => expect(selectStockError(root)).toBe('boom'));
  it('selectSavings reads savings', () => expect(selectSavings(root)).toEqual(savings));
  it('selectPurchaseRefusal reads the purchase refusal', () =>
    expect(selectPurchaseRefusal(root)).toBe('The Savings do not cover 100.00 $'));
});
