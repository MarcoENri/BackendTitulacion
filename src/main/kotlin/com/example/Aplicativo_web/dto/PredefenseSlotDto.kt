package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.PredefenseSlotEntity
import java.time.LocalDateTime

data class PredefenseSlotDto(
    val id: Long,
    val windowId: Long,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val booked: Boolean,
    val bookingId: Long?,
    val studentId: Long? = null,
    val studentName: String? = null
) {
    companion object {
        // Nueva versión del helper que recibe studentName y studentId
        fun from(
            slot: PredefenseSlotEntity,
            booked: Boolean,
            bookingId: Long?,
            studentId: Long? = null,
            studentName: String? = null
        ): PredefenseSlotDto {
            return PredefenseSlotDto(
                id = slot.id!!,
                windowId = slot.window?.id!!,
                startsAt = slot.startsAt,
                endsAt = slot.endsAt,
                booked = booked,
                bookingId = bookingId,
                studentId = studentId,
                studentName = studentName
            )
        }
    }
}
