import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {Location, UpperCasePipe} from '@angular/common';
import { Router } from '@angular/router';

import { Ship } from '../models/ship';
import { ShipService } from '../services/ship.service';
import { DisembarkService } from '../services/disembark.service';
import { Subject } from 'rxjs';
import {CargosComponent} from "../cargos/cargos.component";

@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.css'],
  imports: [
    UpperCasePipe,
    CargosComponent
  ]
})
export class ShipDetailComponent implements OnInit {
  ship!: Ship;
  cargoLoadSubject = new Subject<Ship>();

  constructor(
    private route: ActivatedRoute,
    private shipService: ShipService,
    private disembarkService: DisembarkService,
    private location: Location,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getShip();
  }

  getShip(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.shipService.getShip(id).subscribe((ship) => (this.ship = ship));
  }

  disembark(): void {
    if (this.ship) {
      this.disembarkService
        .releaseShip(this.ship)
        .subscribe((shippingSummary) =>
          this.router.navigate(
            [`/ships/${shippingSummary.shipId}/shipping/${shippingSummary.id}`]
          )
        );
    }
  }

  cancel(): void {
    this.location.back();
  }

  onShipLoadUpdated(ship: Ship) {
    this.ship.cargo.splice(0, this.ship.cargo.length);
    this.ship.cargo.push(...ship.cargo);
    this.ship.weight = ship.weight;
    this.cargoLoadSubject.next(ship);
  }
}
