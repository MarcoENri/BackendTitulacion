package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.PredefenseObservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PredefenseObservationRepository : JpaRepository<PredefenseObservationEntity, Long> {
    fun findAllByBooking_IdOrderByCreatedAtAsc(bookingId: Long): List<PredefenseObservationEntity>
}
