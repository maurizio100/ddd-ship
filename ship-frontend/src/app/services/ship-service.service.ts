import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from '../models/ship';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Cargo } from '../models/cargo';
import { NewShipRequest } from '../models/new-ship-request';

@Injectable({
  providedIn: 'root',
})
export class ShipService {
  private shipsUrl = 'http://localhost:8080/web/ships';

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

  deleteShip(shipId: string) {
    const url = `${this.shipsUrl}/${shipId}`;

    return this.http
      .delete<Ship>(url, this.httpOptions)
      .pipe(tap((_) => console.log(`deleted ship id=${shipId}`)));
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

  updateShip(ship: Ship): Observable<any> {
    const url = `${this.shipsUrl}/${ship.id}`;
    return this.http
      .put(url, ship, this.httpOptions)
      .pipe(tap((_) => console.log(`updated ship id=${ship.id}`)));
  }

  createShipping(ship: Ship): Observable<Ship> {
    const url = `${this.shipsUrl}/${ship.id}/shippings`;

    return this.http
      .post<Ship>(this.shipsUrl, this.httpOptions)
      .pipe(
        tap((newShip: Ship) =>
          console.log(`added shippping w/ id=${newShip.id}`)
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
