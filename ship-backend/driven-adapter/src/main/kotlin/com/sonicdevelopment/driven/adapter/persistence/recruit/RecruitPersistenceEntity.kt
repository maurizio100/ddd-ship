package com.sonicdevelopment.driven.adapter.persistence.recruit

import jakarta.persistence.*
import java.util.UUID

/** One Recruit of this Harbor's Recruit Pool. Read-only for JPA: writes go through the native query. */
@Entity
@Table(name = "recruits")
class RecruitPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @Column(name = "recruit_id")
    var recruitId: UUID,

    @Column(name = "ordinal")
    var ordinal: Int,

    @Column(name = "name")
    var name: String,

    @Column(name = "trait")
    var trait: String
)
