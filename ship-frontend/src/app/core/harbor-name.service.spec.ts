import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { HarborNameService } from './harbor-name.service';
import { environment } from '../../environments/environment';

describe('HarborNameService', () => {
  let service: HarborNameService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(HarborNameService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getHarborName GETs /harbors and emits the harborName', () => {
    let name: string | undefined;

    service.getHarborName().subscribe((n) => (name = n));

    const request = http.expectOne(`${environment.baseUrl}/harbors`);
    expect(request.request.method).toBe('GET');
    request.flush({ harborName: 'Port Royal', knownHarbors: ['Tortuga'] });
    expect(name).toBe('Port Royal');
  });
});
