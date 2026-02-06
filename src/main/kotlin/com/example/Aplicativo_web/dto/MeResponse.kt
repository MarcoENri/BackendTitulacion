package com.example.Aplicativo_web.dto

data class MeResponse(
    val username: String,
    val email: String,
    val fullName: String,
    val roles: List<String>
)