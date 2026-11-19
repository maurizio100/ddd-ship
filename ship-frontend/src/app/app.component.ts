import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {RouterLink, RouterLinkActive, RouterOutlet} from "@angular/router";
import {MatIcon} from "@angular/material/icon";
import {catchError, of} from "rxjs";
import {HarborNameService} from "./core/harbor-name.service";
import {Store} from "@ngrx/store";
import {selectHasImprisonedShips} from "./features/harbor/store/harbor.selectors";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatIcon],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  /** The Harbor Name, or null while loading or when it cannot be retrieved (no chip is shown then). */
  protected readonly harborName = toSignal(
    inject(HarborNameService).getHarborName().pipe(catchError(() => of(null))),
    {initialValue: null}
  );

  /** The Imprisoned Ships pill is offered only while this Harbor holds an Imprisoned Ship. */
  protected readonly hasImprisonedShips = inject(Store).selectSignal(selectHasImprisonedShips);
}
