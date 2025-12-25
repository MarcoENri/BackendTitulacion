package com.example.Aplicativo_web.entity

import jakarta.persistence.*

@Entity
@Table(name = "role")
class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String = ""
)
