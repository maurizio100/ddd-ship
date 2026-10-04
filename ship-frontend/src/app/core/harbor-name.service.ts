import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { KnownHarbors } from '../features/shipping/models/known-harbors';

@Injectable({
  providedIn: 'root',
})
export class HarborNameService {
  private readonly http = inject(HttpClient);
  private readonly harborsUrl = `${environment.baseUrl}/harbors`;

  /** The Harbor Name of the Harbor whose backend this frontend talks to. */
  getHarborName(): Observable<string> {
    return this.http.get<KnownHarbors>(this.harborsUrl).pipe(map((harbors) => harbors.harborName));
  }
}
