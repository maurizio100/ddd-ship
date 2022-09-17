package at.willhaben.domain.model

class Ship(
    val id: Long? = null,
    name: String
) {
    val shipName = if (isValidName(name)) name else throw IllegalArgumentException()
    private fun isValidName(name: String) = name.isNotBlank() && name.length < 255
}
