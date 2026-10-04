import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CdkDrag, CdkDropList } from '@angular/cdk/drag-drop';
import { LowerCasePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Observable } from 'rxjs';
import { AvailableCargo, Cargo } from '../../models/cargo';
import { CargoService } from '../../services/cargo.service';
import { Ship } from '../../models/ship';
import { ShippingService } from '../../services/shipping.service';
import { ShippingSummary } from '../../models/shipping-summary';

@Component({
  selector: 'app-cargos',
  templateUrl: './cargos.component.html',
  styleUrls: ['./cargos.component.scss'],
  imports: [LowerCasePipe, CdkDropList, CdkDrag, MatIconModule],
})
export class CargosComponent implements OnInit {
  @Input() ship!: Ship;
  @Input() shipping!: ShippingSummary;
  @Input() showLoaded!: Boolean;
  @Input() showLoadObserve!: Observable<Ship>;

  @Output() shipUpdated = new EventEmitter<Ship>();
  /** The User asked to load this Cargo without dragging it. */
  @Output() loadRequested = new EventEmitter<Cargo>();

  allCargo: AvailableCargo[] = [];
  cargos: (Cargo & Partial<AvailableCargo>)[] = [];

  constructor(
    private cargoService: CargoService,
    private shippingService: ShippingService
  ) {}

  ngOnInit(): void {
    if (this.showLoaded) {
      this.cargos = this.ship != undefined ? this.ship.cargo : this.shipping.cargo;
    } else {
      this.getCargos();
      // Every load or unload changes the Harbor's Stock, so fetch the Available Cargo again.
      this.showLoadObserve.subscribe(() => this.getCargos());
    }
  }

  getCargos(): void {
    this.cargoService.getCargos().subscribe((cargos) => {
      this.allCargo = cargos;
      this.prepareAvailableCargo();
    });
  }

  /** The Available Cargo is every Cargo the Harbor has in Stock, whether or not it is loaded. */
  prepareAvailableCargo() {
    if (!this.showLoaded) {
      this.cargos = [...this.allCargo];
    }
  }

  unloadCargo(cargo: Cargo) {
    this.shippingService.unloadCargo(this.ship, cargo).subscribe((ship) => this.shipUpdated.emit(ship));
  }
}
