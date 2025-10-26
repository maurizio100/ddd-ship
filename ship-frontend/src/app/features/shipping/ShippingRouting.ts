import {Routes} from "@angular/router";

export const SHIPPING_ROUTES: Routes = [
  {
    path: '',
    providers: [],
    children: [
      {
        path: '', loadComponent: () =>
          import('./components/shipping-details/ship-detail.component')
            .then(m => m.ShipDetailComponent)
      },
    ]
  }
];


