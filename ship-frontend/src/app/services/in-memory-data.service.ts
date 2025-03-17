import { Injectable } from '@angular/core';
import { InMemoryDbService, RequestInfo } from 'angular-in-memory-web-api';
import { Ship } from '../models/ship';
import { Cargo } from '../models/cargo';
import { ShippingSummary } from '../models/shipping-summary';
import { Catain } from '../models/catain';

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
        shippingState: null,
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
        shippingState: null,
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
        shippingState: null,
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
        shippingState: null,
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
        shippingState: null,
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
        shippingState: null,
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
        id: '12',
        shipId: '13',
        name: 'Dr. Nice',
        catainId: '1',
        catainName: 'the catain',
        cargo: [
          { id: 1, name: 'Ale', weight: 1.0 },
          { id: 2, name: 'Chocolate', weight: 1.0 },
          { id: 3, name: 'Cinnamon', weight: 1.0 },
        ],
        sailorsCode: 'Sailing is fun for everyone!',
        weight: 0.0
      },
    ];

    const catains: Catain[] = [
      {
        id: 'e5e63ab1-f0d2-4ee4-9924-478b359c73e1',
        name: 'Bootstrap Bill',
      },
      {
        id: '02f0d814-8e33-4570-afaf-526949a90784',
        name: 'Catain Black Whiskers',
      },
      {
        id: 'cac60e67-f65e-42a2-96cb-1783fdd56873',
        name: 'Catain Cat sparrow',
      },
      {
        id: '6bc80803-f8a5-4e64-9499-eb2758d91bf1',
        name: 'Furry Jones',
      },
    ];

    return { ships, cargos, shippings, catains };
  }

  get(reqInfo: RequestInfo) {
    const { collectionName, id } = reqInfo;

    if (collectionName === 'catains' && id && reqInfo.url.includes('/image')) {
      return reqInfo.utils.createResponse$(() => ({
        body: this.getMockImage(id),
        status: 200,
      }));
    }

    return undefined; // Default behavior for other requests
  }

  private getMockImage(catainId: string): string {
    const imageMap: { [key: string]: string } = {
      'e5e63ab1-f0d2-4ee4-9924-478b359c73e1':
        '../../assets/catains/bootstrap_bill.jpg',
      '02f0d814-8e33-4570-afaf-526949a90784':
        '../../assets/catains/catain_black_whiskers.png',
      'cac60e67-f65e-42a2-96cb-1783fdd56873':
        '../../assets/catains/catain_cat_sparrow.png',
      '6bc80803-f8a5-4e64-9499-eb2758d91bf1':
        '../../assets/catains/furry_jones.jpeg',
    };

    return imageMap[catainId] || '../../assets/catains/default.jpg';
  }

  constructor() {}
}
