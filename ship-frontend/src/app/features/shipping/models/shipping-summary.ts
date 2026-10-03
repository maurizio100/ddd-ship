import { Cargo } from "./cargo";
import { ShippingState } from "./ship";

export interface ShippingSummary {
    id: string;
    shipId: string;
    catainId: string;
    catainName: string;
    name: string;
    cargo: Cargo[];
    sailorsCode: string;
    weight: number;
    destinationHarbor: string | null;
    shippingState?: ShippingState | null;
}
