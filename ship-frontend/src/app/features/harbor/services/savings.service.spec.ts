import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { SavingsService } from './savings.service';
import { environment } from '../../../../environments/environment';
import { aSavings } from '../../../../testing/fixtures';

describe('SavingsService', () => {
  let service: SavingsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SavingsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getSavings GETs /savings and keeps the amount a string', () => {
    let savings: unknown;

    service.getSavings().subscribe((result) => (savings = result));

    const request = http.expectOne(`${environment.baseUrl}/savings`);
    expect(request.request.method).toBe('GET');
    request.flush(aSavings({ amount: '640.50' }));
    expect(savings).toEqual({ amount: '640.50' });
  });
});
