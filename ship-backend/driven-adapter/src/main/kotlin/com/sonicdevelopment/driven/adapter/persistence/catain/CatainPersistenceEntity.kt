package com.sonicdevelopment.driven.adapter.persistence.catain

import jakarta.persistence.*

@Entity
@Table(name = "catains")
class CatainPersistenceEntity(

    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @Column(name = "catain_name")
    var catainName: String,

    @Column(name = "catain_image_id")
    var catainImageId: String

) { }