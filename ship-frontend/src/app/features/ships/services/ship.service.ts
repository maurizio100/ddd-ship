import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from '../models/ship';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NewShipRequest } from '../models/new-ship-request';
import {environment} from "../../../../environments/environment";
import {Cargo} from "../../shipping/models/cargo";
import {ShippingSummary} from "../../shipping/models/shipping-summary";

@Injectable({
  providedIn: 'root',
})
export class ShipService {
  private shipsUrl = `${environment.baseUrl}/ships`;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  addShip(ship: NewShipRequest): Observable<Ship> {
    return this.http
      .post<Ship>(this.shipsUrl, ship, this.httpOptions)
      .pipe(
        tap((newShip: Ship) => console.log(`added ship w/ id=${newShip.id}`))
      );
  }

  getShips(): Observable<Ship[]> {
    return this.http.get<Ship[]>(this.shipsUrl);
  }

  createShipping(ship: Ship): Observable<ShippingSummary> {
    const url = `${this.shipsUrl}/${ship.id}/shippings`;
    return this.http
      .post<ShippingSummary>(url, this.httpOptions)
      .pipe(
        tap((shipping: ShippingSummary) =>
          console.log(`added shippping for ship id=${shipping.id}`)
        )
      );
  }
}
