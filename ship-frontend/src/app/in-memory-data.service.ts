import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Ship } from './ship'

@Injectable({
  providedIn: 'root'
})
export class InMemoryDataService implements InMemoryDataService {

  createDb() {
    const ships = [
      { id: 12, name: 'Black Pearl' },
      { id: 13, name: 'Interceptor' },
      { id: 14, name: 'Flying Dutchman' },
      { id: 15, name: 'Magneta' },
    ];

    return ships;
  }
  
  constructor() { }
}
