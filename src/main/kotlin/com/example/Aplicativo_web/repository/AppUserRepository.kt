package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.AppUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AppUserRepository : JpaRepository<AppUserEntity, Long> {

    @Query("""
        select u from AppUserEntity u
        left join fetch u.roles ur
        left join fetch ur.role r
        where u.username = :username
    """)
    fun findByUsernameWithRoles(@Param("username") username: String): Optional<AppUserEntity>
    fun findByUsername(username: String): AppUserEntity?

    fun existsByUsername(username: String): Boolean
}
