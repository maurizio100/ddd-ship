/** A purchase at the Market: [quantity] of the Cargo [cargoId], paid from the Savings. */
export interface PurchaseRequest {
  cargoId: string;
  quantity: number;
}
