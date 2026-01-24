package com.example.Aplicativo_web.dto

data class AdminAssignStudentRequest(
    val coordinatorId: Long,
    val academicPeriodId: Long? = null  // opcional: para validar periodo si quieres
)
