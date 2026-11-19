/** A paid Fine: the freed ship and the Savings left, as two-decimal strings. */
export interface FinePayment {
  shipId: string;
  name: string;
  fine: string;
  savings: string;
}
