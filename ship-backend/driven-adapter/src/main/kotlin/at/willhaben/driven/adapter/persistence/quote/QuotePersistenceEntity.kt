package at.willhaben.driven.adapter.persistence.quote

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "quotes")
class QuotePersistenceEntity(
    @Id
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "quote")
    var quote: String
) {
}
