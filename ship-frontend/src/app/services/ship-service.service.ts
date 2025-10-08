import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from '../models/ship';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Cargo } from '../models/cargo';
import { NewShipRequest } from '../models/new-ship-request';
import { ShippingSummary } from '../models/shipping-summary';
import { environment } from '../../environments/environment';

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

  getShip(shipId: string): Observable<Ship> {
    const url = `${this.shipsUrl}/${shipId}`;
    return this.http
      .get<Ship>(url)
      .pipe(tap((_) => console.log('fetch details for ship')));
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

  loadCargo(ship: Ship, cargo: Cargo): Observable<Ship> {
    const url = `${this.shipsUrl}/${ship.id}/cargos`;
    return this.http
      .post<Ship>(url, { cargoId: cargo.id }, this.httpOptions)
      .pipe(tap((_) => console.log(`added cargo: ${cargo.name}`)));
  }

  unloadCargo(ship: Ship, cargo: Cargo): Observable<Ship> {
    const url = `${this.shipsUrl}/${ship.id}/cargos/${cargo.id}`;
    return this.http
      .delete<Ship>(url, this.httpOptions)
      .pipe(tap((_) => console.log(`deleted cargo cargo: ${cargo.name}`)));
  }
}
