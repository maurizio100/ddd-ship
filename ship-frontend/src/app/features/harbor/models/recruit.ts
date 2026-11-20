/** A cat of a Harbor's Recruit Pool, offered for hire. */
export interface Recruit {
  id: string;
  name: string;
  trait: string;
  /** The Hiring Fee, a decimal string with two decimals. */
  hiringFee: string;
}
