import { bootstrapApplication } from '@angular/platform-browser';
import { provideStore, provideState } from '@ngrx/store';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { isDevMode } from '@angular/core';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { shipReducers } from './app/ngrx/ship.reducers';
import {ShipsEffects} from "./app/ngrx/ships.effects";
import {provideEffects} from "@ngrx/effects";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideStore(),
    provideState({ name: 'ships', reducer: shipReducers }),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
    provideEffects(ShipsEffects),
  ],
});
