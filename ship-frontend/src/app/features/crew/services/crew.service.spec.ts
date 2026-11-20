import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { CrewService } from './crew.service';
import { Hire } from '../models/hire';
import { environment } from '../../../../environments/environment';
import { aRecruit } from '../../../../testing/fixtures';

describe('CrewService', () => {
  let service: CrewService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CrewService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('hire POSTs /ships/<id>/crew with the recruitId and keeps the money as strings', () => {
    const hire: Hire = {
      shipId: 'ship-1',
      name: 'Black Pearl',
      crewMember: { id: 'r1', name: 'Mittens', trait: 'Strong Paws' },
      hiringFee: '50.00',
      savings: '950.00',
      newRecruit: aRecruit({ id: 'r2', name: 'Patch' }),
    };
    let result: unknown;

    service.hire('ship-1', 'r1').subscribe((value) => (result = value));

    const request = http.expectOne(`${environment.baseUrl}/ships/ship-1/crew`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ recruitId: 'r1' });
    request.flush(hire);
    expect(result).toEqual(hire);
  });

  it('dismiss DELETEs /ships/<id>/crew/<memberId>', () => {
    let done = false;

    service.dismiss('ship-1', 'c1').subscribe(() => (done = true));

    const request = http.expectOne(`${environment.baseUrl}/ships/ship-1/crew/c1`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(done).toBeTrue();
  });
});
