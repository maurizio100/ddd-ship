import {Routes} from '@angular/router';

import {HARBOR_STORE_PROVIDERS} from './features/harbor/store/HarborStoreProviders';
import {SHIPS_STORE_PROVIDERS} from './features/ships/store/ShipsStoreProviders';
import {CATAIN_STORE_PROVIDERS} from './features/catains/store/CatainStoreProviders';

export const routes: Routes = [
  {path: '', redirectTo: '/ships', pathMatch: 'full'},
  {
    path: 'ships',
    loadChildren: () =>
      import('./features/ships/ShipRouting')
        .then(m => m.SHIPS_ROUTES)
  },
  {
    path: 'ships/:id/cargo', loadChildren: () =>
      import('./features/shipping/ShippingRouting')
        .then(m => m.SHIPPING_ROUTES)
  }, {
    path: 'harbor',
    // The Incoming Ships follow the fleet-events stream, which the ships store owns.
    providers: [HARBOR_STORE_PROVIDERS, SHIPS_STORE_PROVIDERS],
    loadComponent: () =>
      import('./features/harbor/components/harbor/harbor.component')
        .then(m => m.HarborComponent)
  }, {
    path: 'imprisoned',
    // Paying a Fine reloads the fleet, and each card shows its Catain Image.
    providers: [HARBOR_STORE_PROVIDERS, SHIPS_STORE_PROVIDERS, CATAIN_STORE_PROVIDERS],
    loadComponent: () =>
      import('./features/harbor/components/imprisoned-ships/imprisoned-ships.component')
        .then(m => m.ImprisonedShipsComponent)
  }, {
    path: 'ships/:shipId/shipping/:shippingId', loadComponent: () =>
      import('./features/shipping/components/disembark-summary/disembark-summary.component')
        .then(m => m.DisembarkSummaryComponent)
  }
];

