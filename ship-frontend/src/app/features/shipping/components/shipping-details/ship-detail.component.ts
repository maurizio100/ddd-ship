import {Component, inject, OnInit} from "@angular/core";
import {Location, UpperCasePipe} from "@angular/common";
import {CargosComponent} from "../cargos/cargos.component";
import {Ship} from "../../models/ship";
import {Subject} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {ShippingService} from "../../services/shipping.service";
import {DisembarkService} from "../../services/disembark.service";


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

  private readonly route = inject(ActivatedRoute);
  private readonly shippingService = inject(ShippingService);
  private readonly disembarkService = inject(DisembarkService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);

  constructor() {}

  ngOnInit(): void {
    this.getShip();
  }

  getShip(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.shippingService.getShip(id).subscribe((ship) => (this.ship = ship));
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
