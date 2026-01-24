package com.example.Aplicativo_web.dto

data class AdminAssignCareerRequest(
    val careerId: Long,                 // ✅ NUEVO (OBLIGATORIO)
    val coordinatorId: Long,
    val tutorId: Long? = null,
    val projectName: String? = null,
    val onlyUnassigned: Boolean? = true,
    val academicPeriodId: Long? = null  // si viene null -> usa periodo activo
)
