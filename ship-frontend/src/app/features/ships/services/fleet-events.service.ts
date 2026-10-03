import {inject, Injectable, InjectionToken} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {FleetEvent, ShipArrived, ShipLeft} from '../models/fleet-event';

/** Builds the EventSource for a URL; specs provide a fake. */
export const EVENT_SOURCE_FACTORY = new InjectionToken<(url: string) => EventSource>('EVENT_SOURCE_FACTORY', {
  providedIn: 'root',
  factory: () => (url: string) => new EventSource(url),
});

/**
 * This Harbor's fleet changes as the backend pushes them (`GET /web/fleet-events`, Server-Sent Events).
 * Each subscription opens its own EventSource and closes it on unsubscribe. While the browser is reconnecting a
 * dropped stream by itself (readyState CONNECTING) nothing is emitted. When it gives up (readyState CLOSED, which
 * is what a non-200 answer such as a 502 from the proxy causes) the observable errors, so the subscriber can
 * open a new stream.
 */
@Injectable({
  providedIn: 'root'
})
export class FleetEventsService {
  private readonly eventSourceFactory = inject(EVENT_SOURCE_FACTORY);

  events(): Observable<FleetEvent> {
    return new Observable<FleetEvent>((subscriber) => {
      const source = this.eventSourceFactory(`${environment.baseUrl}/fleet-events`);
      source.addEventListener('open', () => subscriber.next({type: 'connected'}));
      source.addEventListener('error', () => {
        if (source.readyState === EventSource.CLOSED) {
          source.close();
          subscriber.error(new Error('The fleet-events stream was closed'));
        }
      });
      source.addEventListener('ship-arrived', (event) =>
        subscriber.next({type: 'ship-arrived', ship: JSON.parse((event as MessageEvent).data) as ShipArrived}));
      source.addEventListener('ship-left', (event) =>
        subscriber.next({type: 'ship-left', ship: JSON.parse((event as MessageEvent).data) as ShipLeft}));
      return () => source.close();
    });
  }
}
