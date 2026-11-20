import {
  selectFineNotice,
  selectFineRefusal,
  selectHasImprisonedShips,
  selectImprisonedShips,
  selectIncomingShips,
  selectRecruitPool,
  selectPayingFineShipIds,
  selectPurchaseRefusal,
  selectRefuseFailure,
  selectRefusingShipIds,
  selectSavings,
  selectStock,
  selectStockError,
  selectStockLoading,
  selectUnloadingShipIds,
  selectUnloadRefusal,
} from './harbor.selectors';
import { aFinePayment, aSavings, aStockedCargo, anImprisonedShip, anIncomingShipListing } from '../../../../testing/fixtures';

describe('harbor selectors', () => {
  const stock = [aStockedCargo({ name: 'Rum', quantity: 0 })];
  const savings = aSavings({ amount: '640.50' });
  const incomingShips = [anIncomingShipListing()];
  const recruitPool = [{ id: 'r1', name: 'Mittens', trait: 'Sure-pawed', portrait: { fur: 'ginger', coat: 'tabby', eyes: 'green', accessory: 'none' } as const, hiringFee: '50.00' }];
  const root = {
    harbor: {
      stock,
      savings,
      incomingShips,
      recruitPool,
      loading: true,
      error: 'boom',
      purchaseRefusal: 'The Savings do not cover 100.00 $',
      unloadRefusal: 'The Savings do not cover the Delivery Price of 115.00 $',
      unloadingShipIds: ['s1'],
      refuseFailure: 'Salty Whisker is not an Incoming Ship anymore',
      refusingShipIds: ['s2'],
    },
  };

  it('selectStock reads stock', () => expect(selectStock(root)).toEqual(stock));
  it('selectStockLoading reads loading', () => expect(selectStockLoading(root)).toBeTrue());
  it('selectStockError reads error', () => expect(selectStockError(root)).toBe('boom'));
  it('selectSavings reads savings', () => expect(selectSavings(root)).toEqual(savings));
  it('selectPurchaseRefusal reads the purchase refusal', () =>
    expect(selectPurchaseRefusal(root)).toBe('The Savings do not cover 100.00 $'));
  it('selectUnloadRefusal reads the unload refusal', () =>
    expect(selectUnloadRefusal(root)).toBe('The Savings do not cover the Delivery Price of 115.00 $'));
  it('selectIncomingShips reads the Incoming Ships', () => expect(selectIncomingShips(root)).toEqual(incomingShips));
  it('selectRecruitPool reads the Recruit Pool', () => expect(selectRecruitPool(root)).toEqual(recruitPool));
  it('selectUnloadingShipIds reads unloadingShipIds', () => expect(selectUnloadingShipIds(root)).toEqual(['s1']));
  it('selectRefuseFailure reads the refuse failure', () =>
    expect(selectRefuseFailure(root)).toBe('Salty Whisker is not an Incoming Ship anymore'));
  it('selectRefusingShipIds reads refusingShipIds', () => expect(selectRefusingShipIds(root)).toEqual(['s2']));

  describe('the Imprisoned Ships', () => {
    const imprisoned = {
      harbor: {
        ...root.harbor,
        imprisonedShips: [anImprisonedShip()],
        fineNotice: aFinePayment(),
        fineRefusal: { shipId: 's3', message: 'The Savings do not cover the Fine of 50.00 $' },
        payingFineShipIds: ['s3'],
      },
    };

    it('selectImprisonedShips reads the Imprisoned Ships', () =>
      expect(selectImprisonedShips(imprisoned)).toEqual([anImprisonedShip()]));
    it('selectFineNotice reads the Fine notice', () => expect(selectFineNotice(imprisoned)).toEqual(aFinePayment()));
    it('selectFineRefusal reads the Fine refusal', () =>
      expect(selectFineRefusal(imprisoned)).toEqual({ shipId: 's3', message: 'The Savings do not cover the Fine of 50.00 $' }));
    it('selectPayingFineShipIds reads payingFineShipIds', () => expect(selectPayingFineShipIds(imprisoned)).toEqual(['s3']));

    it('the Fine selectors read nothing while the harbor feature is not registered', () => {
      expect(selectImprisonedShips({})).toEqual([]);
      expect(selectFineNotice({})).toBeNull();
      expect(selectFineRefusal({})).toBeNull();
      expect(selectPayingFineShipIds({})).toEqual([]);
    });

    it('selectHasImprisonedShips is false without a harbor feature and with none, and true with one', () => {
      expect(selectHasImprisonedShips({})).toBeFalse();
      expect(selectHasImprisonedShips(root as never)).toBeFalse();
      expect(selectHasImprisonedShips({ harbor: { ...root.harbor, imprisonedShips: [] } } as never)).toBeFalse();
      expect(selectHasImprisonedShips(imprisoned as never)).toBeTrue();
    });
  });
});
