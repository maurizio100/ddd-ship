package com.sonicdevelopment.driven.adapter.persistence.ship

import jakarta.persistence.*
import java.util.UUID

/** One Crew Member aboard a ship. */
@Entity
@Table(name = "crew_members")
class CrewMemberPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "crew_member_id")
    var crewMemberId: UUID,

    @Column(name = "crew_member_name")
    var name: String,

    @Column(name = "crew_member_trait")
    var trait: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id")
    var ship: ShipPersistenceEntity,
)
