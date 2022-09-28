import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ShipsComponent } from './ships/ships.component';
import { ShipDetailComponent } from './ship-detail/ship-detail.component';
import { DisembarkSummaryComponent } from './disembark-summary/disembark-summary.component';

const routes: Routes = [
  { path: '', redirectTo: '/ships', pathMatch: 'full' },
  { path: 'ships', component: ShipsComponent },
  { path: 'detail/:id', component: ShipDetailComponent },
  { path: 'shipping/:id', component: DisembarkSummaryComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
