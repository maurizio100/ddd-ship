import {EnvironmentProviders, inject, provideAppInitializer} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';

/** Makes every <mat-icon> use the Material Symbols Outlined font loaded in index.html. */
export function provideMaterialSymbols(): EnvironmentProviders {
  return provideAppInitializer(() => {
    inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
  });
}
