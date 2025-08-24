import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Catain } from '../models/catain';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CatainService {
  private catainsUrl = `${environment.baseUrl}/catains`;

  constructor(private http: HttpClient) {}

  getCatains(): Observable<Catain[]> {
    return this.http.get<Catain[]>(this.catainsUrl);
  }

  getCatainImage(id: string): Observable<string> {
    return this.http.get<string>(`${this.catainsUrl}${id}/image`);
  }

  getCatainImageUrl(id: string): string {
    return `${this.catainsUrl}/${id}/image`;
  }
}
