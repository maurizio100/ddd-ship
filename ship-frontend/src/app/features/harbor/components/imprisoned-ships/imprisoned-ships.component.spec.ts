import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

import { ImprisonedShipsComponent } from './imprisoned-ships.component';
import { ImprisonedShip } from '../../models/imprisoned-ship';
import * as HarborActions from '../../store/harbor.actions';
import { aSavings, anImprisonedShip } from '../../../../../testing/fixtures';

describe('ImprisonedShipsComponent (Free an Imprisoned Ship by paying its Fine)', () => {
  let fixture: ComponentFixture<ImprisonedShipsComponent>;
  let store: MockStore;

  function render(
    imprisonedShips: ImprisonedShip[],
    harbor: Record<string, unknown> = {},
  ): void {
    TestBed.configureTestingModule({
      imports: [ImprisonedShipsComponent],
      providers: [
        provideMockStore({
          initialState: {
            harbor: {
              stock: [], savings: aSavings(), incomingShips: [], loading: false, error: null,
              imprisonedShips, fineNotice: null, fineRefusal: null, payingFineShipIds: [],
              ...harbor,
            },
            catains: { catains: [], loading: false, error: null },
          },
        }),
      ],
    });
    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');
    fixture = TestBed.createComponent(ImprisonedShipsComponent);
    fixture.detectChanges();
  }

  const all = (testId: string): HTMLElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll(`[data-testid="${testId}"]`));
  const texts = (testId: string): string[] => all(testId).map((el) => el.textContent!.trim());

  it('The Imprisoned Ships are shown with their Fine', () => {
    render([anImprisonedShip()]);

    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.loadImprisonedShips());
    expect(texts('harbor-imprisoned-ship-name')).toEqual(['Salty Whisker']);
    expect(texts('harbor-imprisoned-ship-fine')).toEqual(['50.00 $']);
  });

  it('The User sees whether the Savings cover the Fine', () => {
    render([anImprisonedShip({ savingsCoverFine: true })]);
    expect(texts('harbor-imprisoned-ship-coverage')).toEqual(['The Savings cover the Fine']);
    expect((all('harbor-imprisoned-ship-pay-fine')[0] as HTMLButtonElement).disabled).toBeFalse();
    TestBed.resetTestingModule();

    render([anImprisonedShip({ savingsCoverFine: false })], { savings: aSavings({ amount: '30.00' }) });
    expect(texts('harbor-imprisoned-ship-coverage')).toEqual(['The Savings do not cover the Fine of 50.00 $']);
    expect((all('harbor-imprisoned-ship-pay-fine')[0] as HTMLButtonElement).disabled).toBeTrue();
  });

  it('Paying the Fine frees the ship', () => {
    const saltyWhisker = anImprisonedShip();
    render([saltyWhisker]);

    (all('harbor-imprisoned-ship-pay-fine')[0] as HTMLButtonElement).click();

    expect(store.dispatch).toHaveBeenCalledWith(HarborActions.payFine({ shipId: saltyWhisker.shipId }));
  });

  it('The ship stays imprisoned when the Savings fall short', () => {
    const saltyWhisker = anImprisonedShip({ savingsCoverFine: false });
    render([saltyWhisker], {
      savings: aSavings({ amount: '30.00' }),
      fineRefusal: { shipId: saltyWhisker.shipId, message: 'The Savings do not cover the Fine of 50.00 $' },
    });

    expect(texts('harbor-imprisoned-ship-fine-refusal')).toEqual(['The Savings do not cover the Fine of 50.00 $']);
    expect(all('harbor-imprisoned-ship-fine-refusal')[0].getAttribute('role')).toBe('alert');
    expect(texts('harbor-imprisoned-ship-name')).toEqual(['Salty Whisker']);
  });
});
