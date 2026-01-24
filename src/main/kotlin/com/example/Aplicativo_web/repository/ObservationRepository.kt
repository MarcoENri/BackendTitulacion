package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.ObservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ObservationRepository : JpaRepository<ObservationEntity, Long> {

    fun findAllByStudent_Id(studentId: Long): List<ObservationEntity>

    @Query("select count(o) from ObservationEntity o where o.student.id = :studentId")
    fun countByStudentId(@Param("studentId") studentId: Long): Long
}
