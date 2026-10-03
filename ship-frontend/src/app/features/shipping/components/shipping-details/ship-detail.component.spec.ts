import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Location } from '@angular/common';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ShipDetailComponent } from './ship-detail.component';
import { CargoService } from '../../services/cargo.service';
import { DisembarkService } from '../../services/disembark.service';
import { HarborService } from '../../services/harbor.service';
import { ShippingService } from '../../services/shipping.service';
import { aShip, aShippingSummary, someKnownHarbors } from '../../../../../testing/fixtures';

describe('ShipDetailComponent (Release to a Destination Harbor)', () => {
  const ship = aShip();

  let fixture: ComponentFixture<ShipDetailComponent>;
  let disembarkService: jasmine.SpyObj<DisembarkService>;
  let harborService: jasmine.SpyObj<HarborService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const shippingService = jasmine.createSpyObj<ShippingService>('ShippingService', ['getShip', 'loadCargo', 'unloadCargo']);
    shippingService.getShip.and.returnValue(of(ship));
    const cargoService = jasmine.createSpyObj<CargoService>('CargoService', ['getCargos']);
    cargoService.getCargos.and.returnValue(of([]));
    disembarkService = jasmine.createSpyObj<DisembarkService>('DisembarkService', ['releaseShip']);
    harborService = jasmine.createSpyObj<HarborService>('HarborService', ['getKnownHarbors']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [ShipDetailComponent],
      providers: [
        { provide: ShippingService, useValue: shippingService },
        { provide: CargoService, useValue: cargoService },
        { provide: DisembarkService, useValue: disembarkService },
        { provide: HarborService, useValue: harborService },
        { provide: Router, useValue: router },
        { provide: Location, useValue: jasmine.createSpyObj<Location>('Location', ['back']) },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: ship.id }) } } },
      ],
    });
  });

  function render(knownHarbors: string[]): void {
    harborService.getKnownHarbors.and.returnValue(of(someKnownHarbors({ knownHarbors })));
    fixture = TestBed.createComponent(ShipDetailComponent);
    fixture.detectChanges();
  }

  const byTestId = (testId: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${testId}"]`);

  const destinationHarborChoices = (): string[] =>
    Array.from(fixture.nativeElement.querySelectorAll('[data-testid="shipping-destination-harbor-option"]')).map(
      (option: any) => option.querySelector('.harbor-card__name').textContent.trim()
    );

  const cards = (): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('[data-testid="shipping-destination-harbor-option"]'));

  function choose(harbor: string): void {
    const card = cards().find((c) => c.textContent!.includes(harbor))!;
    card.click();
    fixture.detectChanges();
  }

  function press(index: number, key: string): void {
    cards()[index].dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true }));
    fixture.detectChanges();
  }

  const checked = (): string[] =>
    cards().filter((c) => c.getAttribute('aria-checked') === 'true').map((c) => c.textContent!.replace('✓', '').trim());

  const releaseButton = (): HTMLButtonElement => byTestId('shipping-release-button') as HTMLButtonElement;

  function clickRelease(): void {
    byTestId('shipping-release-button')!.click();
    fixture.detectChanges();
  }

  it('The Destination Harbor choices are the Known Harbors', () => {
    // Given "Tortuga" knows the Harbors "Port Royal" and "Nassau" and a ship is being prepared
    render(['Port Royal', 'Nassau']);

    // Then the Destination Harbor choices for the ship are "Port Royal" and "Nassau"
    expect(destinationHarborChoices()).toEqual(['Port Royal', 'Nassau']);
    expect(byTestId('shipping-no-known-harbor')).toBeNull();
  });

  it('A ship is Released to a Known Harbor', () => {
    // Given "Tortuga" knows the Harbor "Port Royal" and a ship is being prepared
    render(['Port Royal']);
    const summary = aShippingSummary({ shipId: ship.id });
    disembarkService.releaseShip.and.returnValue(of(summary));

    // When the User Releases the ship to "Port Royal"
    choose('Port Royal');
    clickRelease();

    // Then the ship is Released to "Port Royal" and the Shipping Summary is shown
    expect(disembarkService.releaseShip).toHaveBeenCalledOnceWith(ship, 'Port Royal');
    expect(router.navigate).toHaveBeenCalledOnceWith([`/ships/${ship.id}/shipping/${summary.id}`]);
  });

  it('A ship cannot be Released to an unknown Harbor', () => {
    // Given a ship is being prepared and the backend does not know the chosen Harbor
    render(['Port Royal']);
    disembarkService.releaseShip.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { title: 'Unknown Destination Harbor', status: 409, detail: 'Atlantis is not a Known Harbor' },
          })
      )
    );

    // When the User Releases the ship
    choose('Port Royal');
    clickRelease();

    // Then the Release is rejected and the ship stays on the page, still being prepared
    expect(byTestId('shipping-release-rejection')?.textContent).toContain('Atlantis is not a Known Harbor');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('A ship cannot be Released without a Known Harbor', () => {
    // Given "Tortuga" knows no other Harbor and a ship is being prepared
    render([]);

    // Then the ship cannot be Released
    expect(byTestId('shipping-no-known-harbor')?.textContent).toContain('No Known Harbor to sail to yet');
    expect((byTestId('shipping-release-button') as HTMLButtonElement).disabled).toBeTrue();
    clickRelease();
    expect(disembarkService.releaseShip).not.toHaveBeenCalled();
  });

  it('Selecting a card marks only it as checked and names the Harbor on the button', () => {
    render(['Port Royal', 'Nassau']);
    expect(releaseButton().disabled).toBeTrue();
    expect(releaseButton().textContent!.trim()).toBe('Start Journey');
    expect(byTestId('shipping-destination-harbor-selected')).toBeNull();

    choose('Nassau');

    expect(checked()).toEqual(['Nassau']);
    expect(fixture.nativeElement.querySelectorAll('[data-testid="shipping-destination-harbor-selected"]').length).toBe(1);
    expect(cards()[1].querySelector('[data-testid="shipping-destination-harbor-selected"]')).not.toBeNull();
    expect(releaseButton().disabled).toBeFalse();
    expect(releaseButton().textContent!.replace(/\s+/g, ' ').trim()).toBe('Start Journey → Nassau');
  });

  it('The cards form a radio group with a single tab stop', () => {
    render(['Port Royal', 'Nassau']);
    expect(byTestId('shipping-destination-harbors')!.getAttribute('role')).toBe('radiogroup');
    expect(cards().every((c) => c.getAttribute('role') === 'radio')).toBeTrue();
    expect(cards().map((c) => c.getAttribute('tabindex'))).toEqual(['0', '-1']);
    choose('Nassau');
    expect(cards().map((c) => c.getAttribute('tabindex'))).toEqual(['-1', '0']);
  });

  it('Arrow keys move the selection with wrap and Space/Enter select the focused card', () => {
    render(['Port Royal', 'Nassau']);
    press(0, 'ArrowRight');
    expect(checked()).toEqual(['Nassau']);
    press(1, 'ArrowRight');
    expect(checked()).toEqual(['Port Royal']);
    press(0, 'ArrowLeft');
    expect(checked()).toEqual(['Nassau']);
    press(1, 'ArrowUp');
    expect(checked()).toEqual(['Port Royal']);
    press(0, 'ArrowDown');
    expect(checked()).toEqual(['Nassau']);
    press(0, ' ');
    expect(checked()).toEqual(['Port Royal']);
    press(1, 'Enter');
    expect(checked()).toEqual(['Nassau']);
  });

  it('Without a Known Harbor there is no radio group, only the hint', () => {
    render([]);
    expect(byTestId('shipping-destination-harbors')).toBeNull();
    expect(byTestId('shipping-no-known-harbor')).not.toBeNull();
  });
});
