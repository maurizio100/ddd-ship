import { bootstrapApplication } from '@angular/platform-browser';
import { provideStore } from '@ngrx/store';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { isDevMode } from '@angular/core';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import {provideEffects} from "@ngrx/effects";
import { provideMaterialSymbols } from './app/app.config';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideStore(),
    provideEffects(),
    provideMaterialSymbols(),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() })
  ],
});
