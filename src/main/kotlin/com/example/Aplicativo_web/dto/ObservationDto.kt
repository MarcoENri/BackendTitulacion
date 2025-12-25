package com.example.Aplicativo_web.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class ObservationDto(
    val id: Long,
    val author: String,
    val text: String,
    val createdAt: LocalDateTime,
    val authorUserId: Long?
)