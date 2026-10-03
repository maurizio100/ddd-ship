import { fakeAsync, TestBed, tick } from '@angular/core/testing';

import { EVENT_SOURCE_FACTORY, FleetEventsService } from './fleet-events.service';
import { FleetEvent } from '../models/fleet-event';
import { environment } from '../../../../environments/environment';

/** Stands in for the browser's EventSource: records listeners, and lets the spec push named events. */
class FakeEventSource {
  readonly listeners = new Map<string, (event: MessageEvent) => void>();
  closed = false;
  readyState = 1;

  constructor(readonly url: string) {}

  addEventListener(type: string, listener: (event: MessageEvent) => void): void {
    this.listeners.set(type, listener);
  }

  close(): void {
    this.closed = true;
  }

  emit(type: string): void {
    this.listeners.get(type)!(new Event(type) as MessageEvent);
  }

  push(type: string, data: unknown): void {
    this.listeners.get(type)!(new MessageEvent(type, { data: JSON.stringify(data) }));
  }
}

describe('FleetEventsService', () => {
  let service: FleetEventsService;
  let opened: FakeEventSource[];

  beforeEach(() => {
    opened = [];
    TestBed.configureTestingModule({
      providers: [
        {
          provide: EVENT_SOURCE_FACTORY,
          useValue: (url: string) => {
            const source = new FakeEventSource(url);
            opened.push(source);
            return source as unknown as EventSource;
          },
        },
      ],
    });
    service = TestBed.inject(FleetEventsService);
  });

  it('opens one EventSource on /fleet-events per subscription', () => {
    const subscription = service.events().subscribe();

    expect(opened.map((source) => source.url)).toEqual([`${environment.baseUrl}/fleet-events`]);
    subscription.unsubscribe();
  });

  it('maps ship-arrived and ship-left to fleet events', () => {
    const received: FleetEvent[] = [];
    const subscription = service.events().subscribe((event) => received.push(event));

    opened[0].push('ship-arrived', { shipId: 'id-1', shipName: 'Black Pearl', originHarbor: 'Tortuga' });
    opened[0].push('ship-left', { shipId: 'id-2', shipName: 'Interceptor', destinationHarbor: 'Port Royal' });

    expect(received).toEqual([
      { type: 'ship-arrived', ship: { shipId: 'id-1', shipName: 'Black Pearl', originHarbor: 'Tortuga' } },
      { type: 'ship-left', ship: { shipId: 'id-2', shipName: 'Interceptor', destinationHarbor: 'Port Royal' } },
    ]);
    subscription.unsubscribe();
  });

  it('closes the EventSource on unsubscribe', () => {
    const subscription = service.events().subscribe();

    subscription.unsubscribe();

    expect(opened[0].closed).toBeTrue();
  });

  it('tells when the stream opens', () => {
    const received: FleetEvent[] = [];
    const subscription = service.events().subscribe((event) => received.push(event));

    opened[0].emit('open');

    expect(received).toEqual([{ type: 'connected' }]);
    subscription.unsubscribe();
  });

  it('keeps the observable alive while the browser reconnects by itself', () => {
    let failed = false;
    const subscription = service.events().subscribe({ error: () => (failed = true) });

    opened[0].readyState = 0; // CONNECTING: the browser retries
    opened[0].emit('error');

    expect(failed).toBeFalse();
    subscription.unsubscribe();
  });

  it('errors when the browser gives up on the stream (CLOSED), and closes the source', () => {
    let failed = false;
    service.events().subscribe({ error: () => (failed = true) });

    opened[0].readyState = 2; // CLOSED, e.g. a 502 while the backend restarts
    opened[0].emit('error');

    expect(failed).toBeTrue();
    expect(opened[0].closed).toBeTrue();
  });
});
