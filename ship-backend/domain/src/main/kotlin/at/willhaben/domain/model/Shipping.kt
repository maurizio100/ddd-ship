package at.willhaben.domain.model

class Shipping(
    id: Long? = null,
    val sailorsCode: String
) {
    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }


}
