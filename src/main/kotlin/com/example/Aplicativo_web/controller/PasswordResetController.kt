package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.ForgotPasswordRequest
import com.example.Aplicativo_web.dto.ForgotPasswordResponse
import com.example.Aplicativo_web.dto.ResetPasswordRequest
import com.example.Aplicativo_web.service.PasswordResetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class PasswordResetController(
    private val resetService: PasswordResetService
) {

    @PostMapping("/forgot-password")
    fun forgot(@RequestBody req: ForgotPasswordRequest): ResponseEntity<Map<String, String>> {
        resetService.forgotPassword(req.email)
        return ResponseEntity.ok(
            mapOf("message" to "Se ha enviado un correo con las instrucciones")
        )
    }


    @PostMapping("/reset-password")
    fun reset(@RequestBody req: ResetPasswordRequest): ResponseEntity<Void> {
        resetService.resetPassword(req.token, req.newPassword)
        return ResponseEntity.ok().build()
    }
}
