import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DisembarkService } from '../disembark.service';
import { ShippingSummary } from '../shipping-summary';

@Component({
  selector: 'app-disembark-summary',
  templateUrl: './disembark-summary.component.html',
  styleUrls: ['./disembark-summary.component.css']
})
export class DisembarkSummaryComponent implements OnInit {
  shipping!: ShippingSummary;

  constructor(
    private route: ActivatedRoute,
    private disembarkService: DisembarkService,
  ) { }

  ngOnInit(): void {
    this.getShipping();
  }

  getShipping(): void {
    const id = parseInt(this.route.snapshot.paramMap.get('id')!);
    this.disembarkService.getShipping(id).subscribe(shipping => this.shipping = shipping)
  }

}
