import { AvailableCargo, Cargo } from '../app/features/shipping/models/cargo';
import { KnownHarbors } from '../app/features/shipping/models/known-harbors';
import { Ship, ShippingState } from '../app/features/shipping/models/ship';
import { ShippingSummary } from '../app/features/shipping/models/shipping-summary';
import { ArrivalNotice } from '../app/features/ships/models/fleet-event';
import { PurchaseRequest } from '../app/features/harbor/models/purchase-request';
import { Savings } from '../app/features/harbor/models/savings';
import { IncomingShip } from '../app/features/harbor/models/incoming-ship';
import { StockedCargo } from '../app/features/harbor/models/stocked-cargo';
import { Catain } from '../app/features/catains/model/catain';
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
    cargoAboard: [],
    weight: 0,
    maxweight: 15,
    shippingState: ShippingState.PREPARING,
    homeHarbor: 'Port Royal',
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
    homeHarbor: 'Port Royal',
    arrivedFrom: null,
    incoming: false,
    ...overrides,
  };
}

/** A ship that arrived with Cargo aboard: "Salty Whisker" from "Tortuga". */
export function anIncomingShip(overrides: Partial<AvailableShip> = {}): AvailableShip {
  return anAvailableShip({
    id: '5f1d7c2e-0000-4000-8000-000000000002',
    name: 'Salty Whisker',
    arrivedFrom: 'Tortuga',
    incoming: true,
    ...overrides,
  });
}

/** An Incoming Ship as listed on the harbor management page: 2 Rum and 1 Sugar aboard, Delivery Price 115.00. */
export function anIncomingShipListing(overrides: Partial<IncomingShip> = {}): IncomingShip {
  const rum = aCargo({ id: 'c0a8f3a2-0000-4000-8000-000000000009', name: 'Rum', weight: 5.5 });
  const sugar = aCargo({ id: 'c0a8f3a2-0000-4000-8000-000000000010', name: 'Sugar', weight: 0.7 });
  return {
    shipId: '5f1d7c2e-0000-4000-8000-000000000002',
    name: 'Salty Whisker',
    arrivedFrom: 'Tortuga',
    cargo: [rum, rum, sugar],
    deliveryPrice: '115.00',
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

/** The User is told that "Black Pearl" has just arrived from "Tortuga". */
export function anArrivalNotice(overrides: Partial<ArrivalNotice> = {}): ArrivalNotice {
  return {
    shipId: '5f1d7c2e-0000-4000-8000-000000000001',
    shipName: 'Black Pearl',
    originHarbor: 'Tortuga',
    ...overrides,
  };
}

/** A Catain the User can pick as the commander of a new Ship. */
export function aCatain(overrides: Partial<Catain> = {}): Catain {
  return {
    id: 'c1a7a1n0-0000-4000-8000-000000000001',
    name: 'Whiskers',
    ...overrides,
  };
}

/** A Cargo of the catalog with the quantity the Harbor has on hand. */
export function aStockedCargo(overrides: Partial<StockedCargo> = {}): StockedCargo {
  return {
    cargoId: 'c0a8f3a2-0000-4000-8000-000000000009',
    name: 'Rum',
    quantity: 3,
    price: '42.00',
    ...overrides,
  };
}

/** The Savings of a Harbor; by default the Starting Savings. */
export function aSavings(overrides: Partial<Savings> = {}): Savings {
  return { amount: '1000.00', ...overrides };
}

export function aPurchaseRequest(overrides: Partial<PurchaseRequest> = {}): PurchaseRequest {
  return { cargoId: 'c0a8f3a2-0000-4000-8000-000000000009', quantity: 1, ...overrides };
}
