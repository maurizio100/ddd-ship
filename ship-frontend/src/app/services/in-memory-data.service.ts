import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Ship } from '../models/ship';
import { Cargo } from '../models/cargo';
import { ShippingSummary } from '../models/shipping-summary';

@Injectable({
  providedIn: 'root',
})
export class InMemoryDataService implements InMemoryDbService {
  createDb() {
    const ships: Ship[] = [
      {
        id: '12',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 2, name: 'Chocolate', weight: 1.0 },
          { id: 3, name: 'Cinnamon', weight: 1.0 },
          { id: 7, name: 'Paprika', weight: 1.0 },
          { id: 8, name: 'Planks', weight: 1.0 },
          { id: 9, name: 'Rum', weight: 1.0 },
          { id: 10, name: 'Silk', weight: 1.0 },
        ],
      },

      {
        id: '14',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 7, name: 'Paprika', weight: 1.0 },
          { id: 8, name: 'Planks', weight: 1.0 },
          { id: 9, name: 'Rum', weight: 1.0 },
          { id: 10, name: 'Silk', weight: 1.0 },
        ],
      },
      {
        id: '12',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 1, name: 'Ale', weight: 1.0 },
          { id: 2, name: 'Chocolate', weight: 1.0 },
          { id: 3, name: 'Cinnamon', weight: 1.0 },
        ],
      },

      {
        id: '14',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 7, name: 'Paprika', weight: 1.0 },
          { id: 8, name: 'Planks', weight: 1.0 },
          { id: 9, name: 'Rum', weight: 1.0 },
          { id: 10, name: 'Silk', weight: 1.0 },
        ],
      },
      {
        id: '12',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 1, name: 'Ale', weight: 1.0 },
          { id: 2, name: 'Chocolate', weight: 1.0 },
          { id: 3, name: 'Cinnamon', weight: 1.0 },
        ],
      },

      {
        id: '14',
        name: 'Dr. Nice',
        weight: 3.0,
        maxweight: 20,
        cargo: [
          { id: 7, name: 'Paprika', weight: 1.0 },
          { id: 8, name: 'Planks', weight: 1.0 },
          { id: 9, name: 'Rum', weight: 1.0 },
          { id: 10, name: 'Silk', weight: 1.0 },
        ],
      },
    ];

    const cargos: Cargo[] = [
      { id: 1, name: 'Ale', weight: 5.0 },
      { id: 2, name: 'Chocolate', weight: 1.0 },
      { id: 3, name: 'Cinnamon', weight: 1.0 },
      { id: 4, name: 'Coffee', weight: 1.0 },
      { id: 5, name: 'Fruits', weight: 1.0 },
      { id: 6, name: 'Leather', weight: 1.0 },
      { id: 7, name: 'Paprika', weight: 1.0 },
      { id: 8, name: 'Planks', weight: 1.0 },
      { id: 9, name: 'Rum', weight: 1.0 },
      { id: 10, name: 'Silk', weight: 1.0 },
      { id: 11, name: 'Sugar', weight: 1.0 },
      { id: 12, name: 'Tobacco', weight: 1.0 },
      { id: 13, name: 'Wheat', weight: 1.0 },
      { id: 14, name: 'Wine', weight: 1.0 },
    ];

    const shippings: ShippingSummary[] = [
      {
        id: 12,
        name: 'Dr. Nice',
        cargo: [
          { id: 1, name: 'Ale', weight: 1.0 },
          { id: 2, name: 'Chocolate', weight: 1.0 },
          { id: 3, name: 'Cinnamon', weight: 1.0 },
        ],
        sailorsCode: 'Sailing is fun for everyone!',
      },
    ];

    return { ships, cargos, shippings };
  }

  constructor() {}
}
