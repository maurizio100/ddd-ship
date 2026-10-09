import { Component, inject, OnInit } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { Store } from '@ngrx/store';
import * as HarborActions from '../../store/harbor.actions';
import { selectSavings, selectStock } from '../../store/harbor.selectors';

@Component({
  selector: 'app-harbor',
  templateUrl: './harbor.component.html',
  styleUrl: './harbor.component.scss',
  imports: [LowerCasePipe],
})
export class HarborComponent implements OnInit {
  private readonly store = inject(Store);

  stock = this.store.selectSignal(selectStock);
  savings = this.store.selectSignal(selectSavings);

  ngOnInit(): void {
    this.store.dispatch(HarborActions.loadStock());
    this.store.dispatch(HarborActions.loadSavings());
  }
}
