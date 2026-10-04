export interface Cargo {
    id: string;
    name: string;
    weight: number;
}

/** A Cargo that can be loaded at this Harbor, with how many of it the Harbor's Stock holds. */
export interface AvailableCargo extends Cargo {
    stock: number;
}
