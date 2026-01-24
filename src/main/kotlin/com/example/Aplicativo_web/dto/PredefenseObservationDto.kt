package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.PredefenseObservationEntity
import java.time.LocalDateTime

data class PredefenseObservationDto(
    val id: Long,
    val bookingId: Long,
    val authorUserId: Long?,
    val authorName: String,
    val text: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(o: PredefenseObservationEntity): PredefenseObservationDto {
            return PredefenseObservationDto(
                id = o.id!!,
                bookingId = o.booking?.id!!,
                authorUserId = o.authorUserId,
                authorName = o.authorName,
                text = o.text,
                createdAt = o.createdAt
            )
        }
    }
}
