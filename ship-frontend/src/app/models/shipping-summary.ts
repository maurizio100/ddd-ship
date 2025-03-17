import { Cargo } from "./cargo";

export interface ShippingSummary {
    id: string;
    shipId: string;
    catainId: string;
    catainName: string;
    name: string;
    cargo: Cargo[];
    sailorsCode: string;
    weight: number;

}
