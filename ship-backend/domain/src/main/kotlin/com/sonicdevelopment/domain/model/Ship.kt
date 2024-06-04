package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.ItemAlreadyLoadedException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import java.text.DecimalFormat
import java.time.LocalDateTime

class Ship(
    id: Long? = null,
    name: String? = null,
    private val cargoLoad: MutableMap<Long, Cargo> = mutableMapOf()
) {

    companion object {
        const val MAX_WEIGHT = 15.0F
    }

    val sailorsCode: Int
        get() = (currentWeight * LocalDateTime.now().minute).toInt().mod(14)

    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }

    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) {
            field = if(isValidName(newShipName)) newShipName else field
        }

    var shipping: Shipping? = null

    private fun isValidName(name: String?) = name?.let { it.isNotBlank() && it.length < 255} ?: false

    val loadedCargo: List<Cargo>
        get() = cargoLoad.values.toMutableList()

    private var currentWeight: Float = calculateWeight()
    fun addCargo(cargo: Cargo) {
        if (cargoLoad.contains(cargo.id)) throw ItemAlreadyLoadedException("The cargo is already loaded on the ship!")
        if (isShipLoadToHeavy(cargo)) throw ShipTooHeavyException("The ship gets too heavy with that cargo!")

        cargoLoad[cargo.id] = cargo
        currentWeight += cargo.weight
    }

    private fun isShipLoadToHeavy(cargo: Cargo) = (currentWeight + cargo.weight)  > MAX_WEIGHT

    fun removeCargo(cargo: Cargo) {
        cargoLoad.remove(cargo.id)
        if (currentWeight < cargo.weight) {
            currentWeight = 0.0F
        } else {
            currentWeight -= cargo.weight
        }
    }

    val weight: Float
        get() = DecimalFormat("#.##").format(currentWeight).toFloat()

    val maxWeight: Float
        get() = MAX_WEIGHT

    private fun calculateWeight() = cargoLoad.values.sumOf{ it.weight.toDouble() }.toFloat()
}
