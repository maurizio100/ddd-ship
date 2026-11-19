import { initialState, shipReducers } from './ship.reducers';
import * as ShipActions from '../actions/ship.actions';
import { ShipState } from '../selectors/ship.selectors';
import { anArrivalNotice, anAvailableShip, anImprisonmentNotice } from '../../../../../testing/fixtures';

describe('shipReducers (A Harbor sees ships arrive and leave as they happen)', () => {
  const blackPearl = anAvailableShip();
  const flyingDutchman = anAvailableShip({ id: 'a7c1e9b3-0000-4000-8000-000000000004', name: 'Flying Dutchman' });

  const withShips = (): ShipState => ({ ...initialState, ships: [blackPearl, flyingDutchman] });

  it('A ship that arrived elsewhere leaves the fleet without a reload', () => {
    const state = shipReducers(
      withShips(),
      ShipActions.shipLeft({ shipId: blackPearl.id, shipName: 'Black Pearl', destinationHarbor: 'Port Royal' }),
    );

    expect(state.ships).toEqual([flyingDutchman]);
  });

  it('an arrival adds one notice per ship, a second arrival of the same ship replacing it', () => {
    const first = shipReducers(initialState, ShipActions.shipArrived(anArrivalNotice()));
    const other = shipReducers(
      first,
      ShipActions.shipArrived(anArrivalNotice({ shipId: flyingDutchman.id, shipName: 'Flying Dutchman' })),
    );
    const again = shipReducers(other, ShipActions.shipArrived(anArrivalNotice({ originHarbor: 'Nassau' })));

    expect(first.arrivalNotices).toEqual([anArrivalNotice()]);
    expect(again.arrivalNotices).toEqual([
      anArrivalNotice({ shipId: flyingDutchman.id, shipName: 'Flying Dutchman' }),
      anArrivalNotice({ originHarbor: 'Nassau' }),
    ]);
  });

  it('dismissing a notice removes it', () => {
    const flyingDutchmanNotice = anArrivalNotice({ shipId: flyingDutchman.id, shipName: 'Flying Dutchman' });
    const told: ShipState = { ...initialState, arrivalNotices: [anArrivalNotice(), flyingDutchmanNotice] };

    const state = shipReducers(told, ShipActions.dismissArrivalNotice({ shipId: anArrivalNotice().shipId }));

    expect(state.arrivalNotices).toEqual([flyingDutchmanNotice]);
  });
});

describe('shipReducers (A Harbor carries out an imprisonment)', () => {
  const saltyWhisker = { shipId: anImprisonmentNotice().shipId, shipName: 'Salty Whisker', homeHarbor: 'Tortuga' };

  it('an imprisonment of a ship that sails home adds one notice per ship', () => {
    const first = shipReducers(initialState, ShipActions.shipImprisoned({ ...saltyWhisker, sailsHome: true }));
    const again = shipReducers(first, ShipActions.shipImprisoned({ ...saltyWhisker, sailsHome: true }));

    expect(first.imprisonmentNotices).toEqual([anImprisonmentNotice()]);
    expect(again.imprisonmentNotices).toEqual([anImprisonmentNotice()]);
  });

  it('an imprisonment in place at the Home Harbor adds no notice', () => {
    const state = shipReducers(initialState, ShipActions.shipImprisoned({ ...saltyWhisker, sailsHome: false }));

    expect(state.imprisonmentNotices).toEqual([]);
  });

  it('dismissing an imprisonment notice removes it', () => {
    const other = anImprisonmentNotice({ shipId: 'other', shipName: 'Black Pearl' });
    const told: ShipState = { ...initialState, imprisonmentNotices: [anImprisonmentNotice(), other] };

    const state = shipReducers(told, ShipActions.dismissImprisonmentNotice({ shipId: anImprisonmentNotice().shipId }));

    expect(state.imprisonmentNotices).toEqual([other]);
  });

  it('an imprisoned Arrival keeps imprisoned in its notice', () => {
    const state = shipReducers(initialState, ShipActions.shipArrived(anArrivalNotice({ imprisoned: true })));

    expect(state.arrivalNotices).toEqual([anArrivalNotice({ imprisoned: true })]);
  });

  it('a refused new Shipping is kept until the next attempt or reload', () => {
    const ship = anAvailableShip({ imprisoned: true });
    const refused = shipReducers(
      initialState,
      ShipActions.createShippingFailure({ shipId: ship.id, error: 'Black Pearl is imprisoned' }),
    );

    expect(refused.newShippingRefusal).toEqual({ shipId: ship.id, message: 'Black Pearl is imprisoned' });
    expect(shipReducers(refused, ShipActions.createShipping({ ship })).newShippingRefusal).toBeNull();
    expect(shipReducers(refused, ShipActions.loadShipsSuccess({ ships: [ship] })).newShippingRefusal).toBeNull();
  });
});
