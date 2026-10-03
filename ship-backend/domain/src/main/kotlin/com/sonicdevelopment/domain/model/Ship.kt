package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.exception.ItemAlreadyLoadedException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.*
import java.text.DecimalFormat
import java.util.*

class Ship(
    val id: ShipId = ShipId(UUID.randomUUID()),
    name: String? = null,
    val catainId: CatainId,
    val catainName: String,
    var activeShipping: Shipping? = null,
    private val cargoLoad: MutableMap<CargoId, Cargo> = mutableMapOf()
) {

    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) {
            field = if(isValidName(newShipName)) newShipName else field
        }

    fun createNewShipping() {
        if (activeShipping == null) {
            activeShipping = Shipping(ShippingId(UUID.randomUUID()))
            return
        }

        if (activeShipping?.shippingState == ShippingState.DONE) {
            activeShipping = Shipping(ShippingId(UUID.randomUUID()))
            return
        }

        throw IllegalArgumentException()
    }

    fun release(shippingQuote: ShippingQuote, destinationHarbor: HarborName) {
        val preparing = activeShipping?.takeIf { it.shippingState == ShippingState.PREPARING }
            ?: throw ShippingNotPreparingException("$shipName is not being prepared")
        preparing.release(shippingQuote, destinationHarbor)
    }

    /**
     * The voyage is over: its ship has arrived at the Destination Harbor. Ends the Active Shipping only if it
     * is [shippingId] and at sea; returns whether it did, and changes nothing otherwise.
     */
    fun endShipping(shippingId: ShippingId): Boolean {
        val atSea = activeShipping
            ?.takeIf { it.id == shippingId && it.shippingState == ShippingState.SHIPPING }
            ?: return false
        atSea.end()
        return true
    }

    fun createSailorsCode(): SailorsCode {
        return SailorsCode(currentWeight)
    }

    fun shippingState(): ShippingState {
        return activeShipping?.shippingState ?: ShippingState.IDLE
    }

    private fun isValidName(name: String?) = name?.let { it.isNotBlank() && it.length < 255} ?: false

    companion object {
        const val MAX_WEIGHT = 15.0F
    }

    val loadedCargo: List<Cargo>
        get() = cargoLoad.values.toMutableList()

    private var currentWeight: Float = calculateWeight()
    fun addCargo(cargo: Cargo) {
        if (cargoLoad.contains(cargo.id)) throw ItemAlreadyLoadedException("${cargo.name} is already loaded on the ship")
        if (isShipLoadToHeavy(cargo)) throw ShipTooHeavyException("Loading ${cargo.name} would exceed the Max Weight of $MAX_WEIGHT")

        cargoLoad[cargo.id] = cargo
        currentWeight += cargo.weight
    }

    private fun isShipLoadToHeavy(cargo: Cargo) =
        (currentWeight + cargo.weight)  > MAX_WEIGHT

    /** Unloads [cargo]; returns whether it was on board. The Current Weight changes only if it was. */
    fun removeCargo(cargo: Cargo): Boolean {
        if (cargoLoad.remove(cargo.id) == null) return false

        if (currentWeight < cargo.weight) {
            currentWeight = 0.0F
        } else {
            currentWeight -= cargo.weight
        }
        return true
    }

    val weight: Float
        get() = DecimalFormat("#.##").format(currentWeight).toFloat()

    val maxWeight: Float
        get() = MAX_WEIGHT

    private fun calculateWeight() = cargoLoad.values.sumOf{ it.weight.toDouble() }.toFloat()
}
