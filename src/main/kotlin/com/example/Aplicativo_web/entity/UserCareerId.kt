package com.example.Aplicativo_web.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserCareerId(
    @Column(name = "user_id")
    var userId: Long = 0,

    @Column(name = "career_id")
    var careerId: Long = 0
) : Serializable
