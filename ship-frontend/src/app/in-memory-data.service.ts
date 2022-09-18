import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Ship } from './ship'

@Injectable({
  providedIn: 'root'
})
export class InMemoryDataService implements InMemoryDbService {

  createDb() {
    const ships: Ship[] = [
      { id: 12, name: 'Dr. Nice' },
      { id: 14, name: 'Dr. Nice' },
     ];
    
    return {ships};
  }
  
  constructor() { }
}
