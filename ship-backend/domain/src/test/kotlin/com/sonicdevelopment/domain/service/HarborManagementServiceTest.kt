package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import java.util.*

class HarborManagementServiceTest {

    private val currentHarbor = HarborName("Tortuga")
    private val knownHarbors = mockk<KnownHarborRepositoryPort>(relaxed = true)
    private val harborOutbox = mockk<HarborOutboxRepositoryPort>(relaxed = true)
    private val inbox = mockk<InboxRepositoryPort>()

    private val service = HarborManagementService(currentHarbor, knownHarbors, harborOutbox, inbox)

    @Test
    fun `learns about another Harbor`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns true

        service.learnAboutHarbor(eventId, HarborName("Port Royal"))

        verifyOrder {
            inbox.recordConsumedEvent(eventId)
            knownHarbors.rememberHarbor(HarborName("Port Royal"))
        }
    }

    @Test
    fun `does not remember its own Harbor Name`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns true

        service.learnAboutHarbor(eventId, currentHarbor)

        verify { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 0) { knownHarbors.rememberHarbor(any()) }
    }

    @Test
    fun `ignores an already consumed Harbor Opened`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns false

        service.learnAboutHarbor(eventId, HarborName("Port Royal"))

        verify(exactly = 0) { knownHarbors.rememberHarbor(any()) }
    }

    @Test
    fun `opening publishes Harbor Opened with the current Harbor Name`() {
        service.openHarbor()

        verify(exactly = 1) { harborOutbox.publishHarborOpened(currentHarbor) }
    }
}
