package com.example.Aplicativo_web.dto

import java.time.LocalDate

data class CreateIncidentRequest(
    val stage: String,
    val date: LocalDate,
    val reason: String,
    val action: String
)
