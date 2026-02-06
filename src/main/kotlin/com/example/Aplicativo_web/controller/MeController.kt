package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.MeResponse
import com.example.Aplicativo_web.repository.AppUserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MeController(
    private val appUserRepository: AppUserRepository
) {

    @GetMapping("/me")
    fun me(auth: Authentication): ResponseEntity<MeResponse> {

        val user = appUserRepository
            .findByUsernameIgnoreCase(auth.name)
            .orElse(null)
            ?: return ResponseEntity.notFound().build()

        val roles = auth.authorities.map { it.authority }

        return ResponseEntity.ok(
            MeResponse(
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                roles = roles
            )
        )
    }
}
