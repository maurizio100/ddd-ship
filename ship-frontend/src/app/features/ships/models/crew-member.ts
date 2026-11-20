import { Portrait } from './portrait';

/** A Recruit Hired onto a ship; keeps the Recruit's identity. */
export interface CrewMember {
  id: string;
  name: string;
  trait: string;
  portrait: Portrait;
  /** What a Dismiss refunds into the Savings, a decimal string with two decimals. */
  refund: string;
}
