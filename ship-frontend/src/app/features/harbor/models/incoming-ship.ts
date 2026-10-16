import { Cargo } from '../../shipping/models/cargo';

/** A ship that arrived with Cargo aboard and waits to be unloaded or refused. */
export interface IncomingShip {
  shipId: string;
  name: string;
  arrivedFrom: string | null;
  cargo: Cargo[];
  /** Two-decimal string such as "115.00"; null while a Cargo aboard has no Price at this Harbor. */
  deliveryPrice: string | null;
}
