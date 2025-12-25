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
    val career: String,
    val titulationType: String,
    val status: String,
    val incidentCount: Long,
    val observationCount: Long
)
