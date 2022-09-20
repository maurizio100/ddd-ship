import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Ship } from '../ship';
import { ShipServiceService } from '../ship-service.service';

@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.css']
})
export class ShipDetailComponent implements OnInit {
  ship: Ship | undefined;

  constructor(
    private route: ActivatedRoute,
    private shipService: ShipServiceService,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.getShip()
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

}
