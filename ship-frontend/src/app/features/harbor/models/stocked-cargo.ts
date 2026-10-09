export interface StockedCargo {
  cargoId: string;
  name: string;
  quantity: number;
  /** Two-decimal string such as "42.00"; null until the Harbor has rolled one. */
  price: string | null;
}
