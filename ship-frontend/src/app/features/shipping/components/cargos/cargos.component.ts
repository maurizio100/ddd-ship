import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  signal
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvailableCargo, Cargo } from '../../models/cargo';
import { CargoService } from '../../services/cargo.service';
import { Ship } from '../../models/ship';
import { ShippingService } from '../../services/shipping.service';
import { ShippingSummary } from '../../models/shipping-summary';
import {LowerCasePipe, NgStyle} from "@angular/common";

const LOAD_REJECTED = 'The Cargo could not be loaded';

@Component({
  selector: 'app-cargos',
  templateUrl: './cargos.component.html',
  styleUrls: ['./cargos.component.css'],
  imports: [
    LowerCasePipe,
    NgStyle
  ]
})
export class CargosComponent implements OnInit {
  @Input() ship!: Ship;
  @Input() shipping!: ShippingSummary;
  @Input() showLoaded!: Boolean;
  @Input() showLoadObserve!: Observable<Ship>;

  @Output() shipUpdated = new EventEmitter<Ship>();

  allCargo: AvailableCargo[] = [];
  cargos: (Cargo & Partial<AvailableCargo>)[] = [];
  header: String = 'Available Cargo';
  loadRejection = signal<string | null>(null);

  constructor(
    private cargoService: CargoService,
    private shippingService: ShippingService
  ) {}

  ngOnInit(): void {
    if (this.showLoaded) {
      this.header = 'Loaded Cargo';
      this.cargos =
        this.ship != undefined ? this.ship.cargo : this.shipping.cargo;
    } else {
      this.header = 'Available Cargo';
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

  prepareAvailableCargo() {
    if (!this.showLoaded) {
      this.cargos.splice(0, this.cargos.length);

      const shipCargo = new Map(this.ship.cargo.map((c) => [c.id, c]));
      const availableCargo = this.allCargo.filter(
        (c) => shipCargo.get(c.id) == undefined
      );
      this.cargos.push(...availableCargo);
    }
  }

  performLoading(cargo: Cargo) {
    if (!this.showLoaded) {
      this.shippingService.loadCargo(this.ship, cargo).subscribe({
        next: (ship) => {
          this.loadRejection.set(null);
          this.shipUpdated.emit(ship);
        },
        error: (error: HttpErrorResponse) =>
          this.loadRejection.set(error.error?.detail ?? LOAD_REJECTED),
      });
    } else {
      this.shippingService
        .unloadCargo(this.ship, cargo)
        .subscribe((ship) => {
          this.shipUpdated.emit(ship);
        });
    }
  }
}
