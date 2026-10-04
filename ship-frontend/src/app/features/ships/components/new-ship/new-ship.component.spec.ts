import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideMockActions } from '@ngrx/effects/testing';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { NEVER } from 'rxjs';

import { NewShipComponent } from './new-ship.component';
import { Catain } from '../../../catains/model/catain';
import * as CatainsActions from '../../../catains/store/catains.actions';
import * as ShipActions from '../../store/actions/ship.actions';
import { aCatain } from '../../../../../testing/fixtures';

describe('NewShipComponent (Create Ship with Catain cards)', () => {
  let fixture: ComponentFixture<NewShipComponent>;
  let store: MockStore;
  let location: jasmine.SpyObj<Location>;

  const whiskers = aCatain();
  const mittens = aCatain({ id: 'c1a7a1n0-0000-4000-8000-000000000002', name: 'Mittens' });

  async function render(catains: Catain[] = [whiskers, mittens]): Promise<void> {
    location = jasmine.createSpyObj<Location>('Location', ['back']);
    TestBed.configureTestingModule({
      imports: [NewShipComponent],
      providers: [
        provideRouter([]),
        provideMockActions(() => NEVER),
        { provide: Location, useValue: location },
        provideMockStore({ initialState: { catains: { catains, loading: false, error: null } } }),
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(NewShipComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));

  const radios = (): HTMLInputElement[] => all('catain-radio') as HTMLInputElement[];

  async function choose(index: number): Promise<void> {
    const radio = radios()[index];
    radio.checked = true;
    radio.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  async function fillName(name: string): Promise<void> {
    const input = all('ship-name-input')[0] as HTMLInputElement;
    input.value = name;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const createButton = (): HTMLButtonElement => all('create-ship')[0] as HTMLButtonElement;

  it('Every Catain is offered as a radio with its name as alt text', async () => {
    await render();

    expect(radios().length).toBe(2);
    expect(all('catain-image').map((img) => img.getAttribute('alt'))).toEqual(['Whiskers', 'Mittens']);
  });

  it('Loading the screen requests the Catains', async () => {
    await render();

    expect(store.dispatch).toHaveBeenCalledWith(CatainsActions.loadCatains());
  });

  it('Choosing a Catain marks it selected and badged', async () => {
    await render();

    await choose(1);

    const cards = all('catain-card');
    expect(cards.map((card) => card.classList.contains('catain--selected'))).toEqual([false, true]);
    expect(all('catain-selected-badge').length).toBe(1);
    expect(cards[1].contains(all('catain-selected-badge')[0])).toBeTrue();
    expect(all('catain-selected-badge')[0].textContent).toContain('Selected');
  });

  it('A Catain can be chosen with the keyboard', async () => {
    await render();

    expect(radios().every((radio) => radio.type === 'radio' && radio.name === 'catain')).toBeTrue();
    expect(radios().every((radio) => radio.tabIndex !== -1)).toBeTrue();
    expect(radios().every((radio) => getComputedStyle(radio).display !== 'none')).toBeTrue();

    await choose(0);

    expect(radios()[0].checked).toBeTrue();
    expect(all('catain-card')[0].classList).toContain('catain--selected');
  });

  it('Create Ship is disabled until name and Catain are set, then adds the Ship', async () => {
    await render();
    expect(createButton().disabled).toBeTrue();

    await fillName('Black Pearl');
    expect(createButton().disabled).toBeTrue();

    await choose(0);
    expect(createButton().disabled).toBeFalse();

    createButton().click();

    expect(store.dispatch).toHaveBeenCalledWith(
      ShipActions.addShip({ name: 'Black Pearl', catainId: whiskers.id }),
    );
  });

  it('Cancel navigates back', async () => {
    await render();

    (all('cancel')[0] as HTMLButtonElement).click();

    expect(location.back).toHaveBeenCalled();
  });

  it('A Catain Image that fails to load shows the fallback icon', async () => {
    await render();
    expect(all('catain-image-fallback')).toEqual([]);

    all('catain-image')[0].dispatchEvent(new Event('error'));
    fixture.detectChanges();

    expect(all('catain-image').length).toBe(1);
    expect(all('catain-image-fallback').length).toBe(1);
  });
});
