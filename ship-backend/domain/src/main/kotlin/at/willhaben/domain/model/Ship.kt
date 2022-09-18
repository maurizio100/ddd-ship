package at.willhaben.domain.model

class Ship(
    id: Long? = null,
    name: String
) {
    val shipName = if (isValidName(name)) name else throw IllegalArgumentException()
    private fun isValidName(name: String) = name.isNotBlank() && name.length < 255

    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }
}
