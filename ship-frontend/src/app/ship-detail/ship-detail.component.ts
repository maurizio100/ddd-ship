import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Ship } from '../ship';
import { ShipServiceService } from '../ship-service.service';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.css']
})
export class ShipDetailComponent implements OnInit {
  ship!: Ship;
  cargoLoadSubject = new Subject<Ship>();

  constructor(
    private route: ActivatedRoute,
    private shipService: ShipServiceService,
    private location: Location
  ) { 
    this.getShip();
  }

  ngOnInit(): void {
   
  }

  getShip(): void {
    const id = parseInt(this.route.snapshot.paramMap.get('id')!);
    this.shipService.getShip(id).subscribe(ship => this.ship = ship)
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if (this.ship) {
      this.shipService.updateShip(this.ship).subscribe()
    }
  }

  onShipLoadUpdated(ship: Ship) {
    this.ship.cargo.splice(0, this.ship.cargo.length);
    this.ship.cargo.push(...ship.cargo);
    this.cargoLoadSubject.next(ship);
  }
}
