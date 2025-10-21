import { Routes } from '@angular/router';

import { ShipsComponent } from './ships/ships.component';
import { ShipDetailComponent } from './ship-detail/ship-detail.component';
import { DisembarkSummaryComponent } from './disembark-summary/disembark-summary.component';
import { NewShipComponent } from './new-ship/new-ship.component';

export const routes: Routes = [
  { path: '', redirectTo: '/ships', pathMatch: 'full' },
  { path: 'ships', component: ShipsComponent },
  { path: 'ships/:id/cargo', component: ShipDetailComponent },
  { path: 'ships/:shipId/shipping/:shippingId', component: DisembarkSummaryComponent },
  { path: 'new-ship', component: NewShipComponent },
];

