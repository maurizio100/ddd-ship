import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Catain } from '../model/catain';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CatainService {
  private readonly catainsUrl = `${environment.baseUrl}/catains`;

  constructor(private readonly http: HttpClient) {}

  getCatains(): Observable<Catain[]> {
    return this.http.get<Catain[]>(this.catainsUrl);
  }
}
