import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { IncomingShip } from '../models/incoming-ship';

@Injectable({ providedIn: 'root' })
export class IncomingShipsService {
  private incomingShipsUrl = `${environment.baseUrl}/incoming-ships`;

  constructor(private http: HttpClient) {}

  getIncomingShips(): Observable<IncomingShip[]> {
    return this.http.get<IncomingShip[]>(this.incomingShipsUrl);
  }

  /** Unloads the Incoming Ship; the backend answers 204, so read the new state back from its GETs. */
  unload(shipId: string): Observable<void> {
    return this.http.post<void>(`${this.incomingShipsUrl}/${shipId}/unloading`, null);
  }
}
