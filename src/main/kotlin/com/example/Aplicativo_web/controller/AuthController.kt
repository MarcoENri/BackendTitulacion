package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.LoginRequest
import com.example.Aplicativo_web.dto.LoginResponse
import com.example.Aplicativo_web.security.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.username, req.password)
        )

        val token = jwtService.generateToken(auth.name, auth.authorities)
        return ResponseEntity.ok(LoginResponse(token = token, username = auth.name))
    }
}
