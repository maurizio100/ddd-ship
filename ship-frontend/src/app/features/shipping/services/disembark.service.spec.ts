import { TestBed } from '@angular/core/testing';

import { DisembarkService } from './disembark.service';

describe('DisembarkService', () => {
  let service: DisembarkService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DisembarkService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
