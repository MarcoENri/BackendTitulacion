package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RoleRepository : JpaRepository<RoleEntity, Long> {
    fun findByName(name: String): Optional<RoleEntity>
}
