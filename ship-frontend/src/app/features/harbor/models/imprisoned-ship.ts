/** An Imprisoned Ship at its Home Harbor, with the Fine that frees it. */
export interface ImprisonedShip {
  shipId: string;
  name: string;
  catain: string;
  homeHarbor: string;
  /** Two-decimal string such as "50.00". */
  fine: string;
  /** Whether the Savings cover the Fine, as the backend computed it. */
  savingsCoverFine: boolean;
}

/** What the User is told about whether the Savings cover the Fine of [ship]; the backend decided, nothing is compared here. */
export function fineCoverage(ship: ImprisonedShip): string {
  return ship.savingsCoverFine ? 'The Savings cover the Fine' : `The Savings do not cover the Fine of ${ship.fine} $`;
}
