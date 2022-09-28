package at.willhaben.driven.adapter.persistence.quote

import at.willhaben.domain.ports.driven.QuoteQueryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class QuoteQueryAdapter(
    private val quoteRepository: QuoteRepository
): QuoteQueryPort {
    override fun getQuoteForSailorsCode(sailorsCode: Int): String {
        return quoteRepository.findByIdOrNull(sailorsCode.toLong())?.let {
            it.quote
        } ?: "Yeah go and sail!"
    }
}
