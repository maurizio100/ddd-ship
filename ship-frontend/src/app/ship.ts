import { Cargo } from "./cargo";

export interface Ship {
    id: number;
    name: string;
    cargo: Cargo[];
    weight: number;
    maxweight: number;
}