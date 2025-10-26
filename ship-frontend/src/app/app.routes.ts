import {Routes} from '@angular/router';

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
    path: 'ships/:shipId/shipping/:shippingId', loadComponent: () =>
      import('./features/shipping/components/disembark-summary/disembark-summary.component')
        .then(m => m.DisembarkSummaryComponent)
  }
];

