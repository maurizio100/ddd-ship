import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { StockService } from './stock.service';
import { environment } from '../../../../environments/environment';
import { aStockedCargo } from '../../../../testing/fixtures';

describe('StockService', () => {
  let service: StockService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(StockService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getStock GETs /stock', () => {
    let stock: unknown;

    service.getStock().subscribe((result) => (stock = result));

    const request = http.expectOne(`${environment.baseUrl}/stock`);
    expect(request.request.method).toBe('GET');
    request.flush([aStockedCargo()]);
    expect(stock).toEqual([aStockedCargo()]);
  });
});
