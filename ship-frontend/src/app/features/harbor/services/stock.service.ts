import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StockedCargo } from '../models/stocked-cargo';

@Injectable({ providedIn: 'root' })
export class StockService {
  private stockUrl = `${environment.baseUrl}/stock`;

  constructor(private http: HttpClient) {}

  getStock(): Observable<StockedCargo[]> {
    return this.http.get<StockedCargo[]>(this.stockUrl);
  }
}
