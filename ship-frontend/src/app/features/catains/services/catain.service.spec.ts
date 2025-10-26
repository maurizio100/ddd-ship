import { TestBed } from '@angular/core/testing';

import { CatainService } from './catain.service';

describe('CatainService', () => {
  let service: CatainService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CatainService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
