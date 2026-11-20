import { harborReducers, initialState } from './harbor.reducers';
import * as HarborActions from './harbor.actions';
import { aFinePayment, aSavings, aStockedCargo, anImprisonedShip, anIncomingShipListing } from '../../../../testing/fixtures';

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

    expect(state).toEqual({ stock, savings: null, incomingShips: [], recruitPool: [], loading: false, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] });
  });

  it('loadStockFailure stores the error, keeps the stock and stops loading', () => {
    const stock = [aStockedCargo()];

    const state = harborReducers(
      { stock, savings: null, incomingShips: [], recruitPool: [], loading: true, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] },
      HarborActions.loadStockFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock, savings: null, incomingShips: [], recruitPool: [], loading: false, error: 'boom', purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] });
  });

  it('loadSavings sets loading and clears a previous error', () => {
    const state = harborReducers({ ...initialState, error: 'boom' }, HarborActions.loadSavings());

    expect(state.loading).toBeTrue();
    expect(state.error).toBeNull();
  });

  it('loadSavingsSuccess sets the Savings and stops loading', () => {
    const savings = aSavings({ amount: '640.50' });

    const state = harborReducers({ ...initialState, loading: true }, HarborActions.loadSavingsSuccess({ savings }));

    expect(state).toEqual({ stock: [], savings, incomingShips: [], recruitPool: [], loading: false, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] });
  });

  it('loadSavingsFailure stores the error, keeps the Savings and stops loading', () => {
    const savings = aSavings();

    const state = harborReducers(
      { stock: [], savings, incomingShips: [], recruitPool: [], loading: true, error: null, purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] },
      HarborActions.loadSavingsFailure({ error: 'boom' }),
    );

    expect(state).toEqual({ stock: [], savings, incomingShips: [], recruitPool: [], loading: false, error: 'boom', purchaseRefusal: null, unloadRefusal: null, unloadingShipIds: [], refuseFailure: null, refusingShipIds: [], imprisonedShips: [], fineNotice: null, fineRefusal: null, payingFineShipIds: [] });
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

  it('starts without a refuse failure and with no refusal in flight', () => {
    expect(initialState.refuseFailure).toBeNull();
    expect(initialState.refusingShipIds).toEqual([]);
  });

  it('refuseIncomingShip marks the ship as being refused and clears a previous failure, until it succeeds', () => {
    const started = harborReducers(
      { ...initialState, refuseFailure: 'nope' },
      HarborActions.refuseIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );
    expect(started.refusingShipIds).toEqual(['b1a2c3d4-0000-4000-8000-000000000001']);
    expect(started.refuseFailure).toBeNull();

    const done = harborReducers(
      { ...started, refuseFailure: 'nope' },
      HarborActions.refuseIncomingShipSuccess({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );
    expect(done.refusingShipIds).toEqual([]);
    expect(done.refuseFailure).toBeNull();
  });

  it('refuseIncomingShipFailure stores the failure and ends the refusal of that ship only', () => {
    const state = harborReducers(
      { ...initialState, refusingShipIds: ['b1a2c3d4-0000-4000-8000-000000000001', 'other'] },
      HarborActions.refuseIncomingShipFailure({
        shipId: 'b1a2c3d4-0000-4000-8000-000000000001',
        error: 'boom',
        refusal: 'Salty Whisker is not an Incoming Ship anymore',
      }),
    );

    expect(state.refusingShipIds).toEqual(['other']);
    expect(state.refuseFailure).toBe('Salty Whisker is not an Incoming Ship anymore');
  });

  it('refuseIncomingShip clears a previous unload refusal', () => {
    const state = harborReducers(
      { ...initialState, unloadRefusal: 'The Savings do not cover the Delivery Price of 115.00 $' },
      HarborActions.refuseIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );

    expect(state.unloadRefusal).toBeNull();
  });

  it('unloadIncomingShip clears a previous refuse failure', () => {
    const state = harborReducers(
      { ...initialState, refuseFailure: 'Salty Whisker is not an Incoming Ship anymore' },
      HarborActions.unloadIncomingShip({ shipId: 'b1a2c3d4-0000-4000-8000-000000000001' }),
    );

    expect(state.refuseFailure).toBeNull();
  });

  describe('the Fine', () => {
    const shipId = '5f1d7c2e-0000-4000-8000-000000000002';

    it('starts with no Imprisoned Ships, no notice, no refusal and no payment in flight', () => {
      expect(initialState.imprisonedShips).toEqual([]);
      expect(initialState.fineNotice).toBeNull();
      expect(initialState.fineRefusal).toBeNull();
      expect(initialState.payingFineShipIds).toEqual([]);
    });

    it('loadImprisonedShipsSuccess stores the Imprisoned Ships', () => {
      const state = harborReducers(initialState, HarborActions.loadImprisonedShipsSuccess({ imprisonedShips: [anImprisonedShip()] }));

      expect(state.imprisonedShips).toEqual([anImprisonedShip()]);
    });

    it('payFine marks the payment in flight and clears an earlier refusal', () => {
      const state = harborReducers(
        { ...initialState, fineRefusal: { shipId, message: 'nope' } },
        HarborActions.payFine({ shipId }),
      );

      expect(state.payingFineShipIds).toEqual([shipId]);
      expect(state.fineRefusal).toBeNull();
    });

    it('payFineSuccess sets the notice and ends the payment', () => {
      const state = harborReducers(
        { ...initialState, payingFineShipIds: [shipId, 'other'] },
        HarborActions.payFineSuccess({ payment: aFinePayment() }),
      );

      expect(state.fineNotice).toEqual(aFinePayment());
      expect(state.payingFineShipIds).toEqual(['other']);
    });

    it('payFineFailure sets the refusal for the ship and ends the payment', () => {
      const state = harborReducers(
        { ...initialState, payingFineShipIds: [shipId] },
        HarborActions.payFineFailure({ shipId, error: 'boom', refusal: 'The Savings do not cover the Fine of 50.00 $' }),
      );

      expect(state.fineRefusal).toEqual({ shipId, message: 'The Savings do not cover the Fine of 50.00 $' });
      expect(state.payingFineShipIds).toEqual([]);
    });

    it('dismissFineNotice clears the notice', () => {
      const state = harborReducers({ ...initialState, fineNotice: aFinePayment() }, HarborActions.dismissFineNotice());

      expect(state.fineNotice).toBeNull();
    });
  });

  it('starts with an empty Recruit Pool', () => expect(initialState.recruitPool).toEqual([]));

  it('loadRecruitPoolSuccess stores the Recruit Pool', () => {
    const recruitPool = [{ id: 'r1', name: 'Mittens', trait: 'Sure-pawed' }];

    const state = harborReducers(initialState, HarborActions.loadRecruitPoolSuccess({ recruitPool }));

    expect(state.recruitPool).toEqual(recruitPool);
  });

  it('loadRecruitPoolFailure stores the error and keeps the Recruit Pool', () => {
    const recruitPool = [{ id: 'r1', name: 'Mittens', trait: 'Sure-pawed' }];

    const state = harborReducers({ ...initialState, recruitPool }, HarborActions.loadRecruitPoolFailure({ error: 'boom' }));

    expect(state.recruitPool).toEqual(recruitPool);
    expect(state.error).toBe('boom');
  });
});
