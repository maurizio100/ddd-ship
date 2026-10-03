package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.HarborOpenedEvent
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

@Component
class HarborOutboxRepositoryAdapter(
    private val shippingOutboxPersistenceRepository: ShippingOutboxPersistenceRepository
) : HarborOutboxRepositoryPort {

    private val objectMapper = ObjectMapper()

    override fun publishHarborOpened(harborName: HarborName) {
        shippingOutboxPersistenceRepository.save(
            ShippingOutboxPersistenceEntity(
                aggregatetype = "harbor",
                aggregateId = harborKey(harborName),
                type = "harbor-opened",
                payload = objectMapper.writeValueAsString(HarborOpenedEvent(harborName.name))
            )
        )
    }

    private companion object {
        /** uuid5(NAMESPACE_URL, "https://github.com/maurizio100/ddd-ship/harbor"); shared by every Harbor. */
        val HARBOR_NAMESPACE: UUID = UUID.fromString("d5a17a2e-8e74-5937-8c7d-101e395ae650")

        /**
         * The Kafka key of a Harbor's events: the name-based UUID version 5 (RFC 4122, SHA-1) of its
         * Harbor Name, so it is the same on every restart and compaction keeps one record per Harbor.
         */
        fun harborKey(harborName: HarborName): UUID {
            val namespace = ByteBuffer.allocate(16)
                .putLong(HARBOR_NAMESPACE.mostSignificantBits)
                .putLong(HARBOR_NAMESPACE.leastSignificantBits)
                .array()
            val hash = MessageDigest.getInstance("SHA-1").run {
                update(namespace)
                digest(harborName.name.toByteArray(Charsets.UTF_8))
            }
            hash[6] = ((hash[6].toInt() and 0x0f) or 0x50).toByte()
            hash[8] = ((hash[8].toInt() and 0x3f) or 0x80).toByte()
            val buffer = ByteBuffer.wrap(hash, 0, 16)
            return UUID(buffer.long, buffer.long)
        }
    }
}
