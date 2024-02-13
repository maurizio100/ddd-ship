package com.sonicdevelopment.domain.ports.driven

interface QuoteQueryPort {
    fun getQuoteForSailorsCode(sailorsCode: Int): String
}
