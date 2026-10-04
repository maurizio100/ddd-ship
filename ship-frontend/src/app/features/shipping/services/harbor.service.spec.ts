import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { HarborService } from './harbor.service';
import { environment } from '../../../../environments/environment';
import { someKnownHarbors } from '../../../../testing/fixtures';

describe('HarborService', () => {
  let service: HarborService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(HarborService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getKnownHarbors GETs /harbors', () => {
    let knownHarbors: unknown;

    service.getKnownHarbors().subscribe((harbors) => (knownHarbors = harbors));

    const request = http.expectOne(`${environment.baseUrl}/harbors`);
    expect(request.request.method).toBe('GET');
    request.flush(someKnownHarbors());
    expect(knownHarbors).toEqual(someKnownHarbors());
  });
});
