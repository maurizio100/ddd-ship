package com.sonicdevelopment.driven.adapter.persistence.harbor

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "known_harbors")
class KnownHarborPersistenceEntity(

    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @Column(name = "harbor_name")
    var harborName: String,

    @Column(name = "learned_at")
    var learnedAt: Instant
)
