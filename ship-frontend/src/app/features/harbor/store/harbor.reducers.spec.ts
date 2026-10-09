import { harborReducers, initialState } from './harbor.reducers';
import * as HarborActions from './harbor.actions';
import { aSavings, aStockedCargo, anIncomingShipListing } from '../../../../testing/fixtures';

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

    expect(state).toEqual({ stock, savings: null, incomingShips: [], loading: false, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] });
  });

  it('loadStockFailure stores the error, keeps the stock and stops loading', () => {
    const stock = [aStockedCargo()];

    const state = harborReducers(
      { stock, savings: null, incomingShips: [], loading: true, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] },
      HarborActions.loadStockFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock, savings: null, incomingShips: [], loading: false, error: 'boom', purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] });
  });

  it('loadSavings sets loading and clears a previous error', () => {
    const state = harborReducers({ ...initialState, error: 'boom' }, HarborActions.loadSavings());

    expect(state.loading).toBeTrue();
    expect(state.error).toBeNull();
  });

  it('loadSavingsSuccess sets the Savings and stops loading', () => {
    const savings = aSavings({ amount: '640.50' });

    const state = harborReducers({ ...initialState, loading: true }, HarborActions.loadSavingsSuccess({ savings }));

    expect(state).toEqual({ stock: [], savings, incomingShips: [], loading: false, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] });
  });

  it('loadSavingsFailure stores the error, keeps the Savings and stops loading', () => {
    const savings = aSavings();

    const state = harborReducers(
      { stock: [], savings, incomingShips: [], loading: true, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] },
      HarborActions.loadSavingsFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock: [], savings, incomingShips: [], loading: false, error: 'boom', purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [] });
  });

  it('starts without a purchase refusal', () => {
    expect(initialState.purchaseRefusal).toBeNull();
  });

  it('buyCargoFailure stores the refusal', () => {
    const state = harborReducers(
      initialState,
      HarborActions.buyCargoFailure({ error: 'boom', refusal: 'The Savings do not cover 100.00 $' }),
    );

    expect(state.purchaseRefusal).toBe('The Savings do not cover 100.00 $');
  });

  it('buyCargo clears a previous refusal', () => {
    const state = harborReducers(
      { ...initialState, purchaseRefusal: 'The Savings do not cover 100.00 $' },
      HarborActions.buyCargo({ cargoId: 'c0a8f3a2-0000-4000-8000-000000000001', quantity: 2 }),
    );

    expect(state.purchaseRefusal).toBeNull();
  });

  it('buyCargoSuccess clears a previous refusal', () => {
    const state = harborReducers(
      { ...initialState, purchaseRefusal: 'The Savings do not cover 100.00 $' },
      HarborActions.buyCargoSuccess(),
    );

    expect(state.purchaseRefusal).toBeNull();
  });

  it('starts without Incoming Ships', () => {
    expect(initialState.incomingShips).toEqual([]);
  });

  it('loadIncomingShipsSuccess stores the Incoming Ships', () => {
    const incomingShips = [anIncomingShipListing()];

    const state = harborReducers(initialState, HarborActions.loadIncomingShipsSuccess({ incomingShips }));

    expect(state.incomingShips).toEqual(incomingShips);
  });

  it('loadIncomingShipsFailure stores the error and keeps the Incoming Ships', () => {
    const incomingShips = [anIncomingShipListing()];

    const state = harborReducers(
      { ...initialState, incomingShips },
      HarborActions.loadIncomingShipsFailure({ error: 'boom' }),
    );

    expect(state.incomingShips).toEqual(incomingShips);
    expect(state.error).toBe('boom');
  });

  it('starts without an unload refusal', () => {
    expect(initialState.unloadRefusal).toBeNull();
  });

  it('unloadIncomingShipFailure stores the refusal', () => {
    const state = harborReducers(
      initialState,
      HarborActions.unloadIncomingShipFailure({
        shipId: 'b1a2c3d4-0000-4000-8000-000000000001',
        error: 'boom',
        refusal: 'The Savings do not cover the Delivery Price of 115.00 $',
      }),
    );

    expect(state.unloadRefusal).toBe('The Savings do not cover the Delivery Price of 115.00 $');
  });

  it('unloadIncomingShip clears a previous refusal', () => {
    const state = harborReducers(
      { ...initialState, unloadRefusal: 'The Savings do not cover the Delivery Price of 115.00 $' },
      HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );

    expect(state.unloadRefusal).toBeNull();
  });

  it('unloadIncomingShipSuccess clears a previous refusal', () => {
    const state = harborReducers(
      { ...initialState, unloadRefusal: 'The Savings do not cover the Delivery Price of 115.00 $' },
      HarborActions.unloadIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );

    expect(state.unloadRefusal).toBeNull();
  });

  it('unloadIncomingShip marks the ship as being unloaded until it succeeds', () => {
    const started = harborReducers(initialState, HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));
    expect(started.unloadingShipIds).toEqual(['b1a2c3d4-0000-4000-8000-000000000001']);

    const done = harborReducers(started, HarborActions.unloadIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }));
    expect(done.unloadingShipIds).toEqual([]);
  });

  it('unloadIncomingShipFailure ends the unloading of that ship only', () => {
    const state = harborReducers(
      { ...initialState, unloadingShipIds: ['b1a2c3d4-0000-4000-8000-000000000001', 'other'] },
      HarborActions.unloadIncomingShipFailure({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001', error: 'boom', refusal: 'nope' }),
    );

    expect(state.unloadingShipIds).toEqual(['other']);
  });
});
