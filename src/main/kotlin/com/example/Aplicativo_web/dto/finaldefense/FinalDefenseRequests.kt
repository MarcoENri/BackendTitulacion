package com.example.Aplicativo_web.dto.finaldefense

import java.time.LocalDateTime

data class CreateFinalDefenseWindowRequest(
    val academicPeriodId: Long? = null,
    val careerId: Long? = null,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime
)

data class CreateFinalDefenseSlotRequest(
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime
)

data class CreateFinalDefenseBookingRequest(
    val slotId: Long,
    val studentIds: List<Long>,
    val juryUserIds: List<Long>,
    val finalObservations: String? = null
)

data class CreateFinalDefenseEvaluationRequest(
    val rubricScore: Int,   // 0..50
    val extraScore: Int,    // 0..50
    val observations: String? = null
)
