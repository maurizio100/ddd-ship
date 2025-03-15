import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewShipComponent } from './new-ship.component';

describe('NewShipComponent', () => {
  let component: NewShipComponent;
  let fixture: ComponentFixture<NewShipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewShipComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewShipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
