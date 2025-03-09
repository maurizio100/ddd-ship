import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Catain } from '../models/catain';
import { Observable } from 'rxjs';
import { Binary } from '@angular/compiler';

@Injectable({
  providedIn: 'root',
})
export class CatainService {
  private catainsUrl = 'http://localhost:8080/web/catains';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  getCatains(): Observable<Catain[]> {
    return this.http.get<Catain[]>(this.catainsUrl);
  }

  getCatainImage(id: string): Observable<string> {
    return this.http.get<string>(`${this.catainsUrl}${id}/image`);
  }
}
