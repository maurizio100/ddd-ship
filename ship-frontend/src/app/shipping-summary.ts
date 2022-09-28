import { Cargo } from "./cargo";

export interface ShippingSummary {
    id: number;
    name: string;
    cargo: Cargo[];
    sailorsCode: string;
}