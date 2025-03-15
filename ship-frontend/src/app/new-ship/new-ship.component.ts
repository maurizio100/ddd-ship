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
  imageBaseUrl = 'http://localhost:8080/web/catains';
  public name: string = '';
  selectedCatain: string = '';

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
    return `${this.imageBaseUrl}/${id}/image`;
  }

  createShip() {
    const newShip: NewShipRequest = {
      name: this.name,
      catainId: this.selectedCatain,
    };
    this.shipService.addShip(newShip).subscribe((ship) => {
      this.router.navigate(['/ships']);
    });
  }

  selectCatain(id: string) {
    this.selectedCatain = id;
  }

  cancel() {
    this.location.back();
  }
}
