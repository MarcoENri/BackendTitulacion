package com.example.Aplicativo_web.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class IncidentDto(
    val id: Long,
    val stage: String,
    val date: LocalDate,
    val reason: String,
    val action: String,
    val createdAt: LocalDateTime,
    val createdByUserId: Long?
)
