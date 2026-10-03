import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { DisembarkService } from './disembark.service';
import { environment } from '../../../../environments/environment';
import { aShip, aShippingSummary } from '../../../../testing/fixtures';

describe('DisembarkService', () => {
  let service: DisembarkService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(DisembarkService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('releaseShip PUTs {destinationHarbor} to /ships/{id}/shippings', () => {
    const ship = aShip();
    let released: unknown;

    service.releaseShip(ship, 'Port Royal').subscribe((summary) => (released = summary));

    const request = http.expectOne(`${environment.baseUrl}/ships/${ship.id}/shippings`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ destinationHarbor: 'Port Royal' });
    request.flush(aShippingSummary());
    expect(released).toEqual(aShippingSummary());
  });
});
