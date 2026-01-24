package com.example.Aplicativo_web.dto

import com.example.Aplicativo_web.entity.PredefenseBookingEntity
import java.time.LocalDateTime

data class PredefenseBookingDto(
    val id: Long,
    val slotId: Long,
    val studentId: Long,
    val studentName: String,
    val studentEmail: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(b: PredefenseBookingEntity): PredefenseBookingDto {
            val s = b.student ?: throw IllegalStateException("Booking sin student")
            val slot = b.slot ?: throw IllegalStateException("Booking sin slot")
            return PredefenseBookingDto(
                id = b.id!!,
                slotId = slot.id!!,
                studentId = s.id!!,
                studentName = "${s.firstName} ${s.lastName}",
                studentEmail = s.email,
                createdAt = b.createdAt
            )
        }
    }
}
