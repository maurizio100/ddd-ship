import { AvailableCargo, Cargo } from '../app/features/shipping/models/cargo';
import { KnownHarbors } from '../app/features/shipping/models/known-harbors';
import { Ship, ShippingState } from '../app/features/shipping/models/ship';
import { ShippingSummary } from '../app/features/shipping/models/shipping-summary';
import {
  Ship as AvailableShip,
  ShippingState as AvailableShipShippingState,
} from '../app/features/ships/models/ship';

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
    arrivedFrom: null,
    ...overrides,
  };
}

/** A ship as listed among the Available Ships (ships feature), with no Active Shipping. */
export function anAvailableShip(overrides: Partial<AvailableShip> = {}): AvailableShip {
  return {
    id: '5f1d7c2e-0000-4000-8000-000000000001',
    name: 'Black Pearl',
    catain: 'Furry Jones',
    shippingState: AvailableShipShippingState.IDLE,
    arrivedFrom: null,
    ...overrides,
  };
}

export function aShippingSummary(overrides: Partial<ShippingSummary> = {}): ShippingSummary {
  return {
    id: '9b2e4d6f-0000-4000-8000-000000000003',
    shipId: '5f1d7c2e-0000-4000-8000-000000000001',
    catainId: '3c4d5e6f-0000-4000-8000-000000000002',
    catainName: 'Furry Jones',
    name: 'Black Pearl',
    cargo: [],
    sailorsCode: 'Fair winds',
    weight: 0,
    destinationHarbor: 'Port Royal',
    ...overrides,
  };
}

export function someKnownHarbors(overrides: Partial<KnownHarbors> = {}): KnownHarbors {
  return {
    harborName: 'Tortuga',
    knownHarbors: ['Nassau', 'Port Royal'],
    ...overrides,
  };
}
