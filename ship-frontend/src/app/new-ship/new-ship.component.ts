import { Component, OnInit } from '@angular/core';
import { Catain } from '../models/catain';
import { CatainService } from '../services/catain.service';
import { ShipService } from '../services/ship-service.service';
import { NewShipRequest } from '../models/new-ship-request';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-new-ship',
  templateUrl: './new-ship.component.html',
  styleUrls: ['./new-ship.component.css'],
})
export class NewShipComponent implements OnInit {
  catains: Catain[] = [];

  shipRequest: NewShipRequest = {
    name: '',
    catainId: '',
  };

  constructor(
    private catainService: CatainService,
    private shipService: ShipService,
    private router: Router,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.getCatains();
  }

  getCatains(): void {
    this.catainService
      .getCatains()
      .subscribe((catains) => (this.catains = catains));
  }

  getImageUrl(id: string): string {
    return this.catainService.getCatainImageUrl(id);
  }

  createShip() {
    this.shipService.addShip(this.shipRequest).subscribe((ship) => {
      this.router.navigate(['/ships']);
    });
  }

  selectCatain(id: string) {
    this.shipRequest.catainId = id;
  }

  cancel() {
    this.location.back();
  }
}
