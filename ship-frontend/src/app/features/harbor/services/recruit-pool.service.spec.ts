import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { RecruitPoolService } from './recruit-pool.service';
import { environment } from '../../../../environments/environment';

describe('RecruitPoolService', () => {
  let service: RecruitPoolService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(RecruitPoolService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getRecruitPool GETs /recruits', () => {
    const pool = [{ id: 'r1', name: 'Mittens', trait: 'Sure-pawed' }];
    let result: unknown;

    service.getRecruitPool().subscribe((recruits) => (result = recruits));

    const request = http.expectOne(`${environment.baseUrl}/recruits`);
    expect(request.request.method).toBe('GET');
    request.flush(pool);
    expect(result).toEqual(pool);
  });
});
