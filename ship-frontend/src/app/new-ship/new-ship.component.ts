import { Component, OnInit } from '@angular/core';
import { Catain } from '../models/catain';
import { CatainService } from '../services/catain.service';
import { ShipService } from '../services/ship-service.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-new-ship',
  templateUrl: './new-ship.component.html',
  styleUrls: ['./new-ship.component.css'],
})
export class NewShipComponent implements OnInit {
  catains: Catain[] = [];
  imageBaseUrl = 'http://localhost:8080/web/catains';

  constructor(
    private catainService: CatainService,
    private shipService: ShipService
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
}
