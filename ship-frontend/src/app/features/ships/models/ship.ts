export interface Ship {
  id: string;
  name: string;
  catain: string;
  shippingState: ShippingState | null;
}

export enum ShippingState {
  IDLE = 'IDLE',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DONE = 'DONE',
}
