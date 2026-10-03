import {Injectable, InjectionToken} from '@angular/core';
import {Observable} from 'rxjs';
import {FleetEvent} from '../models/fleet-event';

/** Builds the EventSource for a URL; specs provide a fake. */
export const EVENT_SOURCE_FACTORY = new InjectionToken<(url: string) => EventSource>('EVENT_SOURCE_FACTORY', {
  providedIn: 'root',
  factory: () => (url: string) => new EventSource(url),
});

@Injectable({
  providedIn: 'root'
})
export class FleetEventsService {

  events(): Observable<FleetEvent> {
    throw new Error('Not implemented');
  }
}
