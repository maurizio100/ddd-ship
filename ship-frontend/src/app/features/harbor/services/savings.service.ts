import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Savings } from '../models/savings';

@Injectable({ providedIn: 'root' })
export class SavingsService {
  constructor(private http: HttpClient) {}

  getSavings(): Observable<Savings> {
    throw new Error('STORY-024');
  }
}
