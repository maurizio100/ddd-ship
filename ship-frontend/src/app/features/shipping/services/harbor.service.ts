import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { KnownHarbors } from '../models/known-harbors';

@Injectable({
  providedIn: 'root',
})
export class HarborService {
  private readonly http = inject(HttpClient);
  private readonly harborsUrl = `${environment.baseUrl}/harbors`;

  /** This Harbor's Known Harbors: the choices for a Destination Harbor. */
  getKnownHarbors(): Observable<KnownHarbors> {
    return this.http.get<KnownHarbors>(this.harborsUrl);
  }
}
