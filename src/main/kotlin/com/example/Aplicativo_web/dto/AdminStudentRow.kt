package com.example.Aplicativo_web.dto

data class AdminStudentRow(
    val id: Long,
    val dni: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val corte: String,
    val section: String,
    val modality: String?,

    val career: String,        // ✅ se mantiene (no rompe nada)
    val careerId: Long?,       // ✅ NUEVO (clave para el frontend)

    val titulationType: String,
    val status: String,
    val incidentCount: Long,
    val observationCount: Long,
    val academicPeriodName: String
)
