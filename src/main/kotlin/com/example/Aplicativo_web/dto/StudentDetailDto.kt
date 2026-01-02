package com.example.Aplicativo_web.dto

import java.time.LocalDateTime

data class StudentDetailDto(
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

    val tutorId: Long?,
    val coordinatorId: Long?,
    val thesisProject: String?,
    val thesisProjectSetAt: LocalDateTime?,

    val incidentCount: Long,
    val observationCount: Long,

    val incidents: List<IncidentDto>,
    val observations: List<ObservationDto>
)
