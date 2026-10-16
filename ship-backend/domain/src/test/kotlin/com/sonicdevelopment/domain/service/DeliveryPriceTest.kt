package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.values.Money
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DeliveryPriceTest {

    private val rum = aCargo(name = "Rum")
    private val sugar = aCargo(name = "Sugar", weight = 0.7F)
    private val prices = mapOf(rum.id to Money.of("40.00"), sugar.id to Money.of("35.00"))

    @Test
    fun `the Delivery Price counts each Cargo as often as it is aboard`() {
        DeliveryPrice.of(listOf(rum, rum, sugar), prices) shouldBe Money.of("115.00")
    }

    @Test
    fun `the Delivery Price is null when one Cargo has no Price`() {
        val silk = aCargo(name = "Silk")

        DeliveryPrice.of(listOf(rum, silk), prices) shouldBe null
    }
}
