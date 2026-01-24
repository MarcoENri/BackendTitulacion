package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.AppUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AppUserRepository : JpaRepository<AppUserEntity, Long> {

    // --- Basic Lookups (Case Insensitive for better UX) ---

    fun findByUsernameIgnoreCase(username: String): Optional<AppUserEntity>

    fun findByEmailIgnoreCase(email: String): Optional<AppUserEntity>

    fun findByUsernameIgnoreCaseOrEmailIgnoreCase(username: String, email: String): Optional<AppUserEntity>

    // --- Validation Checks ---

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    @Query("""
    select u from AppUserEntity u
    left join fetch u.roles ur
    left join fetch ur.role r
    where lower(u.username) = lower(:input)
       or lower(u.email) = lower(:input)
""")
    fun findByUsernameOrEmailWithRoles(@Param("input") input: String): Optional<AppUserEntity>


    // --- Advanced Queries (Optimized for Relationships) ---

    /* fetches user + roles + role details in a single query.
       Crucial for Spring Security UserDetailsService to avoid LazyInitializationException.
    */
    @Query("""
        select u from AppUserEntity u
        left join fetch u.roles ur
        left join fetch ur.role r
        where u.username = :username
    """)
    fun findByUsernameWithRoles(@Param("username") username: String): Optional<AppUserEntity>

    /* Trae usuarios por rol específico.
       Uses 'distinct' to avoid duplicate user rows due to the join.
    */
    @Query("""
        select distinct u from AppUserEntity u
        join fetch u.roles ur
        join fetch ur.role r
        where r.name = :roleName
    """)
    fun findAllByRoleNameWithRoles(@Param("roleName") roleName: String): List<AppUserEntity>
}