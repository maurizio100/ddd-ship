import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Ship } from './ship';
import { Cargo } from './cargo';

@Injectable({
  providedIn: 'root'
})
export class InMemoryDataService implements InMemoryDbService {

  createDb() {
    const ships: Ship[] = [
      { id: 12, name: 'Dr. Nice' },
      { id: 14, name: 'Dr. Nice' },
     ];
    
    const cargos: Cargo[] = [
      { id: 1, name:'Ale', weight: 1.0},
      { id: 2, name: 'Chocolate', weight: 1.0},
      { id: 3, name: 'Cinnamon', weight: 1.0},
      { id: 4, name: 'Coffee', weight: 1.0},
      { id: 5, name:'Fruits', weight: 1.0},
      { id: 6, name: 'Leather', weight: 1.0},
      { id: 7, name: 'Paprika', weight: 1.0},
      { id: 8, name: 'Planks', weight: 1.0},
      { id: 9, name:'Rum', weight: 1.0},
      { id: 10, name: 'Silk', weight: 1.0},
      { id: 11, name: 'Sugar', weight: 1.0},
      { id: 12, name: 'Tobacco', weight: 1.0},
      { id: 13, name:'Wheat', weight: 1.0},
      { id: 14, name: 'Wine', weight: 1.0}
    ];

    return {ships, cargos};
  }
  
  constructor() { }
}
