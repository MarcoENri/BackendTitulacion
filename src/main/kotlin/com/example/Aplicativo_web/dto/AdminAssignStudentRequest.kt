package com.example.Aplicativo_web.dto

data class AdminAssignStudentRequest(
    val coordinatorId: Long,
    val tutorId: Long?,
    val projectName: String?
)
