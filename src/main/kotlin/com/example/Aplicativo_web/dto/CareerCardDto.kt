package com.example.Aplicativo_web.dto

data class CareerCardDto(
    val id: Long,
    val name: String,
    val color: String?,
    val coverImage: String?,
    val studentsCount: Long
)
