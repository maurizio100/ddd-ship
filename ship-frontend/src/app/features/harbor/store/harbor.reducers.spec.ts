import { harborReducers, initialState } from './harbor.reducers';
import * as HarborActions from './harbor.actions';
import { aStockedCargo } from '../../../../testing/fixtures';

describe('harborReducers', () => {
  it('loadStock sets loading and clears a previous error', () => {
    const state = harborReducers({ ...initialState, error: 'boom' }, HarborActions.loadStock());

    expect(state.loading).toBeTrue();
    expect(state.error).toBeNull();
  });

  it('loadStockSuccess fills the stock and stops loading', () => {
    const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];

    const state = harborReducers({ ...initialState, loading: true }, HarborActions.loadStockSuccess({ stock }));

    expect(state).toEqual({ stock, loading: false, error: null });
  });

  it('loadStockFailure stores the error, keeps the stock and stops loading', () => {
    const stock = [aStockedCargo()];

    const state = harborReducers(
      { stock, loading: true, error: null },
      HarborActions.loadStockFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock, loading: false, error: 'boom' });
  });
});
