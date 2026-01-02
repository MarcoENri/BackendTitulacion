package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.ObservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ObservationRepository : JpaRepository<ObservationEntity, Long> {
    fun findAllByStudent_Id(studentId: Long): List<ObservationEntity>
    fun countByStudent_Id(studentId: Long): Long
}
