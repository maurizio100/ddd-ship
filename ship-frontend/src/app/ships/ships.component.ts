import { Component, OnInit } from '@angular/core';
import { Ship, ShippingState } from '../models/ship';
import { ShipService } from '../services/ship-service.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-ships',
    templateUrl: './ships.component.html',
    styleUrls: ['./ships.component.css'],
    standalone: false
})
export class ShipsComponent implements OnInit {
  // shippingState = ShippingState;
  ships: Ship[] = [];

  constructor(private shipService: ShipService, private router: Router) {}

  ngOnInit(): void {
    this.getShips();
  }

  getShips(): void {
    this.shipService.getShips().subscribe((ships) => (this.ships = ships));
  }

  createShipping(ship: Ship) {
    this.shipService
      .createShipping(ship)
      .subscribe((shippingSummary) =>
        this.router.navigate([`ships/${shippingSummary.shipId}/cargo`])
      );
  }

  editShipping(ship: Ship) {
    this.router.navigate([`ships/${ship.id}/cargo`]);
  }

  getImageUrl(ship: Ship) {
    return '/assets/img/ship.jpg';
  }

  canCreateNewShipping(ship: Ship): boolean {
    return (
      ship.shippingState === ShippingState.IDLE ||
      ship.shippingState === ShippingState.DONE
    );
  }

  shippingExists(ship: Ship): boolean {
    return ship.shippingState === ShippingState.PREPARING;
  }

  isShipAway(ship: Ship): boolean {
    return ship.shippingState === ShippingState.SHIPPING;
  }

  protected readonly ShippingState = ShippingState;
}
