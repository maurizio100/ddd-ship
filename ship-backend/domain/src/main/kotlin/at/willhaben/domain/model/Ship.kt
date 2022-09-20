package at.willhaben.domain.model

class Ship(
    id: Long? = null,
    name: String? = null,
) {
    var shipName = name?.let { if(isValidName(it)) it else throw IllegalArgumentException() } ?: throw IllegalArgumentException()
        set(newShipName) { field = if(isValidName(newShipName)) newShipName else field }

    private fun isValidName(name: String?) = name?.let { it.isNotBlank() && it.length < 255} ?: false

    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }
}
