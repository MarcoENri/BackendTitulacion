package com.example.Aplicativo_web.dto

data class CreateUserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val role: String
)


