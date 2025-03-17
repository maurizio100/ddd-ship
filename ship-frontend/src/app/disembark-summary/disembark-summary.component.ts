import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { DisembarkService } from '../services/disembark.service';
import { ShippingSummary } from '../models/shipping-summary';
import {CatainService} from "../services/catain.service";

@Component({
  selector: 'app-disembark-summary',
  templateUrl: './disembark-summary.component.html',
  styleUrls: ['./disembark-summary.component.css'],
})
export class DisembarkSummaryComponent implements OnInit {
  shipping!: ShippingSummary;
  imageBaseUrl = 'http://localhost:8080/web/catains';

  constructor(
    private route: ActivatedRoute,
    private disembarkService: DisembarkService,
    private router: Router,
    private catainService: CatainService
  ) {}

  ngOnInit(): void {
    this.getShipping();
  }

  getShipping(): void {
    const shipId = this.route.snapshot.paramMap.get('shipId')!;
    const shippingId = this.route.snapshot.paramMap.get('shippingId')!;
    this.disembarkService
      .getShipping(shipId, shippingId)
      .subscribe((shipping) => (this.shipping = shipping));
  }

  allShips() : void {
    this.router.navigate(['/ships'])
  }

  getImageUrl(id: string): string {
    return `${this.imageBaseUrl}/${id}/image`;
  }
}
