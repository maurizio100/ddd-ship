package at.willhaben.domain.model

class Shipping(
    id: Long? = null,
    val sailorsQuote: String
) {
    var id = id
        set(newId) {
            field = id?.let { id } ?: newId
        }


}
