import {SHIPS_ROUTES} from './ShipRouting';
import {CATAIN_STORE_PROVIDERS} from '../catains/store/CatainStoreProviders';

describe('SHIPS_ROUTES', () => {
  it('provides the Catain store to every ships route, including the ships list', () => {
    expect(SHIPS_ROUTES[0].providers).toContain(CATAIN_STORE_PROVIDERS);
    expect(SHIPS_ROUTES[0].children?.every(child => !child.providers)).toBeTrue();
  });
});
