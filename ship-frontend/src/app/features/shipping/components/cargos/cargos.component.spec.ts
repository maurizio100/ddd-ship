import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargosComponent } from './cargos.component';

describe('CargosComponent', () => {
  let component: CargosComponent;
  let fixture: ComponentFixture<CargosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CargosComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CargosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
