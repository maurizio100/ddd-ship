import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PurchaseRequest } from '../models/purchase-request';

@Injectable({ providedIn: 'root' })
export class MarketService {
  private purchasesUrl = `${environment.baseUrl}/market/purchases`;

  constructor(private http: HttpClient) {}

  /** Buys at the Market; the backend answers 204, so the Stock and the Savings are refetched afterwards. */
  buyCargo(request: PurchaseRequest): Observable<void> {
    return this.http.post<void>(this.purchasesUrl, request);
  }
}
