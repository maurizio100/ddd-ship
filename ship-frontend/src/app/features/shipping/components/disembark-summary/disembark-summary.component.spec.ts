import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of } from 'rxjs';

import { DisembarkSummaryComponent } from './disembark-summary.component';
import { CargoService } from '../../services/cargo.service';
import { DisembarkService } from '../../services/disembark.service';
import { ShippingService } from '../../services/shipping.service';
import { environment } from '../../../../../environments/environment';
import { aShippingSummary } from '../../../../../testing/fixtures';

describe('DisembarkSummaryComponent', () => {
  it('shows the Destination Harbor of a ship at sea', () => {
    const summary = aShippingSummary({ destinationHarbor: 'Port Royal' });
    const disembarkService = jasmine.createSpyObj<DisembarkService>('DisembarkService', ['getShipping']);
    disembarkService.getShipping.and.returnValue(of(summary));

    TestBed.configureTestingModule({
      imports: [DisembarkSummaryComponent],
      providers: [
        { provide: DisembarkService, useValue: disembarkService },
        { provide: CargoService, useValue: jasmine.createSpyObj<CargoService>('CargoService', ['getCargos']) },
        { provide: ShippingService, useValue: jasmine.createSpyObj<ShippingService>('ShippingService', ['loadCargo']) },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ shipId: summary.shipId, shippingId: summary.id }) } },
        },
      ],
    });
    const fixture = TestBed.createComponent(DisembarkSummaryComponent);
    fixture.detectChanges();

    const destination = fixture.nativeElement.querySelector('[data-testid="shipping-summary-destination-harbor"]');
    expect(destination?.textContent).toContain('Port Royal');
  });

  it('builds the Catain image URL from the environment base URL', () => {
    TestBed.configureTestingModule({
      imports: [DisembarkSummaryComponent],
      providers: [
        { provide: DisembarkService, useValue: jasmine.createSpyObj<DisembarkService>('DisembarkService', ['getShipping']) },
        { provide: CargoService, useValue: jasmine.createSpyObj<CargoService>('CargoService', ['getCargos']) },
        { provide: ShippingService, useValue: jasmine.createSpyObj<ShippingService>('ShippingService', ['loadCargo']) },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({}) } } },
      ],
    });
    const component = TestBed.createComponent(DisembarkSummaryComponent).componentInstance;

    expect(component.getImageUrl('c1')).toBe(`${environment.baseUrl}/catains/c1/image`);
  });

  it('renders the Shipping Summary heading, ship and Catain names, and navigates to all Ships', () => {
    const summary = aShippingSummary({ name: 'Black Pearl', catainName: 'Furry Jones' });
    const disembarkService = jasmine.createSpyObj<DisembarkService>('DisembarkService', ['getShipping']);
    disembarkService.getShipping.and.returnValue(of(summary));
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [DisembarkSummaryComponent],
      providers: [
        { provide: DisembarkService, useValue: disembarkService },
        { provide: CargoService, useValue: jasmine.createSpyObj<CargoService>('CargoService', ['getCargos']) },
        { provide: ShippingService, useValue: jasmine.createSpyObj<ShippingService>('ShippingService', ['loadCargo']) },
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ shipId: summary.shipId, shippingId: summary.id }) } },
        },
      ],
    });
    const fixture = TestBed.createComponent(DisembarkSummaryComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('h1')?.textContent?.trim()).toBe('Shipping Summary');
    expect(el.textContent).toContain('Black Pearl');
    expect(el.textContent).toContain('Furry Jones');
    expect(el.querySelector('img')?.getAttribute('alt')).toBe('Furry Jones');
    const button = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.trim() === 'View all Ships')!;
    button.click();
    expect(router.navigate).toHaveBeenCalledWith(['/ships']);
  });
});
