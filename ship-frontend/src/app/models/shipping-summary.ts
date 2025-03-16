import { Cargo } from "./cargo";

export interface ShippingSummary {
    id: string;
    shipId: string;
    name: string;
    cargo: Cargo[];
    sailorsCode: string;
    weight: number;
}
