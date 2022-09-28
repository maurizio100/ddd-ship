package at.willhaben.domain.model

import at.willhaben.domain.exception.ShipTooHeavyException
import java.text.DecimalFormat

class Ship(
    id: Long? = null,
    name: String? = null,
    val cargoLoad: MutableList<Cargo> = mutableListOf()
) {

    companion object {
        const val MAX_WEIGHT = 22.0F
    }

    val sailorsCode: Int
        get() = (System.currentTimeMillis() / currentWeight).toLong().mod(14)

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
        get() = cargoLoad

    private var currentWeight: Float = calculateWeight()
    fun addCargo(cargo: Cargo) {
        if (isShipLoadToHeavy(cargo)) throw ShipTooHeavyException("The ship gets too heavy with that cargo!")

        cargoLoad.add(cargo)
        currentWeight += cargo.weight
    }

    private fun isShipLoadToHeavy(cargo: Cargo) = (currentWeight + cargo.weight)  > MAX_WEIGHT

    fun removeCargo(cargo: Cargo) {
        cargoLoad.remove(cargo)
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

    private fun calculateWeight() = cargoLoad.sumOf{ it.weight.toDouble() }.toFloat()
}
