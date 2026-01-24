package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.PredefenseBookingEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PredefenseBookingRepository : JpaRepository<PredefenseBookingEntity, Long> {
    fun findBySlot_Id(slotId: Long): PredefenseBookingEntity?
}
