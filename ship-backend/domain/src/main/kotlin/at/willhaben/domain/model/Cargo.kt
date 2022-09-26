package at.willhaben.domain.model

class Cargo(
    val id: Long,
    val name: String,
    val weight: Float
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        other as Cargo
        return other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
