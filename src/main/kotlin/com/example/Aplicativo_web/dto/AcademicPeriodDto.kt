package com.example.Aplicativo_web.dto

import java.time.LocalDate

data class AcademicPeriodDto(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isActive: Boolean
)
