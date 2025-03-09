import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ShipsComponent } from './ships/ships.component';
import { ShipDetailComponent } from './ship-detail/ship-detail.component';
import { DisembarkSummaryComponent } from './disembark-summary/disembark-summary.component';
import { NewShipComponent } from './new-ship/new-ship.component';

const routes: Routes = [
  { path: '', redirectTo: '/ships', pathMatch: 'full' },
  { path: 'ships', component: ShipsComponent },
  { path: 'detail/:id', component: ShipDetailComponent },
  { path: 'shipping/:id', component: DisembarkSummaryComponent },
  { path: 'new-ship', component: NewShipComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
