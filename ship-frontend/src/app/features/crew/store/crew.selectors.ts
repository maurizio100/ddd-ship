export interface HireRefusal {
  recruitId: string;
  message: string;
  savingsShort: boolean;
}

export interface CrewState {
  hiringRecruitIds: string[];
  hireRefusal: HireRefusal | null;
  newInPoolRecruitIds: string[];
  harborName: string | null;
  loading: boolean;
  error: unknown;
}
