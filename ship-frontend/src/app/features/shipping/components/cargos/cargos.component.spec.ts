import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';

import { CargosComponent } from './cargos.component';
import { CargoService } from '../../services/cargo.service';
import { ShippingService } from '../../services/shipping.service';
import { AvailableCargo } from '../../models/cargo';
import { Ship } from '../../models/ship';
import { aCargo, anAvailableCargo, aShip } from '../../../../../testing/fixtures';

describe('CargosComponent (Available Cargo)', () => {
  const rum = anAvailableCargo({ id: 'rum', name: 'Rum', weight: 5.5 });
  const ale = anAvailableCargo({ id: 'ale', name: 'Ale', weight: 2.0 });

  let fixture: ComponentFixture<CargosComponent>;
  let cargoService: jasmine.SpyObj<CargoService>;
  let shippingService: jasmine.SpyObj<ShippingService>;
  let cargoLoad: Subject<Ship>;

  beforeEach(() => {
    cargoService = jasmine.createSpyObj<CargoService>('CargoService', ['getCargos']);
    shippingService = jasmine.createSpyObj<ShippingService>('ShippingService', ['loadCargo', 'unloadCargo']);
    cargoLoad = new Subject<Ship>();

    TestBed.configureTestingModule({
      imports: [CargosComponent],
      providers: [
        { provide: CargoService, useValue: cargoService },
        { provide: ShippingService, useValue: shippingService },
      ],
    });
  });

  /** Renders the Available Cargo of [ship]; like the ship detail page, a ship update is fed back. */
  function render(ship: Ship): void {
    fixture = TestBed.createComponent(CargosComponent);
    const component = fixture.componentInstance;
    component.ship = ship;
    component.showLoaded = false;
    component.showLoadObserve = cargoLoad.asObservable();
    component.shipUpdated.subscribe((updated) => {
      ship.cargo = updated.cargo;
      cargoLoad.next(updated);
    });
    fixture.detectChanges();
  }

  function shownCargo(): { name: string; stock: string }[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-testid="available-cargo"]')).map(
      (item: any) => ({
        name: item.querySelector('[data-testid="cargo-name"]').textContent.trim(),
        stock: item.querySelector('[data-testid="cargo-stock"]').textContent.trim(),
      })
    );
  }

  function click(cargoName: string): void {
    const item = Array.from(fixture.nativeElement.querySelectorAll('[data-testid="available-cargo"]')).find(
      (el: any) => el.querySelector('[data-testid="cargo-name"]').textContent.trim() === cargoName
    ) as HTMLElement;
    item.click();
    fixture.detectChanges();
  }

  function stockOf(cargo: AvailableCargo, stock: number): AvailableCargo {
    return { ...cargo, stock };
  }

  it('Loading Cargo takes it out of the Stock', () => {
    // Given the Stock holds 2 Rum and a ship is being prepared
    cargoService.getCargos.and.returnValues(of([stockOf(rum, 2), ale]), of([stockOf(rum, 1), stockOf(ale, 2)]));
    const ship = aShip();
    render(ship);
    expect(shownCargo()).toContain({ name: 'Rum', stock: '2' });

    // When the User loads Rum onto the ship
    shippingService.loadCargo.and.returnValue(of(aShip({ cargo: [aCargo({ id: 'rum', name: 'Rum' })], weight: 5.5 })));
    click('Rum');

    // Then the Available Cargo is fetched again and shows the Stock the backend reports now
    expect(cargoService.getCargos).toHaveBeenCalledTimes(2);
    expect(shownCargo()).toEqual([{ name: 'Ale', stock: '2' }]);
  });

  it('Unloading Cargo while preparing puts it back into the Stock', () => {
    // Given the Stock holds 1 Rum and the ship being prepared has Rum loaded
    cargoService.getCargos.and.returnValues(of([stockOf(rum, 1), ale]), of([stockOf(rum, 2), ale]));
    const ship = aShip({ cargo: [aCargo({ id: 'rum', name: 'Rum' })], weight: 5.5 });
    render(ship);
    expect(shownCargo().map((c) => c.name)).not.toContain('Rum');

    // When the User unloads Rum (the Loaded Cargo list reports the updated ship to the page)
    ship.cargo = [];
    ship.weight = 0;
    cargoLoad.next(ship);
    fixture.detectChanges();

    // Then the Available Cargo shows 2 Rum
    expect(shownCargo()).toContain({ name: 'Rum', stock: '2' });
  });

  it('Cargo that is out of Stock cannot be loaded', () => {
    // Given the Stock holds no Silk: the backend does not offer it
    cargoService.getCargos.and.returnValue(of([rum, ale]));

    render(aShip());

    // Then Silk is not among the Available Cargo
    expect(shownCargo().map((c) => c.name)).toEqual(['Rum', 'Ale']);
  });

  it('A rejected load leaves the Stock unchanged', () => {
    // Given the Stock holds 2 Rum and the ship has a Current Weight of 14.0
    cargoService.getCargos.and.returnValue(of([stockOf(rum, 2)]));
    render(aShip({ weight: 14.0 }));

    // When the User loads Rum and the backend rejects it
    shippingService.loadCargo.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { title: 'Ship too heavy', status: 409, detail: 'Loading Rum would exceed the Max Weight of 15.0' },
          })
      )
    );
    click('Rum');

    // Then the rejection is shown and the Stock still shows 2 Rum
    const rejection = fixture.nativeElement.querySelector('[data-testid="load-rejection"]');
    expect(rejection?.textContent).toContain('Loading Rum would exceed the Max Weight of 15.0');
    expect(shownCargo()).toEqual([{ name: 'Rum', stock: '2' }]);
  });
});
