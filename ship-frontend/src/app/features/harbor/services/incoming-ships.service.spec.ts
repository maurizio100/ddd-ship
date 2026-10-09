import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { IncomingShipsService } from './incoming-ships.service';
import { environment } from '../../../../environments/environment';
import { anIncomingShipListing } from '../../../../testing/fixtures';

describe('IncomingShipsService', () => {
  let service: IncomingShipsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(IncomingShipsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getIncomingShips GETs /incoming-ships and keeps the Delivery Price a string', () => {
    let incomingShips: unknown;

    service.getIncomingShips().subscribe((result) => (incomingShips = result));

    const request = http.expectOne(`${environment.baseUrl}/incoming-ships`);
    expect(request.request.method).toBe('GET');
    request.flush([anIncomingShipListing()]);
    expect(incomingShips).toEqual([anIncomingShipListing()]);
  });
});
