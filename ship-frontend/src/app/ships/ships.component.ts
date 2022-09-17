import { Component, OnInit } from '@angular/core';
import { Ship } from '../ship';
import { ShipServiceService } from '../ship-service.service';

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css']
})
export class ShipsComponent implements OnInit {

  ships: Ship[] = [];

  constructor(
    private shipService: ShipServiceService
  ) { }

  ngOnInit(): void {
  }

  add(name: string): void {
    name = name.trim();
    if (!name) { return; }
    this.shipService.addShip({ name } as Ship)
      .subscribe(ship => {this.ships.push(ship);});
  }

}
