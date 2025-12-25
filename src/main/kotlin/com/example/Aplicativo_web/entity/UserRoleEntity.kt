package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.io.Serializable

@Embeddable
class UserRoleId(
    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(name = "role_id")
    var roleId: Long? = null
) : Serializable

@Entity
@Table(name = "user_role")
class UserRoleEntity(
    @EmbeddedId
    var id: UserRoleId = UserRoleId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: AppUserEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    var role: RoleEntity? = null
)
