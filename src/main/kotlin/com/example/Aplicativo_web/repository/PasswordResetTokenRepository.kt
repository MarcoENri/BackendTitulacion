package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByToken(token: String): Optional<PasswordResetTokenEntity>
}
