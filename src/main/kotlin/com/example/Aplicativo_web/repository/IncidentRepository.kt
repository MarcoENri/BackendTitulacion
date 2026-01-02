package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.IncidentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface IncidentRepository : JpaRepository<IncidentEntity, Long> {
    fun findAllByStudent_Id(studentId: Long): List<IncidentEntity>
    fun countByStudent_Id(studentId: Long): Long
}
