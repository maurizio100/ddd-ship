import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EMPTY, Observable } from 'rxjs';
import { ImprisonedShip } from '../models/imprisoned-ship';
import { FinePayment } from '../models/fine-payment';

@Injectable({ providedIn: 'root' })
export class ImprisonedShipsService {
  constructor(private http: HttpClient) {}

  getImprisonedShips(): Observable<ImprisonedShip[]> {
    return EMPTY;
  }

  payFine(shipId: string): Observable<FinePayment> {
    return EMPTY;
  }
}
