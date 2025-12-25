package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.IncidentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface IncidentRepository : JpaRepository<IncidentEntity, Long> {

    @Query("select i from IncidentEntity i where i.student.id = :studentId order by i.date desc, i.id desc")
    fun findAllByStudentId(@Param("studentId") studentId: Long): List<IncidentEntity>

    @Query("select count(i) from IncidentEntity i where i.student.id = :studentId")
    fun countByStudentId(@Param("studentId") studentId: Long): Long
}
