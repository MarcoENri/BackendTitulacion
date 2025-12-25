package com.example.Aplicativo_web.dto

data class CreateUserRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val roles: List<String> // ["COORDINATOR"] o ["TUTOR"] o ["JURY"]
)

