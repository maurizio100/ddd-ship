import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { Router } from '@angular/router';

import { Ship } from '../ship';
import { ShipServiceService } from '../ship-service.service';
import { DisembarkService } from '../disembark.service';
import { Subject } from 'rxjs';
import { ShippingSummary } from '../shipping-summary';

@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.css'],
})
export class ShipDetailComponent implements OnInit {
  ship!: Ship;
  cargoLoadSubject = new Subject<Ship>();

  constructor(
    private route: ActivatedRoute,
    private shipService: ShipServiceService,
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

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if (this.ship) {
      this.shipService.updateShip(this.ship).subscribe();
    }
  }

  disembark(): void {
    if (this.ship) {
      this.disembarkService
        .createShipping(this.ship)
        .subscribe((shippingsummary) =>
          this.router.navigate(['shipping/' + shippingsummary.id])
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
