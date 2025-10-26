import {Routes} from '@angular/router';
import {SHIPS_STORE_PROVIDERS} from "./store/ShipsStoreProviders";
import {CATAIN_STORE_PROVIDERS} from "../catains/store/CatainStoreProviders";

export const SHIPS_ROUTES: Routes = [
  {
    path: '',
    providers: [SHIPS_STORE_PROVIDERS],
    children: [
      {
        path: '',  // matches /ships
        loadComponent: () =>
          import('./components/ships-list/ships.component')
            .then(m => m.ShipsComponent)
      },
      {
        path: 'new',  // matches /ships/new
        providers: [CATAIN_STORE_PROVIDERS],
        loadComponent: () =>
          import('./components/new-ship/new-ship.component')
            .then(m => m.NewShipComponent)
      }
    ]
  }
];
