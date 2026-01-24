package com.example.Aplicativo_web.dto

import java.time.LocalDate

data class CreateAcademicPeriodRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isActive: Boolean = false
)
