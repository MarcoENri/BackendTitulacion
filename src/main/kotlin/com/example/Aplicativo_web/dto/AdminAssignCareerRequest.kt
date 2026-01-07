package com.example.Aplicativo_web.dto

data class AdminAssignCareerRequest(
    val coordinatorId: Long,
    val tutorId: Long?,
    val projectName: String?,
    val onlyUnassigned: Boolean? = true
)
