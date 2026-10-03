import { AvailableCargo, Cargo } from '../app/features/shipping/models/cargo';
import { Ship, ShippingState } from '../app/features/shipping/models/ship';

export function aCargo(overrides: Partial<Cargo> = {}): Cargo {
  return {
    id: 'c0a8f3a2-0000-4000-8000-000000000009',
    name: 'Rum',
    weight: 5.5,
    ...overrides,
  };
}

export function anAvailableCargo(overrides: Partial<AvailableCargo> = {}): AvailableCargo {
  return {
    ...aCargo(),
    stock: 3,
    ...overrides,
  };
}

export function aShip(overrides: Partial<Ship> = {}): Ship {
  return {
    id: '5f1d7c2e-0000-4000-8000-000000000001',
    name: 'Black Pearl',
    catain: 'Furry Jones',
    cargo: [],
    weight: 0,
    maxweight: 15,
    shippingState: ShippingState.PREPARING,
    ...overrides,
  };
}
