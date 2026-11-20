import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ShipService } from './ship.service';
import { ShipClass } from '../models/ship';
import { environment } from '../../../../environments/environment';

describe('ShipService (Ship Classes)', () => {
  it('getShipClasses GETs /ship-classes', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    const offers = [{ shipClass: ShipClass.GALLEON, crewCapacity: 6 }];
    let result: unknown;

    TestBed.inject(ShipService).getShipClasses().subscribe((r) => (result = r));
    const request = http.expectOne(`${environment.baseUrl}/ship-classes`);
    expect(request.request.method).toBe('GET');
    request.flush(offers);

    expect(result).toEqual(offers);
  });
});
