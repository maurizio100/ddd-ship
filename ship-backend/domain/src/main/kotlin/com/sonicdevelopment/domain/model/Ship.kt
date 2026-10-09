package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.NewShippingRefusedException
import com.sonicdevelopment.domain.exception.RefusedCargoException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
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
    /** The Harbor where the ship was registered; never changes, travels with the ship. */
    val homeHarbor: HarborName,
    /** The Origin Harbor of the Arrival that last took this ship into this Harbor's fleet; `null` if it was registered here. */
    val arrivedFrom: HarborName? = null,
    var activeShipping: Shipping? = null,
    private val cargoLoad: MutableList<Cargo> = mutableListOf(),
    cargoAboard: List<Cargo> = emptyList(),
    incoming: Boolean = false,
    earnings: Money = Money.dollars(0),
) {

    /** The Earnings the ship carries until it reaches its Home Harbor. */
    var earnings: Money = earnings
        private set

    /**
     * The Cargo aboard, one entry per Cargo instance; separate from the Loaded Cargo of an Active Shipping. It
     * exists while the ship is not at sea: an Incoming Ship keeps the Cargo it arrived with here until it is
     * unloaded ([unload] clears it together with [isIncoming]) or refused away from home ([refuse] moves it
     * into the Loaded Cargo of the voyage home). Cargo aboard without [isIncoming] was refused by the ship's
     * own Home Harbor: it stays aboard beside a `PREPARING` Shipping, cannot be unloaded into the Stock
     * ([removeCargo]) and becomes Loaded Cargo on [release].
     */
    var cargoAboard: List<Cargo> = cargoAboard.toList()
        private set

    /** Whether the ship is an Incoming Ship: it arrived here with Cargo aboard that is not yet unloaded or refused. */
    var isIncoming: Boolean = incoming
        private set

    init {
        require(!incoming || cargoAboard.isNotEmpty()) { "An Incoming Ship has Cargo aboard" }
    }

    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) {
            field = if(isValidName(newShipName)) newShipName else field
        }

    /**
     * Starts a new Shipping; Cargo aboard that the Home Harbor refused stays aboard beside it. Refused for an Incoming Ship and for a ship whose Active Shipping is not `DONE`.
     */
    fun createNewShipping() {
        if (isIncoming) throw NewShippingRefusedException("$shipName must be unloaded or refused first")

        if (activeShipping == null) {
            activeShipping = Shipping(ShippingId(UUID.randomUUID()))
            return
        }

        if (activeShipping?.shippingState == ShippingState.DONE) {
            activeShipping = Shipping(ShippingId(UUID.randomUUID()))
            return
        }

        throw NewShippingRefusedException("$shipName already has an Active Shipping")
    }

    fun release(shippingQuote: ShippingQuote, destinationHarbor: HarborName) {
        val preparing = activeShipping?.takeIf { it.shippingState == ShippingState.PREPARING }
            ?: throw ShippingNotPreparingException("$shipName is not being prepared")
        moveCargoAboardIntoLoadedCargo()
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

    /**
     * Unloads an Incoming Ship: returns its Cargo aboard, one entry per Cargo instance, and clears the Cargo
     * aboard and the Incoming flag together. Refused for a ship that is not Incoming.
     */
    fun unload(): List<Cargo> {
        if (!isIncoming) throw ShipNotIncomingException("$shipName is not an Incoming Ship")
        val unloaded = cargoAboard
        cargoAboard = emptyList()
        isIncoming = false
        return unloaded
    }

    /**
     * The ship earns the [deliveryPrice] paid for its Cargo at [currentHarbor]. Away from its Home Harbor the
     * Delivery Price is added to its Earnings and `true` is returned. At its Home Harbor nothing changes and
     * `false` is returned: the caller credits the Delivery Price to the Savings at once.
     */
    fun earn(deliveryPrice: Money, currentHarbor: HarborName): Boolean = TODO("STORY-049")

    /**
     * Refuses an Incoming Ship at [currentHarbor]. At its Home Harbor it only stops being Incoming: the Cargo
     * aboard stays aboard and no Shipping is started. Anywhere else it starts a new Shipping, moves every Cargo
     * aboard into its Loaded Cargo, and clears the Cargo aboard and the Incoming flag together, so that the ship
     * is ready to be Released to its Home Harbor. Changes nothing when it is refused.
     */
    fun refuse(currentHarbor: HarborName) {
        if (!isIncoming) throw ShipNotIncomingException("$shipName is not an Incoming Ship")

        if (homeHarbor != currentHarbor) {
            activeShipping = Shipping(ShippingId(UUID.randomUUID()))
            moveCargoAboardIntoLoadedCargo()
        }
        isIncoming = false
    }

    private fun moveCargoAboardIntoLoadedCargo() {
        cargoLoad.addAll(cargoAboard)
        cargoAboard = emptyList()
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
        get() = cargoLoad.toList()

    private var currentWeight: Float = calculateWeight()
    fun addCargo(cargo: Cargo) {
        if (isShipLoadToHeavy(cargo)) throw ShipTooHeavyException("Loading ${cargo.name} would exceed the Max Weight of $MAX_WEIGHT")

        cargoLoad.add(cargo)
        currentWeight += cargo.weight
    }

    private fun isShipLoadToHeavy(cargo: Cargo) =
        (currentWeight + cargo.weight)  > MAX_WEIGHT

    /** Unloads one instance of [cargo]; returns whether it was on board. The Current Weight changes only if it was. */
    fun removeCargo(cargo: Cargo): Boolean {
        val loaded = cargoLoad.firstOrNull { it.id == cargo.id }
        if (loaded == null) {
            if (cargoAboard.any { it.id == cargo.id }) {
                throw RefusedCargoException("${cargo.name} aboard $shipName was refused here and can only be delivered to another Harbor")
            }
            return false
        }
        cargoLoad.remove(loaded)

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

    private fun calculateWeight() = (cargoLoad + cargoAboard).sumOf { it.weight.toDouble() }.toFloat()
}
