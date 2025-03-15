import { Component, OnInit } from '@angular/core';
import { Ship } from '../models/ship';
import { ShipService } from '../services/ship-service.service';

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css'],
})
export class ShipsComponent implements OnInit {
  ships: Ship[] = [];

  constructor(private shipService: ShipService) {}

  ngOnInit(): void {
    this.getShips();
  }

  getShips(): void {
    this.shipService.getShips().subscribe((ships) => (this.ships = ships));
  }

  delete(ship: Ship): void {
    this.ships = this.ships.filter((s) => s != ship);
    this.shipService.deleteShip(ship.id).subscribe();
  }
}
