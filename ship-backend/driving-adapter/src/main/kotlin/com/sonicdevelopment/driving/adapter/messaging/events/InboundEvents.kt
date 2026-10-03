/**
 * Inbound copies of other Harbors' events, one `<Name>InboundEvent` class per event shape.
 *
 * They are deliberately not shared with the outbox payloads in `driven.adapter.persistence.outbox.events`:
 * the backend keeps its own copy of each event shape it consumes and ignores fields it does not know.
 */
package com.sonicdevelopment.driving.adapter.messaging.events
