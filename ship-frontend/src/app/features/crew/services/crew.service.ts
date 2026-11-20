import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Hire, HireRequest } from '../models/hire';

@Injectable({ providedIn: 'root' })
export class CrewService {
  private readonly http = inject(HttpClient);

  /** Dismisses the Crew Member from the ship; no refund. */
  dismiss(shipId: string, crewMemberId: string): Observable<void> {
    return this.http.delete<void>(`${environment.baseUrl}/ships/${shipId}/crew/${crewMemberId}`);
  }

  /** Hires the Recruit onto the ship; the backend pays the Hiring Fee and answers with the Hire. */
  hire(shipId: string, recruitId: string): Observable<Hire> {
    const request: HireRequest = { recruitId };
    return this.http.post<Hire>(`${environment.baseUrl}/ships/${shipId}/crew`, request);
  }
}
