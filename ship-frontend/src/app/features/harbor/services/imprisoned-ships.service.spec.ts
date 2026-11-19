import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ImprisonedShipsService } from './imprisoned-ships.service';
import { environment } from '../../../../environments/environment';
import { aFinePayment, anImprisonedShip } from '../../../../testing/fixtures';

describe('ImprisonedShipsService', () => {
  let service: ImprisonedShipsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ImprisonedShipsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getImprisonedShips GETs /imprisoned-ships and keeps the Fine a string', () => {
    let imprisonedShips: unknown;

    service.getImprisonedShips().subscribe((result) => (imprisonedShips = result));

    const request = http.expectOne(`${environment.baseUrl}/imprisoned-ships`);
    expect(request.request.method).toBe('GET');
    request.flush([anImprisonedShip()]);
    expect(imprisonedShips).toEqual([anImprisonedShip()]);
  });

  it('payFine POSTs to /imprisoned-ships/<id>/fine-payment without a body and returns the payment', () => {
    let payment: unknown;

    service.payFine('5f1d7c2e-0000-4000-8000-000000000002').subscribe((result) => (payment = result));

    const request = http.expectOne(`${environment.baseUrl}/imprisoned-ships/5f1d7c2e-0000-4000-8000-000000000002/fine-payment`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();
    request.flush(aFinePayment());
    expect(payment).toEqual(aFinePayment());
  });
});
