package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_user")
class AppUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(name = "full_name", nullable = false)
    var fullName: String = "",

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var roles: MutableSet<UserRoleEntity> = mutableSetOf()
)
