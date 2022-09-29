import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Cargo } from './cargo';

import { Observable } from 'rxjs';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CargoService {
  private cargosUrl = 'http://172.22.118.168:8080/web/cargos';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient
  ) { }

  getCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(this.cargosUrl);
  }

}
