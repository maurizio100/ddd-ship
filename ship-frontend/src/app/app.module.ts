import {NgModule, isDevMode} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {ShipsComponent} from './ships/ships.component';
import {FormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {AppRoutingModule} from './app-routing.module';

import {ShipDetailComponent} from './ship-detail/ship-detail.component';
import {CargosComponent} from './cargos/cargos.component';
import {DisembarkSummaryComponent} from './disembark-summary/disembark-summary.component';
import {NewShipComponent} from './new-ship/new-ship.component';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {shipReducers} from "./ngrx/ship.reducers";

@NgModule({
  declarations: [
    AppComponent,
    ShipsComponent,
    ShipDetailComponent,
    CargosComponent,
    DisembarkSummaryComponent,
    NewShipComponent,
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    FormsModule,
    AppRoutingModule,
    StoreModule.forRoot({
      ships: shipReducers
    }, {}),
    EffectsModule.forRoot([]),
    StoreDevtoolsModule.instrument({maxAge: 25, logOnly: !isDevMode()})],
  providers: [provideHttpClient(withInterceptorsFromDi())]
})
export class AppModule {
}
