package com.example.Aplicativo_web.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_career")
class UserCareerEntity(
    @EmbeddedId
    var id: UserCareerId = UserCareerId(),

    @ManyToOne
    @MapsId("userId")
    var user: AppUserEntity? = null,

    @ManyToOne
    @MapsId("careerId")
    var career: CareerEntity? = null
)
