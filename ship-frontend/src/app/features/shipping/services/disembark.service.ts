import { HttpClient, HttpHandler, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Ship } from '../models/ship';
import { ShippingSummary } from '../models/shipping-summary';
import { environment } from '../../../../environments/environment';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DisembarkService {
  private shippingsUrl = `${environment.baseUrl}/ships`;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  releaseShip(ship: Ship): Observable<ShippingSummary> {
    const url = `${this.shippingsUrl}/${ship.id}/shippings`;
    return this.http
      .put<ShippingSummary>(url, this.httpOptions)
      .pipe(
        tap((shippingSummary: ShippingSummary) =>
          console.log(`created shipping for ship ${shippingSummary.name}`)
        )
      );
  }

  getShipping(shipId: String, shippingId: String): Observable<ShippingSummary> {
    const url = `${this.shippingsUrl}/${shipId}/shippings/${shippingId}`;
    return this.http
      .get<ShippingSummary>(url)
      .pipe(tap((_) => console.log('fetch shipping details')));
  }
}
