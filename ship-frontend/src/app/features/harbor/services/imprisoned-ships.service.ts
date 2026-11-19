import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ImprisonedShip } from '../models/imprisoned-ship';
import { FinePayment } from '../models/fine-payment';

@Injectable({ providedIn: 'root' })
export class ImprisonedShipsService {
  private imprisonedShipsUrl = `${environment.baseUrl}/imprisoned-ships`;

  constructor(private http: HttpClient) {}

  getImprisonedShips(): Observable<ImprisonedShip[]> {
    return this.http.get<ImprisonedShip[]>(this.imprisonedShipsUrl);
  }

  /** Pays the Fine of the Imprisoned Ship and frees it; the backend answers with the Fine paid and the Savings left. */
  payFine(shipId: string): Observable<FinePayment> {
    return this.http.post<FinePayment>(`${this.imprisonedShipsUrl}/${shipId}/fine-payment`, null);
  }
}
