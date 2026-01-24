package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.AcademicPeriodEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AcademicPeriodRepository : JpaRepository<AcademicPeriodEntity, Long> {

    fun findByIsActiveTrue(): List<AcademicPeriodEntity>

    fun findFirstByIsActiveTrueOrderByStartDateDesc(): Optional<AcademicPeriodEntity>

    @Modifying
    @Query("update AcademicPeriodEntity p set p.isActive = false where p.isActive = true")
    fun deactivateAllActive()

    @Modifying
    @Query("update AcademicPeriodEntity p set p.isActive = true where p.id = :id")
    fun activateById(@Param("id") id: Long)
}
