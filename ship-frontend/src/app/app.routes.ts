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
    path: 'ships/:id/cargo', loadComponent: () =>
      import('./cargos/cargos.component')
        .then(m => m.CargosComponent)
  },
  {
    path: 'ships/:shipId/shipping/:shippingId', loadComponent: () =>
      import('./disembark-summary/disembark-summary.component')
        .then(m => m.DisembarkSummaryComponent)
  },

];

