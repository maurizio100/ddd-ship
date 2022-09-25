import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { Cargo } from '../cargo';
import { CargoService } from '../cargo.service';
import { Ship } from '../ship';
import { ShipServiceService } from '../ship-service.service';

@Component({
  selector: 'app-cargos',
  templateUrl: './cargos.component.html',
  styleUrls: ['./cargos.component.css']
})
export class CargosComponent implements OnInit {

  @Input() ship!: Ship;
  @Input() showLoaded!: Boolean;

  @Output() shipUpdated = new EventEmitter<Ship>();

  cargos: Cargo[] = [];
  header: String = 'Available Cargo';

  constructor(
    private cargoService: CargoService,
    private shipServiceService: ShipServiceService,
    
  ) { 

  }

  ngOnInit(): void {
    if (this.showLoaded) {
      this.header = 'Loaded Cargo';
      this.cargos = this.ship.cargo;

    } else {
      this.header = 'Available Cargo';
      this.getCargos()
    }
  }

  ngOnChanges(): void {
    
  }

  getCargos(): void {
    this.cargoService.getCargos().subscribe(cargos => this.cargos = cargos);
  }

  performLoading(cargo: Cargo) {
    if (!this.showLoaded) {
      this.shipServiceService.loadCargo(this.ship, cargo).subscribe(
        (ship) => {this.shipUpdated.emit(ship)}
      );
    }
  }
}
