package at.willhaben.driven.adapter.persistence.quote

import at.willhaben.domain.ports.driven.QuoteQueryPort
import org.springframework.stereotype.Component

@Component
class QuoteQueryAdapter(): QuoteQueryPort {
    override fun getQuoteForSailorsCode(sailorsCode: Int): String {
        TODO("Not yet implemented")
    }
}
