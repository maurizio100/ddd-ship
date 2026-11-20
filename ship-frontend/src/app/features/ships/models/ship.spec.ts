import { shipStatusLabel, ShippingState } from './ship';
import { anAvailableShip } from '../../../../testing/fixtures';

describe('shipStatusLabel', () => {
  it('words the status of a ship', () => {
    const label = (overrides: object) => shipStatusLabel(anAvailableShip(overrides) as never);

    expect(label({ shippingState: null })).toBe('In port');
    expect(label({ shippingState: ShippingState.PREPARING })).toBe('Preparing');
    expect(label({ shippingState: ShippingState.SHIPPING })).toBe('At sea');
    expect(label({ shippingState: ShippingState.IDLE, imprisoned: true })).toBe('Imprisoned');
    expect(label({ shippingState: ShippingState.SHIPPING, imprisoned: true })).toBe('At sea');
  });
});
