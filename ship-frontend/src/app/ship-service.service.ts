import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Ship } from './ship';


import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Cargo } from './cargo';


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
    return this.http.post<Ship>(this.shipsUrl, {name: ship.name}, this.httpOptions).pipe(
      tap((newShip: Ship) => console.log(`added ship w/ id=${newShip.id}`))
    );
  }

  deleteShip(shipId:Number) {
    const url = `${this.shipsUrl}/${shipId}`;

    return this.http.delete<Ship>(url, this.httpOptions).pipe(
      tap(_ => console.log(`deleted ship id=${shipId}`))
    )
  }

  getShips(): Observable<Ship[]> {
    return this.http.get<Ship[]>(this.shipsUrl)
  }

  getShip(shipId: Number): Observable<Ship> {
    const url = `${this.shipsUrl}/${shipId}`
    return this.http.get<Ship>(url).pipe(
      tap(_ => console.log('fetch details for ship'))
    )
  }

  updateShip(ship: Ship): Observable<any> {
    const url = `${this.shipsUrl}/${ship.id}`
    return this.http.put(url, ship, this.httpOptions).pipe(
      tap(_ => console.log(`updated ship id=${ship.id}`))
    )
  }

  loadCargo(ship: Ship, cargo: Cargo): Observable<Ship> {
    const url = `${this.shipsUrl}/${ship.id}/cargos/`
    return this.http.post<Ship>(url, {cargoId: cargo.id}, this.httpOptions).pipe(
      tap(_ => console.log(`added cargo: ${cargo.name}`))
    )
  }
}
