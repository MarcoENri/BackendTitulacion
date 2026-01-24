package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.PredefenseWindowEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PredefenseWindowRepository : JpaRepository<PredefenseWindowEntity, Long> {

    fun findAllByAcademicPeriod_IdOrderByStartsAtDesc(periodId: Long): List<PredefenseWindowEntity>

    @Query("""
      select w from PredefenseWindowEntity w
      where w.academicPeriod.id = :periodId
        and w.isActive = true
        and (w.career is null or w.career.id = :careerId)
      order by w.startsAt desc
    """)
    fun findActiveForPeriodAndCareer(
        @Param("periodId") periodId: Long,
        @Param("careerId") careerId: Long
    ): List<PredefenseWindowEntity>
}
