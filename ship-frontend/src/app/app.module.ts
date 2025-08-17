import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ShipsComponent } from './ships/ships.component';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';

import { HttpClientInMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDataService } from './services/in-memory-data.service';
import { ShipDetailComponent } from './ship-detail/ship-detail.component';
import { CargosComponent } from './cargos/cargos.component';
import { DisembarkSummaryComponent } from './disembark-summary/disembark-summary.component';
import { NewShipComponent } from './new-ship/new-ship.component';

@NgModule({
  declarations: [
    AppComponent,
    ShipsComponent,
    ShipDetailComponent,
    CargosComponent,
    DisembarkSummaryComponent,
    NewShipComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    HttpClientInMemoryWebApiModule.forRoot(InMemoryDataService, {
      dataEncapsulation: false,
    }),
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
