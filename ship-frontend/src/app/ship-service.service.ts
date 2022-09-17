import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from './ship';


import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class ShipServiceService {

  private shipsUrl = 'http://localhost:8080/web/ships';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient
  ) { }

  addShip(ship: Ship): Observable<Ship> {
    return this.http.post<Ship>(this.shipsUrl, ship, this.httpOptions).pipe(
      tap((newShip: Ship) => console.log(`added ship w/ id=${newShip.id}`))
    );
  }
}
