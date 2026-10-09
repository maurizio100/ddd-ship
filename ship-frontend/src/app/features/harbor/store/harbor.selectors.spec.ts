import { selectStock, selectStockError, selectStockLoading } from './harbor.selectors';
import { aStockedCargo } from '../../../../testing/fixtures';

describe('harbor selectors', () => {
  const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];
  const root = { harbor: { stock, loading: true, error: 'boom' } };

  it('selectStock reads stock', () => expect(selectStock(root)).toEqual(stock));
  it('selectStockLoading reads loading', () => expect(selectStockLoading(root)).toBeTrue());
  it('selectStockError reads error', () => expect(selectStockError(root)).toBe('boom'));
});
