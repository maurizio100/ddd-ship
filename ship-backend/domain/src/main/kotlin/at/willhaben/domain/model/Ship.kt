package at.willhaben.domain.model

class Ship(
    id: Long? = null,
    name: String? = null,
    private val cargoLoad: List<Cargo> = listOf()
) {
    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) {
            field = if(isValidName(newShipName)) newShipName else field
        }

    private fun isValidName(name: String?) = name?.let { it.isNotBlank() && it.length < 255} ?: false

    fun addCargo(cargo: Cargo) {
        cargoLoad.toMutableList().add(cargo)
    }
    val loadedCargo: List<Cargo>
        get() = cargoLoad

    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }
}
