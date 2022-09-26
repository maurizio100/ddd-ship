package at.willhaben.domain.model

class Ship(
    id: Long? = null,
    name: String? = null,
    private val cargoLoad: MutableList<Cargo> = mutableListOf()
) {

    companion object {
        const val MAX_WEIGHT = 22.0F
    }

    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) {
            field = if(isValidName(newShipName)) newShipName else field
        }

    private fun isValidName(name: String?) = name?.let { it.isNotBlank() && it.length < 255} ?: false

    fun addCargo(cargo: Cargo) {
        cargoLoad.add(cargo)
    }

    fun removeCargo(cargo: Cargo) {
        cargoLoad.remove(cargo)
    }

    val loadedCargo: List<Cargo>
        get() = cargoLoad

    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }

    val weight: Float
        get() = cargoLoad.sumOf{ it.weight.toDouble() }.toFloat()

    val maxWeight: Float
        get() = MAX_WEIGHT
}
