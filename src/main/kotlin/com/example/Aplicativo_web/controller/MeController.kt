package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.MeResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MeController {

    @GetMapping("/me")
    fun me(auth: Authentication): ResponseEntity<MeResponse> {
        val roles = auth.authorities.map { it.authority } // ej: ROLE_ADMIN, ROLE_COORDINATOR
        return ResponseEntity.ok(MeResponse(username = auth.name, roles = roles))


    }
}
