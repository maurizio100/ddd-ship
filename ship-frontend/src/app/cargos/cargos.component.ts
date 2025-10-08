import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChange,
  SimpleChanges,
} from '@angular/core';
import { Observable } from 'rxjs';
import { Cargo } from '../models/cargo';
import { CargoService } from '../services/cargo.service';
import { Ship } from '../models/ship';
import { ShipService } from '../services/ship-service.service';
import { ShippingSummary } from '../models/shipping-summary';

@Component({
    selector: 'app-cargos',
    templateUrl: './cargos.component.html',
    styleUrls: ['./cargos.component.css'],
    standalone: false
})
export class CargosComponent implements OnInit {
  @Input() ship!: Ship;
  @Input() shipping!: ShippingSummary;
  @Input() showLoaded!: Boolean;
  @Input() showLoadObserve!: Observable<Ship>;

  @Output() shipUpdated = new EventEmitter<Ship>();

  allCargo: Cargo[] = [];
  cargos: Cargo[] = [];
  header: String = 'Available Cargo';

  constructor(
    private cargoService: CargoService,
    private shipServiceService: ShipService
  ) {}

  ngOnInit(): void {
    if (this.showLoaded) {
      this.header = 'Loaded Cargo';
      this.cargos =
        this.ship != undefined ? this.ship.cargo : this.shipping.cargo;
    } else {
      this.header = 'Available Cargo';
      this.getCargos();
      this.showLoadObserve.subscribe((s) => this.prepareAvailableCargo());
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
      this.shipServiceService.loadCargo(this.ship, cargo).subscribe((ship) => {
        this.shipUpdated.emit(ship);
      });
    } else {
      this.shipServiceService
        .unloadCargo(this.ship, cargo)
        .subscribe((ship) => {
          this.shipUpdated.emit(ship);
        });
    }
  }
}
