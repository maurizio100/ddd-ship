import { Component, OnInit } from '@angular/core';
import { Ship } from '../ship';

@Component({
  selector: 'app-ships',
  templateUrl: './ships.component.html',
  styleUrls: ['./ships.component.css']
})
export class ShipsComponent implements OnInit {

  ship: Ship = {
    id: 1,
    name: 'Flying Dutchman'
  }
  
  constructor() { }

  ngOnInit(): void {
  }

}
