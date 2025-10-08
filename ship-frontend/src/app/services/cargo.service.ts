import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Cargo } from '../models/cargo';
import { environment } from '../../environments/environment';

import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CargoService {
  private cargosUrl = `${environment.baseUrl}/cargos`;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  getCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(this.cargosUrl);
  }
}
