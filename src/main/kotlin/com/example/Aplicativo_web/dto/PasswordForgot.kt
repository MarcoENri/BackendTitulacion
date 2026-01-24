package com.example.Aplicativo_web.dto

data class ForgotPasswordRequest(val email: String)

// para pruebas te devuelvo el token (luego lo quitas y lo mandas por correo)
data class ForgotPasswordResponse(val message: String, val token: String)

data class ResetPasswordRequest(val token: String, val newPassword: String)
