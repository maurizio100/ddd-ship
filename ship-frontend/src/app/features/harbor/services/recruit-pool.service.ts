import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Recruit } from '../models/recruit';

@Injectable({ providedIn: 'root' })
export class RecruitPoolService {
  private recruitsUrl = `${environment.baseUrl}/recruits`;

  constructor(private http: HttpClient) {}

  getRecruitPool(): Observable<Recruit[]> {
    return this.http.get<Recruit[]>(this.recruitsUrl);
  }
}
