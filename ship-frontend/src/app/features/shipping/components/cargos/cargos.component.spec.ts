import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';

import { CargosComponent } from './cargos.component';
import { CargoService } from '../../services/cargo.service';
import { ShippingService } from '../../services/shipping.service';
import { Cargo } from '../../models/cargo';
import { Ship } from '../../models/ship';
import { aCargo, anAvailableCargo, aShip } from '../../../../../testing/fixtures';

describe('CargosComponent', () => {
  const rum = anAvailableCargo({ id: 'rum', name: 'Rum', weight: 5.5, stock: 3 });
  const ale = anAvailableCargo({ id: 'ale', name: 'Ale', weight: 2.0, stock: 2 });

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

  function render(ship: Ship, showLoaded: boolean): CargosComponent {
    fixture = TestBed.createComponent(CargosComponent);
    const component = fixture.componentInstance;
    component.ship = ship;
    component.showLoaded = showLoaded;
    component.showLoadObserve = cargoLoad.asObservable();
    fixture.detectChanges();
    return component;
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));
  const within = (el: HTMLElement, testId: string) => el.querySelector(`[data-testid="${testId}"]`) as HTMLElement;

  describe('Available mode', () => {
    it('renders a crate card per Cargo with name, Stock, Weight and a decorative picture', () => {
      cargoService.getCargos.and.returnValue(of([rum, ale]));
      render(aShip(), false);

      const cards = all('available-cargo');
      expect(cards.map((c) => within(c, 'cargo-name').textContent!.trim())).toEqual(['Rum', 'Ale']);
      expect(within(cards[0], 'cargo-stock').textContent).toContain('3 in stock');
      expect(cards[0].textContent).toContain('5.5');
      const picture = cards[0].querySelector('img')!;
      expect(picture.getAttribute('alt')).toBe('');
      expect(picture.getAttribute('src')).toContain('/cargo/rum.jpg');
    });

    it('keeps Cargo that is already loaded among the Available Cargo', () => {
      cargoService.getCargos.and.returnValue(of([rum, ale]));
      render(aShip({ cargo: [aCargo({ id: 'rum', name: 'Rum' })] }), false);

      expect(all('available-cargo').length).toBe(2);
    });

    it('fetches the Available Cargo again after every load or unload', () => {
      cargoService.getCargos.and.returnValues(of([rum]), of([{ ...rum, stock: 2 }]));
      render(aShip(), false);

      cargoLoad.next(aShip());
      fixture.detectChanges();

      expect(cargoService.getCargos).toHaveBeenCalledTimes(2);
      expect(within(all('available-cargo')[0], 'cargo-stock').textContent).toContain('2 in stock');
    });

    it('the Load button names the Cargo and emits loadRequested with it', () => {
      cargoService.getCargos.and.returnValue(of([rum, ale]));
      const component = render(aShip(), false);
      const requested: Cargo[] = [];
      component.loadRequested.subscribe((c) => requested.push(c));

      const button = within(all('available-cargo')[0], 'load-cargo-button') as HTMLButtonElement;
      expect(button.getAttribute('aria-label')).toBe('Load Rum');
      button.click();

      expect(requested).toEqual([rum]);
    });
  });

  describe('Loaded mode', () => {
    it('renders chips and the remove button unloads that Cargo', () => {
      const loaded = aCargo({ id: 'rum', name: 'Rum' });
      const ship = aShip({ cargo: [loaded], weight: 5.5 });
      const updated = aShip({ cargo: [], weight: 0 });
      shippingService.unloadCargo.and.returnValue(of(updated));
      const component = render(ship, true);
      const shipUpdates: Ship[] = [];
      component.shipUpdated.subscribe((s) => shipUpdates.push(s));

      const chips = all('loaded-cargo');
      expect(chips.length).toBe(1);
      expect(within(chips[0], 'cargo-name').textContent!.trim()).toBe('Rum');
      const remove = chips[0].querySelector('button') as HTMLButtonElement;
      expect(remove.getAttribute('aria-label')).toBe('Unload Rum');
      remove.click();

      expect(shippingService.unloadCargo).toHaveBeenCalledWith(ship, loaded);
      expect(shipUpdates).toEqual([updated]);
    });
  });
});
