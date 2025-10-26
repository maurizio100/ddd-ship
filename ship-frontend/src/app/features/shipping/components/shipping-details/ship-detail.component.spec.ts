import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShipDetailComponent } from './ship-detail.component';

describe('ShipDetailComponent', () => {
  let component: ShipDetailComponent;
  let fixture: ComponentFixture<ShipDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShipDetailComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShipDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
