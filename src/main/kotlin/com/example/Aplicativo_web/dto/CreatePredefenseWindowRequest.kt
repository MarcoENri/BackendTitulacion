package com.example.Aplicativo_web.dto

import java.time.LocalDateTime

data class CreatePredefenseWindowRequest(
    val academicPeriodId: Long? = null,
    val careerId: Long? = null,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime
)
