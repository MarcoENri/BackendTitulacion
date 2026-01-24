package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.PredefenseWindowEntity
import java.time.LocalDateTime

data class PredefenseWindowDto(
    val id: Long,
    val academicPeriodId: Long,
    val academicPeriodName: String,
    val careerId: Long?,
    val careerName: String?,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val isActive: Boolean
) {
    companion object {
        fun from(w: PredefenseWindowEntity): PredefenseWindowDto {
            val p = w.academicPeriod ?: throw IllegalStateException("Window sin academicPeriod")
            return PredefenseWindowDto(
                id = w.id!!,
                academicPeriodId = p.id!!,
                academicPeriodName = p.name,
                careerId = w.career?.id,
                careerName = w.career?.name,
                startsAt = w.startsAt,
                endsAt = w.endsAt,
                isActive = w.isActive
            )
        }
    }
}
