import { Injectable } from '@angular/core';
import { EMPTY, Observable } from 'rxjs';
import { KnownHarbors } from '../models/known-harbors';

@Injectable({
  providedIn: 'root',
})
export class HarborService {
  getKnownHarbors(): Observable<KnownHarbors> {
    return EMPTY;
  }
}
