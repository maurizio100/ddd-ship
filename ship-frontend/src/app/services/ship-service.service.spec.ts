import { TestBed } from '@angular/core/testing';

import { ShipService } from './ship-service.service';

describe('ShipServiceService', () => {
  let service: ShipService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShipService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
