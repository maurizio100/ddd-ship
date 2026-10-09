import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Location } from '@angular/common';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { environment } from '../../../../../environments/environment';
import { AvailableCargo } from '../../models/cargo';
import { Ship } from '../../models/ship';

import { ShipDetailComponent } from './ship-detail.component';
import { CargoService } from '../../services/cargo.service';
import { DisembarkService } from '../../services/disembark.service';
import { HarborService } from '../../services/harbor.service';
import { ShippingService } from '../../services/shipping.service';
import { aCargo, anAvailableCargo, aShip, aShippingSummary, someKnownHarbors } from '../../../../../testing/fixtures';

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
    cards().filter((c) => c.getAttribute('aria-checked') === 'true').map((c) => c.querySelector('.harbor-card__name')!.textContent!.trim());

  const releaseButton = (): HTMLButtonElement => byTestId('shipping-release-button') as HTMLButtonElement;

  function clickRelease(): void {
    byTestId('shipping-release-button')!.click();
    fixture.detectChanges();
  }

  it('The release controls sit beside the ship, after it, and outside the drop zone', () => {
    render(['Port Royal']);

    const aside = fixture.nativeElement.querySelector('.aside') as HTMLElement;
    const dropZone = byTestId('ship-drop-zone')!;
    const radiogroup = byTestId('shipping-destination-harbors')!;

    expect(aside).not.toBeNull();
    expect(aside.contains(dropZone)).toBeTrue();
    expect(aside.contains(releaseButton())).toBeTrue();
    expect(dropZone.compareDocumentPosition(releaseButton()) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(dropZone.contains(releaseButton())).toBeFalse();
    expect(dropZone.contains(radiogroup)).toBeFalse();
  });

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
    expect(releaseButton().textContent!.trim()).toBe('Release');
    expect(byTestId('shipping-destination-harbor-selected')).toBeNull();

    choose('Nassau');

    expect(checked()).toEqual(['Nassau']);
    expect(fixture.nativeElement.querySelectorAll('[data-testid="shipping-destination-harbor-selected"]').length).toBe(1);
    expect(cards()[1].querySelector('[data-testid="shipping-destination-harbor-selected"]')).not.toBeNull();
    expect(releaseButton().disabled).toBeFalse();
    expect(releaseButton().textContent!.replace(/\s+/g, ' ').trim()).toBe('Release → Nassau');
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

describe('ShipDetailComponent (Load Cargo by dragging it onto the ship)', () => {
  const base = environment.baseUrl;
  const ale = aCargo({ id: 'ale', name: 'Ale', weight: 2 });
  const rum = anAvailableCargo({ id: 'rum', name: 'Rum', weight: 5.5, stock: 3 });
  const gold = anAvailableCargo({ id: 'gold', name: 'Gold', weight: 1, stock: 4 });
  // Background: a ship with a new Shipping (Ale already aboard) and Rum among the Available Cargo
  const preparedShip = (): Ship => aShip({ cargo: [ale], weight: 2 });

  let fixture: ComponentFixture<ShipDetailComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const harborService = jasmine.createSpyObj<HarborService>('HarborService', ['getKnownHarbors']);
    harborService.getKnownHarbors.and.returnValue(of(someKnownHarbors()));
    TestBed.configureTestingModule({
      imports: [ShipDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: HarborService, useValue: harborService },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
        { provide: Location, useValue: jasmine.createSpyObj<Location>('Location', ['back']) },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: aShip().id }) } } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  function render(ship: Ship, available: AvailableCargo[]): void {
    fixture = TestBed.createComponent(ShipDetailComponent);
    fixture.detectChanges();
    httpMock.expectOne(`${base}/ships/${ship.id}`).flush(ship);
    fixture.detectChanges();
    httpMock.expectOne(`${base}/cargos`).flush(available);
    fixture.detectChanges();
  }

  /** The Available Cargo are fetched again after a load attempt. */
  function refetchAvailable(available: AvailableCargo[]): void {
    httpMock.expectOne(`${base}/cargos`).flush(available);
    fixture.detectChanges();
  }

  const byTestId = (testId: string, root: ParentNode = fixture.nativeElement): HTMLElement | null =>
    root.querySelector(`[data-testid="${testId}"]`);

  const names = (testId: string): string[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`)).map((el: any) =>
      el.querySelector('[data-testid="cargo-name"]').textContent.trim()
    );

  const loadedCargo = (): string[] => names('loaded-cargo');
  const availableCargo = (): string[] => names('available-cargo');
  const weightText = (): string => (byTestId('current-weight')?.textContent ?? '').replace(/\s+/g, ' ');
  const stockOf = (name: string): string => {
    const card = Array.from(fixture.nativeElement.querySelectorAll('[data-testid="available-cargo"]')).find(
      (el: any) => el.querySelector('[data-testid="cargo-name"]').textContent.trim() === name
    ) as HTMLElement;
    return card.querySelector('[data-testid="cargo-stock"]')!.textContent!.trim();
  };

  /** What CDK emits when a crate of the Available Cargo list is released over [dropTarget]. */
  function dropRum(overShip: boolean): void {
    const availableList = { id: 'available-cargo-list' };
    const shipList = { id: 'ship-drop-list' };
    const event = {
      previousContainer: availableList,
      container: overShip ? shipList : availableList,
      item: { data: rum },
    } as unknown as CdkDragDrop<any>;
    fixture.componentInstance.onCargoDropped(event);
    fixture.detectChanges();
  }

  afterEach(() => httpMock.verify());

  it('Dragging Cargo onto the ship loads it', () => {
    render(preparedShip(), [rum, gold]);
    expect(stockOf('Rum')).toContain('3');

    // When the User drags Rum onto the ship
    dropRum(true);
    const post = httpMock.expectOne(`${base}/ships/${aShip().id}/cargos`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ cargoId: 'rum' });
    post.flush({ ...preparedShip(), cargo: [ale, rum], weight: 7.5 });
    fixture.detectChanges();
    // the Harbor's Stock changed, so the Available Cargo are fetched again (Rum: Stock 2)
    refetchAvailable([gold, { ...rum, stock: 2 }]);

    // Then Rum is among the Loaded Cargo, the Current Weight grew by 5.5 and the Stock shown is one lower
    expect(loadedCargo()).toContain('Rum');
    expect(weightText()).toMatch(/7\.5\s*\/\s*15/);
    expect(stockOf('Rum')).toContain('2');
  });

  it('loading the same Cargo again adds another one aboard', () => {
    const ship = aShip({ cargo: [rum], weight: 5.5 });
    render(ship, [rum, gold]);

    // When the User drags Rum onto the ship again
    dropRum(true);
    const post = httpMock.expectOne(`${base}/ships/${aShip().id}/cargos`);
    expect(post.request.body).toEqual({ cargoId: 'rum' });
    post.flush({ ...ship, cargo: [rum, rum], weight: 11 });
    fixture.detectChanges();
    refetchAvailable([gold, { ...rum, stock: 2 }]);

    // Then 2 Rum are among the Loaded Cargo and the Current Weight grew by 5.5
    expect(loadedCargo()).toEqual(['Rum', 'Rum']);
    expect(weightText()).toMatch(/11\s*\/\s*15/);
  });

  it('Cargo can be loaded without dragging', () => {
    render(preparedShip(), [rum, gold]);

    // When the User loads Rum using only the keyboard: a focusable button on Rum's card
    const card = Array.from(fixture.nativeElement.querySelectorAll('[data-testid="available-cargo"]')).find((el: any) =>
      el.textContent.includes('Rum')
    ) as HTMLElement;
    const button = byTestId('load-cargo-button', card) as HTMLButtonElement;
    expect(button.tagName).toBe('BUTTON');
    expect(button.tabIndex).toBeGreaterThanOrEqual(0);
    button.click();
    const post = httpMock.expectOne(`${base}/ships/${aShip().id}/cargos`);
    expect(post.request.body).toEqual({ cargoId: 'rum' });
    post.flush({ ...preparedShip(), cargo: [ale, rum], weight: 7.5 });
    fixture.detectChanges();
    refetchAvailable([gold]);

    // Then Rum is among the Loaded Cargo
    expect(loadedCargo()).toContain('Rum');
  });

  it('Cargo dropped beside the ship is not loaded', () => {
    render(preparedShip(), [rum, gold]);
    expect(weightText()).toMatch(/2\s*\/\s*15/);

    // When the User drags Rum and lets go beside the ship
    dropRum(false);

    // Then Rum is not among the Loaded Cargo and the Current Weight is unchanged
    httpMock.expectNone(`${base}/ships/${aShip().id}/cargos`);
    expect(loadedCargo()).toEqual(['Ale']);
    expect(weightText()).toMatch(/2\s*\/\s*15/);
    expect(availableCargo()).toContain('Rum');
  });

  describe('A rejected load is explained at the ship', () => {
    const rows = [
      {
        situation: 'loading Rum would take the ship over its Max Weight',
        title: 'Ship too heavy',
        detail: 'Loading Rum would exceed the Max Weight of 15.0',
        reason: 'Rum would exceed the Max Weight of 15.0',
        ship: () => aShip({ cargo: [ale], weight: 12 }),
        loadedAfter: ['Ale'],
        availableAfter: [rum, gold],
        weight: /12\s*\/\s*15/,
      },
      {
        situation: 'loading a second Rum would take the ship over its Max Weight',
        title: 'Ship too heavy',
        detail: 'Loading Rum would exceed the Max Weight of 15.0',
        reason: 'Rum would exceed the Max Weight of 15.0',
        ship: () => aShip({ cargo: [rum], weight: 5.5 }),
        loadedAfter: ['Rum'],
        availableAfter: [rum, gold],
        weight: /5\.5\s*\/\s*15/,
      },
      {
        situation: 'the Stock of Rum ran out after the Available Cargo was shown',
        title: 'Cargo out of Stock',
        detail: 'Rum is out of Stock',
        reason: 'Rum is out of Stock',
        ship: () => aShip({ cargo: [ale], weight: 2 }),
        loadedAfter: ['Ale'],
        availableAfter: [gold],
        weight: /2\s*\/\s*15/,
      },
    ];

    for (const row of rows) {
      it(`when ${row.situation}`, () => {
        render(row.ship(), [rum, gold]);
        expect(weightText()).toMatch(row.weight);

        // When the User drags Rum onto the ship
        dropRum(true);
        httpMock
          .expectOne(`${base}/ships/${aShip().id}/cargos`)
          .flush({ title: row.title, status: 409, detail: row.detail }, { status: 409, statusText: 'Conflict' });
        fixture.detectChanges();
        refetchAvailable(row.availableAfter);

        // Then the User is told, at the ship, that <reason>, and the Loaded Cargo is unchanged
        const alert = byTestId('load-rejection', byTestId('ship-drop-zone')!);
        expect(alert).not.toBeNull();
        expect(alert!.getAttribute('role')).toBe('alert');
        expect(alert!.textContent!.trim()).toBe(row.reason);
        expect(loadedCargo()).toEqual(row.loadedAfter);
        expect(weightText()).toMatch(row.weight);
        expect(availableCargo().includes('Rum')).toBe(row.availableAfter.includes(rum));
      });
    }
  });
});

describe('ShipDetailComponent (loading screen details)', () => {
  const base = environment.baseUrl;
  const rum = anAvailableCargo({ id: 'rum', name: 'Rum', weight: 5.5, stock: 3 });
  let fixture: ComponentFixture<ShipDetailComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const harborService = jasmine.createSpyObj<HarborService>('HarborService', ['getKnownHarbors']);
    harborService.getKnownHarbors.and.returnValue(of(someKnownHarbors()));
    TestBed.configureTestingModule({
      imports: [ShipDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: HarborService, useValue: harborService },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
        { provide: Location, useValue: jasmine.createSpyObj<Location>('Location', ['back']) },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: aShip().id }) } } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function render(ship: Ship): void {
    fixture = TestBed.createComponent(ShipDetailComponent);
    fixture.detectChanges();
    httpMock.expectOne(`${base}/ships/${ship.id}`).flush(ship);
    fixture.detectChanges();
    httpMock.expectOne(`${base}/cargos`).flush([rum]);
    fixture.detectChanges();
  }

  const byTestId = (testId: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${testId}"]`);
  const overlay = (): string => (byTestId('ship-drop-zone')!.textContent ?? '');

  function drop(): void {
    fixture.componentInstance.onCargoDropped({
      previousContainer: { id: 'available-cargo-list' },
      container: { id: 'ship-drop-list' },
      item: { data: rum },
    } as unknown as CdkDragDrop<any>);
  }

  function rejectLoad(): void {
    drop();
    httpMock
      .expectOne(`${base}/ships/${aShip().id}/cargos`)
      .flush(
        { title: 'Cargo out of Stock', status: 409, detail: 'Rum is out of Stock' },
        { status: 409, statusText: 'Conflict' }
      );
    httpMock.expectOne(`${base}/cargos`).flush([rum]);
    fixture.detectChanges();
  }

  it('shows "Heave it aboard!" while a crate is over the ship and hides it after leaving', () => {
    render(aShip());
    expect(overlay()).not.toContain('Heave it aboard!');

    fixture.componentInstance.onDragEntered();
    fixture.detectChanges();
    expect(overlay()).toContain('Heave it aboard!');

    fixture.componentInstance.onDragExited();
    fixture.detectChanges();
    expect(overlay()).not.toContain('Heave it aboard!');
  });

  it('hides "Heave it aboard!" after a drop', () => {
    render(aShip());
    fixture.componentInstance.onDragEntered();
    fixture.detectChanges();

    drop();
    httpMock.expectOne(`${base}/ships/${aShip().id}/cargos`).flush({ ...aShip(), cargo: [rum], weight: 5.5 });
    httpMock.expectOne(`${base}/cargos`).flush([rum]);
    fixture.detectChanges();

    expect(overlay()).not.toContain('Heave it aboard!');
  });

  it('a later successful load clears the previous rejection', () => {
    render(aShip());
    rejectLoad();
    expect(byTestId('load-rejection')).not.toBeNull();

    drop();
    httpMock.expectOne(`${base}/ships/${aShip().id}/cargos`).flush({ ...aShip(), cargo: [rum], weight: 5.5 });
    httpMock.expectOne(`${base}/cargos`).flush([rum]);
    fixture.detectChanges();

    expect(byTestId('load-rejection')).toBeNull();
  });

  it('a later unload clears the previous rejection', () => {
    render(aShip({ cargo: [rum], weight: 5.5 }));
    rejectLoad();
    expect(byTestId('load-rejection')).not.toBeNull();

    fixture.componentInstance.onShipLoadUpdated(aShip({ cargo: [], weight: 0 }));
    httpMock.expectOne(`${base}/cargos`).flush([rum]);
    fixture.detectChanges();

    expect(byTestId('load-rejection')).toBeNull();
  });

  it('shows the Current Weight as x / Max Weight with a progress bar', () => {
    render(aShip({ weight: 7.5, maxweight: 15 }));

    expect((byTestId('current-weight')!.textContent ?? '').replace(/\s+/g, ' ')).toContain('7.5 / 15');
    const bar = fixture.nativeElement.querySelector('[role="progressbar"]');
    expect(bar.getAttribute('aria-valuenow')).toBe('7.5');
    expect(bar.getAttribute('aria-valuemax')).toBe('15');
    expect(fixture.nativeElement.querySelector('.weight--warning')).toBeNull();
  });

  it('warns when the Current Weight passes 85 % of the Max Weight', () => {
    render(aShip({ weight: 13, maxweight: 15 }));

    expect(fixture.nativeElement.querySelector('.weight--warning')).not.toBeNull();
  });
});
