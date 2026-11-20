import { CrewMember } from '../../ships/models/crew-member';
import { Recruit } from '../../harbor/models/recruit';

/** A Hire: the Crew Member now aboard, the Hiring Fee paid and the Savings left (two-decimal strings), and the Recruit that took its place. */
export interface Hire {
  shipId: string;
  name: string;
  crewMember: CrewMember;
  hiringFee: string;
  savings: string;
  newRecruit: Recruit;
}

export interface HireRequest {
  recruitId: string;
}
