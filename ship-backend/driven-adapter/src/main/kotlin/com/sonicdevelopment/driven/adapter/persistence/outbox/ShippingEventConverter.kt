package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class ShippingEventConverter : AttributeConverter<ShippingEvent, String> {

    private val objectMapper = ObjectMapper()


    override fun convertToDatabaseColumn(event: ShippingEvent?): String? {
        return event?.let { objectMapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(eventString: String?): ShippingEvent? {
        return eventString?.let { objectMapper.readValue(it, ShippingEvent::class.java) }
    }
}