import { Component, OnInit } from '@angular/core';
import { Cargo } from '../cargo';
import { CargoService } from '../cargo.service';

@Component({
  selector: 'app-cargos',
  templateUrl: './cargos.component.html',
  styleUrls: ['./cargos.component.css']
})
export class CargosComponent implements OnInit {
  
  cargos: Cargo[] = [];

  constructor(
    private cargoService: CargoService,
  ) { }

  ngOnInit(): void {
    this.getCargos()
  }

  getCargos(): void {
    this.cargoService.getCargos().subscribe(cargos => this.cargos = cargos);
  }

}
