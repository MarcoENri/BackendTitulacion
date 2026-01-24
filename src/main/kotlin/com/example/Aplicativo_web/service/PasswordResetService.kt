package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.ForgotPasswordResponse
import com.example.Aplicativo_web.entity.PasswordResetTokenEntity
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.PasswordResetTokenRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

@Service
class PasswordResetService(
    private val userRepo: AppUserRepository,
    private val tokenRepo: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun forgotPassword(email: String): ForgotPasswordResponse {
        val user = userRepo.findByEmailIgnoreCase(email)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Correo no registrado") }

        val token = UUID.randomUUID().toString()
        val expires = LocalDateTime.now().plusMinutes(30)

        tokenRepo.save(
            PasswordResetTokenEntity(
                token = token,
                user = user,
                expiresAt = expires,
                used = false
            )
        )

        // ✅ Para tesis/pruebas: devolvemos el token.
        // En producción: aquí mandarías email con el link.
        return ForgotPasswordResponse(
            message = "Token generado (válido 30 min).",
            token = token
        )
    }

    fun resetPassword(token: String, newPassword: String) {
        if (newPassword.length < 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres")
        }

        val t = tokenRepo.findByToken(token)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido") }

        if (t.used) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token ya usado")
        if (t.expiresAt.isBefore(LocalDateTime.now())) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado")

        val user = t.user ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Token sin usuario")

        user.password = passwordEncoder.encode(newPassword)
        userRepo.save(user)

        t.used = true
        tokenRepo.save(t)
    }
}
