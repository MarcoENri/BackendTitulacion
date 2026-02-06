package com.example.Aplicativo_web.dto.finaldefense

import com.example.Aplicativo_web.entity.StudentEntity
import com.example.Aplicativo_web.entity.AppUserEntity
import com.example.Aplicativo_web.entity.finaldefense.*
import com.example.Aplicativo_web.entity.enums.FinalDefenseBookingStatus
import com.example.Aplicativo_web.entity.enums.FinalDefenseVerdict
import java.time.LocalDateTime

data class FinalDefenseWindowDto(
    val id: Long,
    val academicPeriodId: Long,
    val academicPeriodName: String,
    val careerId: Long?,
    val careerName: String?,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val isActive: Boolean,
    val hasRubric: Boolean // ✅ NUEVO
) {
    companion object {
        fun from(w: FinalDefenseWindowEntity) = FinalDefenseWindowDto(
            id = w.id!!,
            academicPeriodId = w.academicPeriod!!.id!!,
            academicPeriodName = w.academicPeriod!!.name,
            careerId = w.career?.id,
            careerName = w.career?.name,
            startsAt = w.startsAt,
            endsAt = w.endsAt,
            isActive = w.isActive,
            hasRubric = !w.rubricPath.isNullOrBlank() // ✅
        )
    }
}


data class FinalDefenseSlotDto(
    val id: Long,
    val windowId: Long,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val booked: Boolean,
    val bookingId: Long?
) {
    companion object {
        fun from(s: FinalDefenseSlotEntity, booked: Boolean, bookingId: Long?) = FinalDefenseSlotDto(
            id = s.id!!,
            windowId = s.window!!.id!!,
            startsAt = s.startsAt,
            endsAt = s.endsAt,
            booked = booked,
            bookingId = bookingId
        )
    }
}

data class FinalDefenseStudentMiniDto(
    val id: Long,
    val dni: String,
    val fullName: String,
    val email: String,
    val status: String,
    val projectName: String? = null
) {
    companion object {
        fun from(s: StudentEntity) = FinalDefenseStudentMiniDto(
            id = s.id!!,
            dni = s.dni,
            fullName = "${s.firstName} ${s.lastName}",
            email = s.email,
            status = s.status.name
        )
    }
}

data class FinalDefenseJuryDto(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String
) {
    companion object {
        fun from(u: AppUserEntity) = FinalDefenseJuryDto(
            id = u.id!!,
            username = u.username,
            fullName = u.fullName,
            email = u.email
        )
    }
}

data class FinalDefenseBookingDto(
    val id: Long,
    val status: FinalDefenseBookingStatus,
    val slotId: Long,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val academicPeriodId: Long,
    val careerId: Long,
    val careerName: String,
    val groupId: Long,
    val projectName: String?,
    val students: List<FinalDefenseStudentMiniDto>,
    val jury: List<FinalDefenseJuryDto>,
    val finalAverage: Double?,
    val verdict: FinalDefenseVerdict?,
    val finalObservations: String?,
    val actaPath: String?
)

data class FinalDefenseEvaluationDto(
    val id: Long,
    val bookingId: Long,
    val studentId: Long,
    val juryUserId: Long,
    val juryName: String,
    val rubricScore: Int,
    val extraScore: Int,
    val totalScore: Int,
    val observations: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: FinalDefenseEvaluationEntity) =
            FinalDefenseEvaluationDto(
                id = e.id!!,
                bookingId = e.booking.id!!,
                studentId = e.student.id!!,
                juryUserId = e.juryUser.id!!,
                juryName = e.juryUser.fullName,
                rubricScore = e.rubricScore,
                extraScore = e.extraScore,
                totalScore = e.totalScore,
                observations = e.observations,
                createdAt = e.createdAt
            )
    }
}

