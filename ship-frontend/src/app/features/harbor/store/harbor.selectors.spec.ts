import {
  selectIncomingShips,
  selectPurchaseRefusal,
  selectSavings,
  selectStock,
  selectStockError,
  selectStockLoading,
  selectUnloadRefusal,
} from './harbor.selectors';
import { aSavings, aStockedCargo, anIncomingShipListing } from '../../../../testing/fixtures';

describe('harbor selectors', () => {
  const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];
  const savings = aSavings({ amount: '640.50' });
  const incomingShips = [anIncomingShipListing()];
  const root = {
    harbor: {
      stock,
      savings,
      incomingShips,
      loading: true,
      error: 'boom',
      purchaseRefusal: 'The Savings do not cover 100.00 $',
      unloadRefusal: 'The Savings do not cover the Delivery Price of 115.00 $',
    },
  };

  it('selectStock reads stock', () => expect(selectStock(root)).toEqual(stock));
  it('selectStockLoading reads loading', () => expect(selectStockLoading(root)).toBeTrue());
  it('selectStockError reads error', () => expect(selectStockError(root)).toBe('boom'));
  it('selectSavings reads savings', () => expect(selectSavings(root)).toEqual(savings));
  it('selectPurchaseRefusal reads the purchase refusal', () =>
    expect(selectPurchaseRefusal(root)).toBe('The Savings do not cover 100.00 $'));
  it('selectUnloadRefusal reads the unload refusal', () =>
    expect(selectUnloadRefusal(root)).toBe('The Savings do not cover the Delivery Price of 115.00 $'));
  it('selectIncomingShips reads the Incoming Ships', () => expect(selectIncomingShips(root)).toEqual(incomingShips));
});
