import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Savings } from '../models/savings';

@Injectable({ providedIn: 'root' })
export class SavingsService {
  private savingsUrl = `${environment.baseUrl}/savings`;

  constructor(private http: HttpClient) {}

  getSavings(): Observable<Savings> {
    return this.http.get<Savings>(this.savingsUrl);
  }
}
