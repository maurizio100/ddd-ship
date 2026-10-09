import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { MarketService } from './market.service';
import { environment } from '../../../../environments/environment';
import { aPurchaseRequest } from '../../../../testing/fixtures';

describe('MarketService', () => {
  let service: MarketService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(MarketService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('buyCargo POSTs the purchase to /market/purchases', () => {
    const purchase = aPurchaseRequest({ quantity: 2 });
    let completed = false;

    service.buyCargo(purchase).subscribe({ complete: () => (completed = true) });

    const request = http.expectOne(`${environment.baseUrl}/market/purchases`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ cargoId: purchase.cargoId, quantity: 2 });
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(completed).toBeTrue();
  });
});
