package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.PredefenseSlotEntity
import java.time.LocalDateTime

data class PredefenseSlotDto(
    val id: Long,
    val windowId: Long,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val booked: Boolean,
    val bookingId: Long?
) {
    companion object {
        fun from(slot: PredefenseSlotEntity, booked: Boolean, bookingId: Long?): PredefenseSlotDto {
            return PredefenseSlotDto(
                id = slot.id!!,
                windowId = slot.window?.id!!,
                startsAt = slot.startsAt,
                endsAt = slot.endsAt,
                booked = booked,
                bookingId = bookingId
            )
        }
    }
}
