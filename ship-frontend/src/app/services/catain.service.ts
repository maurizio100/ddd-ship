import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Catain } from '../models/catain';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CatainService {
  private catainsUrl = 'http://localhost/web/catains';

  constructor(private http: HttpClient) {}

  getCatains(): Observable<Catain[]> {
    return this.http.get<Catain[]>(this.catainsUrl);
  }

  getCatainImage(id: string): Observable<string> {
    return this.http.get<string>(`${this.catainsUrl}${id}/image`);
  }
}
