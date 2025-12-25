package com.example.Aplicativo_web.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val enabled: Boolean,
    val roles: List<String>
)