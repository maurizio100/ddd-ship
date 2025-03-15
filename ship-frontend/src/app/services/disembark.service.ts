import { HttpClient, HttpHandler, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Ship } from '../models/ship';
import { ShippingSummary } from '../models/shipping-summary';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DisembarkService {
  private shippingsUrl = 'http://localhost/web/ships';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  releaseShip(ship: Ship): Observable<Ship> {
    const url = `/${ship.id}/shippings`;
    return this.http
      .put<Ship>(url, this.httpOptions)
      .pipe(
        tap((releaseShip: Ship) =>
          console.log(`created shipping for ship ${releaseShip.name}`)
        )
      );
  }

  createShipping(ship: Ship): Observable<ShippingSummary> {
    return this.http
      .post<ShippingSummary>(
        this.shippingsUrl,
        {
          shippingId: ship.id,
        },
        this.httpOptions
      )
      .pipe(
        tap((newShipping: ShippingSummary) =>
          console.log(`created shipping for ship ${newShipping.name}`)
        )
      );
  }

  getShipping(shippingId: Number): Observable<ShippingSummary> {
    const url = `${this.shippingsUrl}/${shippingId}`;
    return this.http
      .get<ShippingSummary>(url)
      .pipe(tap((_) => console.log('fetch shipping details')));
  }
}
