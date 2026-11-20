import { ShipClass } from './ship';

export interface NewShipRequest {
  name: string;
  catainId: string;
  shipClass: ShipClass;
}
