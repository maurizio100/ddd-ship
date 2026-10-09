package com.sonicdevelopment.driven.adapter.persistence.inbox

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, InboxRepositoryAdapter::class)
@DbTest
class InboxRepositoryAdapterTest {

    @Autowired
    lateinit var inbox: InboxRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun truncateInbox() {
        jdbcTemplate.execute("TRUNCATE TABLE inbox_events")
    }

    @Test
    fun `records an unseen event id and reports it as new`() {
        val eventId = EventId(UUID.randomUUID())

        val recorded = inbox.recordConsumedEvent(eventId)

        recorded shouldBe true
        rowsFor(eventId) shouldBe 1
    }

    @Test
    fun `reports an already consumed event id as seen`() {
        val eventId = EventId(UUID.randomUUID())
        inbox.recordConsumedEvent(eventId)

        val recordedAgain = inbox.recordConsumedEvent(eventId)

        recordedAgain shouldBe false
        rowsFor(eventId) shouldBe 1
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to record outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> {
            inbox.recordConsumedEvent(EventId(UUID.randomUUID()))
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `a rolled-back transaction leaves the event id unconsumed`() {
        val eventId = EventId(UUID.randomUUID())
        val transaction = TransactionTemplate(transactionManager)

        transaction.executeWithoutResult { status ->
            inbox.recordConsumedEvent(eventId)
            status.setRollbackOnly()
        }
        val recordedInNewTransaction = transaction.execute { inbox.recordConsumedEvent(eventId) }

        recordedInNewTransaction shouldBe true
    }

    private fun rowsFor(eventId: EventId): Int =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId.id
        )!!
}
