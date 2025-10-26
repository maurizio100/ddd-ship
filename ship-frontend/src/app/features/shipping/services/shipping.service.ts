import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from '../models/ship';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Cargo } from '../models/cargo';
import { environment } from '../../../../environments/environment';
import {ShippingSummary} from "../models/shipping-summary";

@Injectable({
  providedIn: 'root',
})
export class ShippingService {
  private shipsUrl = `${environment.baseUrl}/ships`;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  getShip(shipId: string): Observable<Ship> {
    const url = `${this.shipsUrl}/${shipId}`;
    return this.http
      .get<Ship>(url)
      .pipe(tap((_) => console.log('fetch details for ship')));
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
