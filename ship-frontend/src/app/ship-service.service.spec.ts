import { TestBed } from '@angular/core/testing';

import { ShipServiceService } from './ship-service.service';

describe('ShipServiceService', () => {
  let service: ShipServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShipServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
