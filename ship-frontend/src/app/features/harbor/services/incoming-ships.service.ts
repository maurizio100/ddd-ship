import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncomingShip } from '../models/incoming-ship';

@Injectable({ providedIn: 'root' })
export class IncomingShipsService {
  constructor(private http: HttpClient) {}

  getIncomingShips(): Observable<IncomingShip[]> {
    throw new Error('not implemented');
  }
}
