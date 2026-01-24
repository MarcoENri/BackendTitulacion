package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.enums.StudentStatus

data class StudentRowDto(
    val id: Long,
    val dni: String,
    val firstName: String,
    val lastName: String,
    val email: String,

    val corte: String,
    val section: String,
    val modality: String?,

    val career: String,
    val titulationType: String,
    val status: StudentStatus,

    val incidentCount: Long,
    val observationCount: Long,

    // Útil para el frontend (si quieres mostrarlo)
    val academicPeriodId: Long?,
    val academicPeriodName: String?
)
