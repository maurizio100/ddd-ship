import { Portrait } from '../../ships/models/portrait';

/** A cat of a Harbor's Recruit Pool, offered for hire. */
export interface Recruit {
  id: string;
  name: string;
  trait: string;
  portrait: Portrait;
  /** The Hiring Fee, a decimal string with two decimals. */
  hiringFee: string;
}
