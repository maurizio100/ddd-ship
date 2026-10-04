import { initialState, shipReducers } from './ship.reducers';
import * as ShipActions from '../actions/ship.actions';
import { ShipState } from '../selectors/ship.selectors';
import { anArrivalNotice, anAvailableShip } from '../../../../../testing/fixtures';

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
