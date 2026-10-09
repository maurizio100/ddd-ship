import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PurchaseRequest } from '../models/purchase-request';

@Injectable({ providedIn: 'root' })
export class MarketService {
  constructor(private http: HttpClient) {}

  buyCargo(request: PurchaseRequest): Observable<void> {
    throw new Error('STORY-025: not implemented');
  }
}
