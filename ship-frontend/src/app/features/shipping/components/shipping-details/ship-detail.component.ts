import {Component, inject, OnInit, signal} from "@angular/core";
import {HttpErrorResponse} from "@angular/common/http";
import {Location} from "@angular/common";
import {CargosComponent} from "../cargos/cargos.component";
import {Ship} from "../../models/ship";
import {Subject} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {ShippingService} from "../../services/shipping.service";
import {DisembarkService} from "../../services/disembark.service";
import {HarborService} from "../../services/harbor.service";
import {CdkDragDrop, CdkDropList} from "@angular/cdk/drag-drop";
import {MatIconModule} from "@angular/material/icon";
import {Cargo} from "../../models/cargo";
import {loadRejectionMessage} from "../../services/load-rejection";

const RELEASE_REJECTED = 'The ship could not be Released';


@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.scss'],
  imports: [
    CargosComponent,
    CdkDropList,
    MatIconModule
  ]
})
export class ShipDetailComponent implements OnInit {
  ship!: Ship;
  cargoLoadSubject = new Subject<Ship>();
  knownHarbors = signal<string[]>([]);
  destinationHarbor = signal<string | null>(null);
  releaseRejection = signal<string | null>(null);
  loadRejection = signal<string | null>(null);
  dragOver = signal(false);

  private readonly route = inject(ActivatedRoute);
  private readonly shippingService = inject(ShippingService);
  private readonly disembarkService = inject(DisembarkService);
  private readonly harborService = inject(HarborService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);

  constructor() {}

  ngOnInit(): void {
    this.getShip();
    this.getKnownHarbors();
  }

  getKnownHarbors(): void {
    this.harborService
      .getKnownHarbors()
      .subscribe((harbors) => this.knownHarbors.set(harbors.knownHarbors));
  }

  chooseDestinationHarbor(harbor: string): void {
    this.destinationHarbor.set(harbor || null);
    this.releaseRejection.set(null);
  }

  isTabStop(harbor: string, index: number): boolean {
    const chosen = this.destinationHarbor();
    return chosen ? chosen === harbor : index === 0;
  }

  onHarborKeydown(event: KeyboardEvent, index: number): void {
    const harbors = this.knownHarbors();
    let target = index;
    switch (event.key) {
      case 'ArrowRight':
      case 'ArrowDown':
        target = (index + 1) % harbors.length;
        break;
      case 'ArrowLeft':
      case 'ArrowUp':
        target = (index - 1 + harbors.length) % harbors.length;
        break;
      case ' ':
      case 'Enter':
        break;
      default:
        return;
    }
    event.preventDefault();
    this.chooseDestinationHarbor(harbors[target]);
    const cards = (event.currentTarget as HTMLElement).parentElement?.children;
    (cards?.[target] as HTMLElement | undefined)?.focus();
  }

  getShip(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.shippingService.getShip(id).subscribe((ship) => (this.ship = ship));
  }

  disembark(): void {
    const destinationHarbor = this.destinationHarbor();
    if (this.ship && destinationHarbor) {
      this.disembarkService
        .releaseShip(this.ship, destinationHarbor)
        .subscribe({
          next: (shippingSummary) =>
            this.router.navigate(
              [`/ships/${shippingSummary.shipId}/shipping/${shippingSummary.id}`]
            ),
          error: (error: HttpErrorResponse) =>
            this.releaseRejection.set(error.error?.detail ?? RELEASE_REJECTED),
        });
    }
  }

  cancel(): void {
    this.location.back();
  }

  onShipLoadUpdated(ship: Ship) {
    this.ship.cargo.splice(0, this.ship.cargo.length);
    this.ship.cargo.push(...ship.cargo);
    this.ship.weight = ship.weight;
    this.loadRejection.set(null);
    this.cargoLoadSubject.next(ship);
  }

  /** Load a Cargo onto the ship, by dropping it on the deck or with the crate's Load button. */
  loadCargo(cargo: Cargo): void {
    this.shippingService.loadCargo(this.ship, cargo).subscribe({
      next: (ship) => this.onShipLoadUpdated(ship),
      error: (error: HttpErrorResponse) => {
        // The Stock or the ship may have changed meanwhile, so do not keep offering stale Cargo.
        this.cargoLoadSubject.next(this.ship);
        this.loadRejection.set(loadRejectionMessage(error, cargo, this.ship.maxweight));
      },
    });
  }

  /** Weight on board as a share of the Max Weight, 0-100. */
  weightPercent(): number {
    return this.ship?.maxweight ? Math.min(100, (this.ship.weight / this.ship.maxweight) * 100) : 0;
  }

  isNearlyFull(): boolean {
    return this.weightPercent() > 85;
  }

  /** A crate dropped on the deck is loaded; one released beside the ship fires on its origin list and is ignored. */
  onCargoDropped(event: CdkDragDrop<Cargo[]>): void {
    this.dragOver.set(false);
    if (event.previousContainer !== event.container) {
      this.loadCargo(event.item.data);
    }
  }

  onDragEntered(): void {
    this.dragOver.set(true);
  }

  onDragExited(): void {
    this.dragOver.set(false);
  }
}
