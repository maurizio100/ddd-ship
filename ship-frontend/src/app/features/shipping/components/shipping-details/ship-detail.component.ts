import {Component, inject, OnInit, signal} from "@angular/core";
import {HttpErrorResponse} from "@angular/common/http";
import {Location, NgOptimizedImage, UpperCasePipe} from "@angular/common";
import {CargosComponent} from "../cargos/cargos.component";
import {Ship} from "../../models/ship";
import {Subject} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {ShippingService} from "../../services/shipping.service";
import {DisembarkService} from "../../services/disembark.service";
import {HarborService} from "../../services/harbor.service";
import {CdkDragDrop} from "@angular/cdk/drag-drop";
import {Cargo} from "../../models/cargo";

const RELEASE_REJECTED = 'The ship could not be Released';


@Component({
  selector: 'app-ship-detail',
  templateUrl: './ship-detail.component.html',
  styleUrls: ['./ship-detail.component.css'],
  imports: [
    UpperCasePipe,
    CargosComponent,
    NgOptimizedImage
  ]
})
export class ShipDetailComponent implements OnInit {
  ship!: Ship;
  cargoLoadSubject = new Subject<Ship>();
  knownHarbors = signal<string[]>([]);
  destinationHarbor = signal<string | null>(null);
  releaseRejection = signal<string | null>(null);

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
    this.cargoLoadSubject.next(ship);
  }

  /** Signature only (STORY-016): loads the dropped Cargo when it comes from another list. */
  onCargoDropped(event: CdkDragDrop<Cargo[]>): void {}
}
