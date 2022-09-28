package at.willhaben.domain.ports.driven

interface QuoteQueryPort {
    fun getQuoteForSailorsCode(sailorsCode: Int): String
}
