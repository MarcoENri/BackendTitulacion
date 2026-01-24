package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.PredefenseSlotEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PredefenseSlotRepository : JpaRepository<PredefenseSlotEntity, Long> {
    fun findAllByWindow_IdOrderByStartsAtAsc(windowId: Long): List<PredefenseSlotEntity>
}
