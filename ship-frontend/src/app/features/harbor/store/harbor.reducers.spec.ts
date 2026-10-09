import { harborReducers, initialState } from './harbor.reducers';
import * as HarborActions from './harbor.actions';
import { aSavings, aStockedCargo } from '../../../../testing/fixtures';

describe('harborReducers', () => {
  it('starts without Savings', () => {
    expect(initialState.savings).toBeNull();
  });

  it('loadStock sets loading and clears a previous error', () => {
    const state = harborReducers({ ...initialState, error: 'boom' }, HarborActions.loadStock());

    expect(state.loading).toBeTrue();
    expect(state.error).toBeNull();
  });

  it('loadStockSuccess fills the stock and stops loading', () => {
    const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];

    const state = harborReducers({ ...initialState, loading: true }, HarborActions.loadStockSuccess({ stock }));

    expect(state).toEqual({ stock, savings: null, loading: false, error: null });
  });

  it('loadStockFailure stores the error, keeps the stock and stops loading', () => {
    const stock = [aStockedCargo()];

    const state = harborReducers(
      { stock, savings: null, loading: true, error: null },
      HarborActions.loadStockFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock, savings: null, loading: false, error: 'boom' });
  });

  it('loadSavings sets loading and clears a previous error', () => {
    const state = harborReducers({ ...initialState, error: 'boom' }, HarborActions.loadSavings());

    expect(state.loading).toBeTrue();
    expect(state.error).toBeNull();
  });

  it('loadSavingsSuccess sets the Savings and stops loading', () => {
    const savings = aSavings({ amount: '640.50' });

    const state = harborReducers({ ...initialState, loading: true }, HarborActions.loadSavingsSuccess({ savings }));

    expect(state).toEqual({ stock: [], savings, loading: false, error: null });
  });

  it('loadSavingsFailure stores the error, keeps the Savings and stops loading', () => {
    const savings = aSavings();

    const state = harborReducers(
      { stock: [], savings, loading: true, error: null },
      HarborActions.loadSavingsFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock: [], savings, loading: false, error: 'boom' });
  });
});
