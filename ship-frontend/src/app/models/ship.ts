import { Cargo } from "./cargo";

export interface Ship {
    id: string;
    name: string;
    cargo: Cargo[];
    weight: number;
    maxweight: number;
    shippingState: ShippingState | null;
}

export enum ShippingState {
  PREPARING= 'PREPARING', SHIPPING= 'SHIPPING', DONE= 'DONE'
}
