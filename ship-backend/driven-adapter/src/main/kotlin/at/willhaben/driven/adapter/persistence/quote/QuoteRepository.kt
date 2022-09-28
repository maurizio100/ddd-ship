package at.willhaben.driven.adapter.persistence.quote

import org.springframework.data.jpa.repository.JpaRepository

interface QuoteRepository: JpaRepository<QuotePersistenceEntity, Long> {
}
