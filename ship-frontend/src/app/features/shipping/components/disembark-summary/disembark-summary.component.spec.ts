import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisembarkSummaryComponent } from './disembark-summary.component';

describe('DisembarkSummaryComponent', () => {
  let component: DisembarkSummaryComponent;
  let fixture: ComponentFixture<DisembarkSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DisembarkSummaryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DisembarkSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
